from .models import User
from .forms import SignInForm, SignUpForm, CheckPasswordForm, UserPasswordUpdateForm, ApiPasswordResetForm, UpdateUserInfoForm
from .token import account_activation_token, message
from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.core.exceptions import ValidationError
from django.contrib.sites.shortcuts import get_current_site
from django.utils.http import urlsafe_base64_decode, urlsafe_base64_encode
from django.core.mail import EmailMessage
from django.utils.encoding import force_bytes, force_text
from django.shortcuts import render, redirect
from django.contrib.auth import login, authenticate, logout, update_session_auth_hash
from django.contrib.auth.decorators import login_required
from django.db.models.query_utils import Q
from django.contrib.auth.tokens import default_token_generator
from django.contrib import messages
from django.contrib.auth.forms import PasswordResetForm
from django.template.loader import render_to_string
from django.core.mail import send_mail, BadHeaderError
from django.utils.translation import ugettext_lazy as _
import requests
from django.utils.translation import gettext_lazy as _

BASE_URL = 'http://127.0.0.1:8000'

def signup(request):
    """
    회원가입
    회원가입 폼 양식이 유효하면, 입력한 이메일로 회원가입 인증 메일을 발송함
    """
    if request.user.is_authenticated:
        return HttpResponseRedirect('/index/')
    if request.method == 'POST':
        form = SignUpForm(request.POST)
        if form.is_valid():
            form.save()
            user = authenticate(email=form.cleaned_data['email'], password=form.cleaned_data['password1'])
            if user is not None:
                current_site = get_current_site(request)
                domain = current_site.domain
                uid64 = urlsafe_base64_encode(force_bytes(user.pk))
                token = account_activation_token.make_token(user)
                message_data = message(domain, uid64, token)
                mail_title = _("이메일 인증을 완료해 주세요")
                mail_to = form.cleaned_data['email']
                email = EmailMessage(mail_title, message_data, to=[mail_to])
                email.send()
                return HttpResponseRedirect('../signup_email_confirm/')
    else:
        form = SignUpForm()
    return render(request, 'signup.html', {'form': form})


def signin(request):
    """
    로그인
    로그인 폼 양식이 유효하면, 로그인 인증 과정을 거치고 인증이 완료되면 메인 페이지로 돌아감
    """
    if request.user.is_authenticated:
        return HttpResponseRedirect('/index/')
    args = {}
    if request.method == 'POST':
        form = SignInForm(request.POST)
        if form.is_valid():
            user = authenticate(email=form.data['email'], password=form.data['password'])
            login(request, user)
            return HttpResponseRedirect('../index/')
    else:    
        form = SignInForm()
    args['form'] = form
    return render(request, 'signin.html', args)


def signout(request):
    """
    로그아웃 한 뒤 메인페이지로 이동함
    """
    logout(request)
    return HttpResponseRedirect('../index/')


def user_activate(request, uidb64, token):
    """
    계정 활성화
    이메일로 들어온 링크 클릭 시, 그 이메일의 User object 활성화
    """
    try:
        uid = force_text(urlsafe_base64_decode(uidb64))
        user = User.objects.get(pk=uid)
        if account_activation_token.check_token(user, token):
            user.is_active = True
            user.save()
            return redirect('/index/')
    except ValidationError:
        return HttpResponse({"messge": "TYPE_ERROR"}, status=400)


def api_user_activate(request):
    """
    Api로 만든 계정 활성화
    이메일로 들어온 링크 클릭 시, 그 이메일의 User object 활성화
    """
    if request.method == 'GET':
        user_id = request.GET.get('user_id')
        timestamp = request.GET.get('timestamp')
        signature = request.GET.get('signature')
        requests.post(BASE_URL + '/api/v1/accounts/verify-registration/',
                      data={'user_id': user_id, 'timestamp': timestamp, 'signature': signature})              
    return HttpResponseRedirect('../index/')


def myinfo(request):
    """
    회원 정보 조회
    현재 로그인 되어있는 사용자의 정보를 사용자의 pk값으로 렌더링해서 보여줌
    """
    if request.user.is_authenticated:
        user_id = request.user.id
        context = {
            'users': User.objects.filter(id=user_id)
        }
        return render(request, 'myinfo.html', context)
    else:
        return HttpResponseRedirect('/signin/')


@login_required
def user_info_update(request):
    """
    회원 정보 수정
    회원 정보 수정 폼 양식이 유효하다면, 변경사항을 저장하고 변경된 회원정보를 다시 보여줌
    """
    if request.method == 'POST':
        form = UpdateUserInfoForm(request.POST, request.FILES, instance=request.user)
        if form.is_valid():
            form.save()
        else:
            messages.error(request, '형식에 맞게 작성해주세요(YYYY-MM-DD)')
            return HttpResponseRedirect('../user_info_update/')
    else:
        form = UpdateUserInfoForm(instance=request.user)
        return render(request, 'user_info_update.html', {'form': form})
    return HttpResponseRedirect("../myinfo")


@login_required
def user_delete(request):
    """
    회원 탈퇴
    회원 탈퇴를 하기 위해서는 비밀번호를 재확인 받는 절차가 선행됨
    폼 양식이 유효하다면(비밀번호 인증이 완료되면) 현재 로그인되어있는 사용자를 삭제하고 자동으로 로그아웃 시켜줌
    """
    if request.method == 'POST':
        password_form = CheckPasswordForm(request.user, request.POST)
        if password_form.is_valid():
            request.user.delete()
            logout(request)
            return redirect('../index')
        else:
            messages.error(request, '비밀번호가 일치하지 않습니다. 다시 입력해주세요')
            return HttpResponseRedirect('../user_delete/')
    else:
        password_form = CheckPasswordForm(request.user)
        return render(request, 'user_delete.html', {'password_form': password_form})
    return HttpResponseRedirect("../index")


def user_password_update(request):
    """
    회원 비밀번호 변경
    비밀번호 변경 폼이 유효하다면, 사용자의 비밀번호 정보를 새로 업데이트하고, 변경된 비밀번호로 자동으로 로그인 시켜줌
    """
    if request.method == 'POST':
        form = UserPasswordUpdateForm(request.user, request.POST)
        try:
            if form.is_valid():
                user = form.save()
                update_session_auth_hash(request, user)  # 변경된 비밀번호로 자동으로 로그인 시켜줌, 중요!
                return redirect('../index')
        except ValidationError as e:
            messages.error(request, e)
            return HttpResponseRedirect("../user_password_update")
    else:
        form = UserPasswordUpdateForm(request.user)
    return render(request, 'user_password_update.html', {'form': form})


def api_password_reset(request):
    """
    계정 활성화
    이메일로 들어온 링크 클릭 시, 그 이메일의 User object 활성화
    """
    user_id = request.GET.get('user_id')
    timestamp = request.GET.get('timestamp')
    signature = request.GET.get('signature')
    form_class = ApiPasswordResetForm
    form = form_class(request.POST or None)
    if request.method == 'POST':
        if form.is_valid():
            password = request.POST.get('new_password2')
            response_message = requests.post(BASE_URL + '/api/v1/accounts/reset-password/',
                                             data={'user_id': user_id, 'timestamp': timestamp, 'signature': signature,
                                                   'password': password})
            if response_message.status_code == 200:
                return HttpResponseRedirect('../signin/')
            else:
                template = loader.get_template("user_password_find_error.html")
                res_text = response_message.text
                return HttpResponse(template.render({"data": res_text}))
    return render(request, 'user_password_find.html', {'form': form})


def user_password_find(request):
    """
    비밀번호 찾기
    회원가입 시 사용했던 메일을 입력받아 존재하는 사용자일 경우 비밀번호 찾기 링크 전송
    """
    if request.method == "POST":
        password_reset_form = PasswordResetForm(request.POST)
        if password_reset_form.is_valid():
            data = password_reset_form.cleaned_data['email']
            associated_users = User.objects.filter(Q(email=data))
            if associated_users.exists():
                for user in associated_users:
                    subject = "Password Reset Requested"
                    email_template_name = "password_reset_email.txt"
                    c = {
                        "email": user.email,
                        'domain': '127.0.0.1:8000',
                        'site_name': 'Website',
                        "uid": urlsafe_base64_encode(force_bytes(user.pk)),
                        "user": user,
                        'token': default_token_generator.make_token(user),
                        'protocol': 'http',
                    }
                    email = render_to_string(email_template_name, c)
                    try:
                        send_mail(subject, email, 'sch.iot.esc@gmail.com', [user.email], fail_silently=False)
                    except BadHeaderError:
                        return HttpResponse('Invalid header found.')
                    return redirect("/password_reset/done/")
                    # 이메일로 url을 성공적으로 잘 보냄
            else:
                messages.error(request, '유효하지 않은 이메일입니다.')
    password_reset_form = PasswordResetForm()
    return render(request=request, template_name="user_password_find.html",
                  context={"password_reset_form": password_reset_form})