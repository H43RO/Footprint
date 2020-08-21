from django.contrib.auth.decorators import login_required
from .backends import EmailAuthBackend
from django.http import HttpResponse, HttpResponseRedirect, request
from django.core.exceptions import ValidationError
from django.core.validators import validate_email
from django.contrib.sites.shortcuts import get_current_site
from django.utils.http import urlsafe_base64_decode, urlsafe_base64_encode
from django.core.mail import EmailMessage
from django.utils.encoding import force_bytes, force_text
from django.shortcuts import render, get_object_or_404, redirect
from django.contrib.auth import login, authenticate, logout, update_session_auth_hash
from django.contrib.auth.decorators import login_required
from django.contrib import messages, auth
from django.db import transaction
from django.db.models import Count, Avg
from django.core.paginator import Paginator

from .models import User, History, Place, Post
from .forms import SignUpForm, PlaceRegisterForm, SignInForm, HistoryForm, UpdateHistoryForm, UpdateUserInfoForm, CheckPasswordForm, UserPasswordUpdateForm, ApiPasswordResetForm
from rest_framework.response import Response
from .backends import EmailAuthBackend
from .token import account_activation_token, message
from django.utils.translation import gettext_lazy as _

import requests
from django.template import loader
from django.core.mail import send_mail, BadHeaderError
from django.contrib.auth.forms import PasswordResetForm
from django.template.loader import render_to_string
from django.db.models.query_utils import Q
from django.contrib.auth.tokens import default_token_generator
from django.template import loader
from django.utils import timezone, dateformat
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.options import Options


def index(request):
    sights = Place.objects.filter(place_div=0).order_by('-count')[:6]
    restaurants = Place.objects.filter(place_div=1).order_by('-count')[:6]
    return render(request, 'index.html', {'sights': sights, 'restaurants': restaurants})


def list(request):
    user = User.objects.all()
    context = {
        'users': user
    }
    return render(request, 'list.html', context)


def signup(request):
    if request.method == 'POST':
        form = SignUpForm(request.POST)
        if form.is_valid():
            form.save()
            user = authenticate(username=form.cleaned_data['email'], password=form.cleaned_data['password1'])
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
                # login(request, user)
                return HttpResponseRedirect('../list/')
    else:
        form = SignUpForm()
    return render(request, 'signup.html', {'form': form})


def signin(request):
    if request.method == 'POST':
        form = SignInForm(data=request.POST)
        if form.is_valid():
            user = authenticate(username=form.cleaned_data['username'], password=form.cleaned_data['password'])
            if user is not None:
                login(request, user)
                return HttpResponseRedirect('../index/')
        else:
            messages.error(request, '이메일 혹은 비밀번호를 다시 입력해주세요')
            return HttpResponseRedirect('../signin/')

    else:
        form = SignInForm()
    return render(request, 'signin.html', {'form': form})


def signout(request):
    auth.logout(request)
    return HttpResponseRedirect('../index/')


def user_activate(request, uidb64, token):
    try:
        uid = force_text(urlsafe_base64_decode(uidb64))
        user = User.objects.get(pk=uid)

        if account_activation_token.check_token(user, token):
            user.is_active = True
            user.save()
            return redirect('../place_search/')

    except ValidationError:
        return HttpResponse({"messge": "TYPE_ERROR"}, status=400)


def api_user_activate(request):
    if request.method == 'GET':
        user_id = request.GET.get('user_id')
        timestamp = request.GET.get('timestamp')
        signature = request.GET.get('signature')
        requests.post('http://127.0.0.1:8000/api/v1/accounts/verify-registration/', data={'user_id' : user_id, 'timestamp' : timestamp, 'signature' : signature  })
    return HttpResponseRedirect('../index/')


def myinfo(request):
    if request.user.is_authenticated:
        user_id = request.user.id
        context = {
            'users': User.objects.filter(id=user_id)
        }
        return render(request, 'myinfo.html', context)


def place_list(request):
    places = Place.objects.all()
    histories = History.objects.all()
    return render(request, 'place_list.html', {'places':places, 'histories': histories})


def place_detail(request, id):
    # if id is not None:
    #     places = get_object_or_404(Place, pk=id)
    #     return render(request, 'place_detail.html', {'places': places})
    # return HttpResponseRedirect('history/')
    context = {
        'places': place_detail_crawl(pk=id)
    }
    return render(request, 'place_detail.html', context)



def place_register(request):
    if request.method == 'POST':
        form = PlaceRegisterForm(request.POST)
        if form.is_valid():
            new_item = form.save()
        return HttpResponseRedirect('../place_list')
    form = PlaceRegisterForm()
    return render(request, 'place_register.html', {'form': form})


def place_restaurant(request):
    context = {
        'restaurants': Place.objects.filter(place_div=1)
    }
    return render(request, 'place_restaurant_list.html', context)


def place_sights(request):
    context = {
        'sights': Place.objects.filter(place_div=0)
    }
    return render(request, 'place_sights_list.html', context)


def place_search(request):
    place_search = Place.objects.all()
    q = request.POST.get('q', "")

    if q:
        place_search = place_search.filter(title__icontains=q)
        return render(request, 'place_search.html', {'place_search': place_search, 'q': q})
    else:
        return render(request, 'place_search.html')


def history(request):
    if request.method == 'POST' and 'id' in request.POST:
        item = get_object_or_404(History, id=id, user=request.user)
        item.delete()
        return redirect('history-delete')
    historys = History.objects.all().order_by('created_at')
    context = {
        'historys': historys,
    }
    return render(request, 'history_list.html', context)


def history_create(request):
    if request.method == 'POST':
        if request.POST['created_at'] == '':
            request.POST._mutable = True
            formatted_date = dateformat.format(timezone.now(), 'Y-m-d H:i:s')
            request.POST['created_at'] = formatted_date
            form = HistoryForm(request.POST, request.FILES)
            if form.is_valid():
                new_item = form.save()
            return HttpResponseRedirect('../')
        else:
            form = HistoryForm(request.POST, request.FILES)
            if form.is_valid():
                new_item = form.save()
            return HttpResponseRedirect('../')
    form = HistoryForm(request.FILES)
    return render(request, 'history_create.html', {'form': form})


def history_delete(request, id):
    item = get_object_or_404(History, pk=id)
    if request.method == 'POST':
        item.delete()
        return redirect('history')  # 리스트 화면으로 이동합니다.

    return render(request, 'history_delete.html', {'item': item})


def history_update(request):
    if request.method == 'POST' and 'id' in request.POST:
        item = get_object_or_404(History, pk=request.POST.get('id'))
        form = UpdateHistoryForm(request.POST, request.FILES, instance=item)
        if form.is_valid():
            item = form.save()
    elif 'id' in request.GET:
        item = get_object_or_404(History, pk=request.GET.get('id'))
        form = HistoryForm(request.FILES, instance=item)
        form.password = ''  # password 데이터를 비웁니다.
        return render(request, 'history_update.html', {'form': form})
    return HttpResponseRedirect("../")


@login_required
def user_info_update(request):
    if request.method == 'POST':
        form = UpdateUserInfoForm(request.POST, instance=request.user)
        if form.is_valid():
            form.save()
    elif 'id' in request.GET:
        form = UpdateUserInfoForm(instance=request.user)
        return render(request, 'user_info_update.html', {'form': form})
    return HttpResponseRedirect("../myinfo")


@login_required
def user_delete(request):
    if request.method == 'POST':
        password_form = CheckPasswordForm(request.user, request.POST)
        if password_form.is_valid():
            request.user.delete()
            logout(request)
            return redirect('../list')
        else:
            messages.error(request, '비밀번호가 일치하지 않습니다. 다시 입력해주세요')
            return HttpResponseRedirect('../user_delete/')
    else:
        password_form = CheckPasswordForm(request.user)
        return render(request, 'user_delete.html', {'password_form': password_form})
    return HttpResponseRedirect("../list")


def user_password_update(request):
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
    user_id = request.GET.get('user_id')
    timestamp = request.GET.get('timestamp')
    signature = request.GET.get('signature')
    form_class = ApiPasswordResetForm
    form = form_class(request.POST or None)
    if request.method == 'POST':
        if form.is_valid():
            password = request.POST.get('new_password2')
            response_message = requests.post('http://127.0.0.1:8000/api/v1/accounts/reset-password/', data={'user_id' : user_id, 'timestamp' : timestamp, 'signature' : signature, 'password' : password })  
            if response_message.status_code == 200:
                return HttpResponseRedirect('../signin/') 
            else:
                template = loader.get_template("user_password_find_error.html")
                res_text = response_message.text
                return HttpResponse(template.render({"data" : res_text}))
    return render(request, 'user_password_find.html', {'form' : form })

def user_password_find(request):
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
                        send_mail(subject, email, 'pcj980@gmail.com', [user.email], fail_silently=False)
                    except BadHeaderError:
                        return HttpResponse('Invalid header found.')
                    return redirect("/password_reset/done/")
                    # 이메일로 url을 성공적으로 잘 보냄
            else:
                messages.error(request, '유효하지 않은 이메일입니다.')

    password_reset_form = PasswordResetForm()
    return render(request=request, template_name="user_password_find.html", context={"password_reset_form": password_reset_form})


def noticelist(request):
    notices = Post.objects.filter(post_div=1)
    return render(request, 'notice.html', {'notices': notices})


def noticeview(request, id):
    notices = Post.objects.get(id=id)
    return render(request, 'notice_view.html', {'notices': notices})


def editor(request):
    editors = Post.objects.filter(post_div=0)
    return render(request, 'editor.html', {'editors' : editors})


def editorview(request, id):
    editors = Post.objects.get(id=id)
    return render(request, 'editor_view.html', {'editors': editors})


def place_detail_crawl(pk):
    URL = 'https://store.naver.com/restaurants/detail?id'

    result = requests.get(f'{URL}={pk}')
    soup = BeautifulSoup(result.content, 'html.parser')
    title = soup.find("strong", {"class": "name"})
    title = str(title.string).strip()
    print(title)

    category = soup.find("span", {"class": "category"})
    category = str(category.string).strip()
    print(category)

    location = soup.find("span", {"class": "addr"})
    location = str(location.string).strip()
    print(location)

    open = soup.find("span", {"class": "time"})
    if open is not None:
        open = str(open.string).strip()
        print(open)
    else:
        open = " "
        print(open)

    description = soup.find("div", {"class": "info"})
    desc = description.find("span", {"class": "txt"})
    if desc is not None:
        tag = soup.find("span", {"class": "kwd"})
        if tag is not None:
            desc = " "
            print(desc)
        else:
            desc = str(desc.string).strip()
            print(desc)
    else:
        desc = " "
        print(desc)

    URL_IMG = 'https://store.naver.com/restaurants/detail?id'
    result_IMG = requests.get(f'{URL_IMG}={pk}&tab=photo')
    soups = BeautifulSoup(result_IMG.content, 'html.parser')

    area = soups.find("div", {"class": "list_photo"})
    a = area.find("a")
    if a is not None:
        img = a.find("img").get("src")
        print(img)
    else:
        a = area.find("div")
        img = a.find("img").get("src")
        print(img)

    list_menu = soup.find("ul", {"class": "list_menu"})
    menu = list_menu.find_all("span", {"class": "name"})
    menu_result = []
    for item in menu:
        menu_result.append(item.get_text())
    print(menu_result)

    price = soup.find_all("em", {"class": "price"})
    print(price)
    price_result = []
    for item in price:
        price_result.append(item.get_text())
    print(price_result)


    return {
        'title': title,
        'category': category,
        'location': location,
        'open': open,
        'description': desc,
        'menu': menu_result,
        'price': price_result,
        'img' : img
    }
