
# from .backends import EmailAuthBackend
from django.http import HttpResponse, HttpResponseRedirect, request
# from django.core.exceptions import ValidationError
from django.core.validators import validate_email
# from django.contrib.sites.shortcuts import get_current_site
# from django.utils.http import urlsafe_base64_decode, urlsafe_base64_encode
# from django.core.mail import EmailMessage
# from django.utils.encoding import force_bytes, force_text
from django.shortcuts import render, get_object_or_404, redirect
# from django.contrib.auth import login, authenticate, logout, update_session_auth_hash
# from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.db import transaction
from django.db.models import Count, Avg
from django.core.paginator import Paginator
from .models import User, History, Place, Post, HotPlace
from .forms import SignUpForm, SignInForm, HistoryForm, UpdateHistoryForm, UpdateUserInfoForm, \
    CheckPasswordForm, UserPasswordUpdateForm, ApiPasswordResetForm
from rest_framework.response import Response
from .backends import EmailAuthBackend
from .token import account_activation_token, message
from django.utils.translation import gettext_lazy as _
import requests
from django.template import loader
# from django.core.mail import send_mail, BadHeaderError
# from django.contrib.auth.forms import PasswordResetForm
# from django.template.loader import render_to_string
from django.db.models.query_utils import Q
# from django.contrib.auth.tokens import default_token_generator
from django.template import loader
from django.utils import timezone, dateformat
from bs4 import BeautifulSoup
from multiprocessing import Pool, Manager
import pymysql
import json


def index(request):
    """
     메인화면 페이지
     Hotplace 정보를 6개 전달하여 보여줌
    """
    sights = Place.objects.filter(place_div=0).order_by('-count')[:6]
    restaurants = Place.objects.filter(place_div=1).order_by('-count')[:6]
    user = request.user
    return render(request, 'index.html', {'sights': sights, 'restaurants': restaurants, 'user': user})

# def signup(request):
#     """
#     회원가입
#     회원가입 폼 양식이 유효하면, 입력한 이메일로 회원가입 인증 메일을 발송함
#     """
#     if request.user.is_authenticated:
#         return HttpResponseRedirect('/index/')
#     if request.method == 'POST':
#         form = SignUpForm(request.POST)
#         if form.is_valid():
#             form.save()
#             user = authenticate(username=form.cleaned_data['email'], password=form.cleaned_data['password1'])
#             if user is not None:
#                 current_site = get_current_site(request)
#                 domain = current_site.domain
#                 uid64 = urlsafe_base64_encode(force_bytes(user.pk))
#                 token = account_activation_token.make_token(user)
#                 message_data = message(domain, uid64, token)
#                 mail_title = _("이메일 인증을 완료해 주세요")
#                 mail_to = form.cleaned_data['email']
#                 email = EmailMessage(mail_title, message_data, to=[mail_to])
#                 email.send()
                
#                 return HttpResponseRedirect('../signup_email_confirm/')

#     else:
#         form = SignUpForm()
#     return render(request, 'signup.html', {'form': form})


# def signin(request):
#     """
#     로그인
#     로그인 폼 양식이 유효하면, 로그인 인증 과정을 거치고 인증이 완료되면 메인 페이지로 돌아감
#     """
#     if request.user.is_authenticated:
#         return HttpResponseRedirect('/index/')
#     if request.method == 'POST':
#         form = SignInForm(data=request.POST)
#         if form.is_valid():
#             user = authenticate(username=form.cleaned_data['email'], password=form.cleaned_data['password'])
#             if user is not None:
#                 if user.is_active is True:
#                     login(request, user)
#                     return HttpResponseRedirect('../index/')
#                 else:
#                     messages.error(request, '인증되지 않은 이메일입니다.')
#                     return HttpResponseRedirect('../signin/')
#             else:
#                 messages.error(request, '이메일 혹은 비밀번호를 다시 입력해주세요')
#                 return HttpResponseRedirect('../signin/')
#     else:
#         form = SignInForm()
#     return render(request, 'signin.html', {'form': form})


# def signout(request):
#     """
#     로그아웃 한 뒤 메인페이지로 이동함
#     """
#     auth.logout(request)
#     return HttpResponseRedirect('../index/')


# def user_activate(request, uidb64, token):
#     """
#     계정 활성화
#     이메일로 들어온 링크 클릭 시, 그 이메일의 User object 활성화
#     """
#     try:
#         uid = force_text(urlsafe_base64_decode(uidb64))
#         user = User.objects.get(pk=uid)
#         if account_activation_token.check_token(user, token):
#             user.is_active = True
#             user.save()
#             return redirect('/index/')
#     except ValidationError:
#         return HttpResponse({"messge": "TYPE_ERROR"}, status=400)


# def api_user_activate(request):
#     """
#     Api로 만든 계정 활성화
#     이메일로 들어온 링크 클릭 시, 그 이메일의 User object 활성화
#     """
#     if request.method == 'GET':
#         user_id = request.GET.get('user_id')
#         timestamp = request.GET.get('timestamp')
#         signature = request.GET.get('signature')
#         requests.post('http://127.0.0.1:8000/api/v1/accounts/verify-registration/',
#                       data={'user_id': user_id, 'timestamp': timestamp, 'signature': signature})
#     return HttpResponseRedirect('../index/')


# def myinfo(request):
#     """
#     회원 정보 조회
#     현재 로그인 되어있는 사용자의 정보를 사용자의 pk값으로 렌더링해서 보여줌
#     """
#     if request.user.is_authenticated:
#         user_id = request.user.id
#         context = {
#             'users': User.objects.filter(id=user_id)
#         }
#         return render(request, 'myinfo.html', context)
#     else:
#         return HttpResponseRedirect('/signin/')


def place_detail(request, id):
    """
    장소 자세히보기
    크롤링한 데이터를 기반으로 한 장소 자세히보기 페이지 보여줌
    """
    context = {
        'places': place_detail_crawl(pk=id)
    }
    return render(request, 'place_detail.html', context)


def history(request):
    """
    히스토리회(일기) 조회
    생성 날짜 순으로 리스트를 보여줌
    삭제 버튼이 눌렸을 시, 전달된 id값을 통해 item 삭제
    로그인하지 않은 유저가 히스토리 접근할 경우 로그인 페이지로 리다이렉트함
    """
    if not request.user.is_authenticated:
        return HttpResponseRedirect('/signin/')
    historys = History.objects.filter(user_id=request.user.pk).order_by('created_at')
    context = {
        'historys': historys,
    }
    return render(request, 'history_list.html', context)


def history_create(request):
    """
    히스토리(일기) 생성
    임의로 작성할 수 있는 'created_at' field가 빈 폼일 경우 자동으로 현재 시간 생성
    """
    if request.method == 'POST':
        if request.POST['created_at'] == '':
            request.POST._mutable = True
            formatted_date = dateformat.format(timezone.now(), 'Y-m-d H:i:s')
            request.POST['created_at'] = formatted_date
            request.POST['user'] = request.user
            form = HistoryForm(request.POST, request.FILES)
            if form.is_valid():
                new_item = form.save()
            return HttpResponseRedirect('../')
        else:
            form = HistoryForm(request.POST, request.FILES)
            request.POST._mutable = True
            request.POST['user'] = request.user
            if form.is_valid():
                new_item = form.save()
            else:
                messages.error(request, '작성 시간은 YYYY-mm-dd h:m:s 양식입니다.')
                return redirect('history-create')
            return HttpResponseRedirect('../')
    form = HistoryForm(request.FILES)
    return render(request, 'history_create.html', {'form': form})


def history_delete(request, id):
    """
    히스토리(일기) 삭제
    해당하는 id의 History item 삭제
    """
    item = get_object_or_404(History, pk=id)
    if request.method == 'POST':
        item.delete()
        return redirect('history')  # 리스트 화면으로 이동합니다.
    return render(request, 'history_delete.html', {'item': item})


def history_update(request):
    """
    히스토리(일기) 수정
    해당하는 id의 History item 수정
    글 수정 시, 기존의 내용 폼에 유지
    임의로 작성할 수 있는 'created_at' field가 빈 폼일 경우 자동으로 현재 시간 생성
    """
    if request.method == 'POST' and 'id' in request.POST:
        if request.POST['created_at'] == '':
            request.POST._mutable = True
            formatted_date = dateformat.format(timezone.now(), 'Y-m-d H:i:s')
            request.POST['created_at'] = formatted_date
        item = get_object_or_404(History, pk=request.POST.get('id'))
        form = UpdateHistoryForm(request.POST, request.FILES, instance=item)
        if form.is_valid():
            item = form.save()
    elif 'id' in request.GET:
        item = get_object_or_404(History, pk=request.GET.get('id'))
        form = HistoryForm(instance=item)
        return render(request, 'history_update.html', {'form': form})
    return HttpResponseRedirect("../")


# @login_required
# def user_info_update(request):
#     """
#     회원 정보 수정
#     회원 정보 수정 폼 양식이 유효하다면, 변경사항을 저장하고 변경된 회원정보를 다시 보여줌
#     """
#     if request.method == 'POST':
#         form = UpdateUserInfoForm(request.POST, request.FILES, instance=request.user)
#         if form.is_valid():
#             form.save()
#     elif 'id' in request.GET:
#         form = UpdateUserInfoForm(instance=request.user)
#         return render(request, 'user_info_update.html', {'form': form})
#     return HttpResponseRedirect("../myinfo")


# @login_required
# def user_delete(request):
#     """
#     회원 탈퇴
#     회원 탈퇴를 하기 위해서는 비밀번호를 재확인 받는 절차가 선행됨
#     폼 양식이 유효하다면(비밀번호 인증이 완료되면) 현재 로그인되어있는 사용자를 삭제하고 자동으로 로그아웃 시켜줌
#     """
#     if request.method == 'POST':
#         password_form = CheckPasswordForm(request.user, request.POST)
#         if password_form.is_valid():
#             request.user.delete()
#             logout(request)
#             return redirect('../index')
#         else:
#             messages.error(request, '비밀번호가 일치하지 않습니다. 다시 입력해주세요')
#             return HttpResponseRedirect('../user_delete/')
#     else:
#         password_form = CheckPasswordForm(request.user)
#         return render(request, 'user_delete.html', {'password_form': password_form})
#     return HttpResponseRedirect("../index")


# def user_password_update(request):
#     """
#     회원 비밀번호 변경
#     비밀번호 변경 폼이 유효하다면, 사용자의 비밀번호 정보를 새로 업데이트하고, 변경된 비밀번호로 자동으로 로그인 시켜줌
#     """
#     if request.method == 'POST':
#         form = UserPasswordUpdateForm(request.user, request.POST)
#         try:
#             if form.is_valid():
#                 user = form.save()
#                 update_session_auth_hash(request, user)  # 변경된 비밀번호로 자동으로 로그인 시켜줌, 중요!
#                 return redirect('../index')
#         except ValidationError as e:
#             messages.error(request, e)
#             return HttpResponseRedirect("../user_password_update")
#     else:
#         form = UserPasswordUpdateForm(request.user)
#     return render(request, 'user_password_update.html', {'form': form})


# def api_password_reset(request):
#     """
#     계정 활성화
#     이메일로 들어온 링크 클릭 시, 그 이메일의 User object 활성화
#     """
#     user_id = request.GET.get('user_id')
#     timestamp = request.GET.get('timestamp')
#     signature = request.GET.get('signature')
#     form_class = ApiPasswordResetForm
#     form = form_class(request.POST or None)
#     if request.method == 'POST':
#         if form.is_valid():
#             password = request.POST.get('new_password2')
#             response_message = requests.post('http://127.0.0.1:8000/api/v1/accounts/reset-password/',
#                                              data={'user_id': user_id, 'timestamp': timestamp, 'signature': signature,
#                                                    'password': password})
#             if response_message.status_code == 200:
#                 return HttpResponseRedirect('../signin/')
#             else:
#                 template = loader.get_template("user_password_find_error.html")
#                 res_text = response_message.text
#                 return HttpResponse(template.render({"data": res_text}))
#     return render(request, 'user_password_find.html', {'form': form})


# def user_password_find(request):
#     """
#     비밀번호 찾기
#     회원가입 시 사용했던 메일을 입력받아 존재하는 사용자일 경우 비밀번호 찾기 링크 전송
#     """
#     if request.method == "POST":
#         password_reset_form = PasswordResetForm(request.POST)
#         if password_reset_form.is_valid():
#             data = password_reset_form.cleaned_data['email']
#             associated_users = User.objects.filter(Q(email=data))
#             if associated_users.exists():
#                 for user in associated_users:
#                     subject = "Password Reset Requested"
#                     email_template_name = "password_reset_email.txt"
#                     c = {
#                         "email": user.email,
#                         'domain': '127.0.0.1:8000',
#                         'site_name': 'Website',
#                         "uid": urlsafe_base64_encode(force_bytes(user.pk)),
#                         "user": user,
#                         'token': default_token_generator.make_token(user),
#                         'protocol': 'http',
#                     }
#                     email = render_to_string(email_template_name, c)
#                     try:
#                         send_mail(subject, email, 'pcj980@gmail.com', [user.email], fail_silently=False)
#                     except BadHeaderError:
#                         return HttpResponse('Invalid header found.')
#                     return redirect("/password_reset/done/")
#                     # 이메일로 url을 성공적으로 잘 보냄
#             else:
#                 messages.error(request, '유효하지 않은 이메일입니다.')
#     password_reset_form = PasswordResetForm()
#     return render(request=request, template_name="user_password_find.html",
#                   context={"password_reset_form": password_reset_form})


def notice_list(request):
    """
     공지사항 리스트 보여줌
    """
    notices = Post.objects.filter(post_div=1)
    return render(request, 'notice.html', {'notices': notices})


def noticeview(request, id):
    """
     선택한 공지사항 자세히보기 페이지
    """
    notices = Post.objects.get(id=id)
    return render(request, 'notice_view.html', {'notices': notices})


def editor_list(request):
    """
     Editor Pick 리스트 보여줌줌
    """
    editors = Post.objects.filter(post_div=0)
    return render(request, 'editor.html', {'editors': editors})


def editorview(request, id):
    """
     선택한 Editor Pick 자세히보기 페이지
    """
    editors = Post.objects.get(id=id)
    return render(request, 'editor_view.html', {'editors': editors})


def place_detail_crawl(pk):
    """
     네이버 플레이스 페이지를 크롤링
    """
    URL = 'https://store.naver.com/restaurants/detail?id'
    naverPlaceID = pk
    result = requests.get(f'{URL}={pk}')
    soup = BeautifulSoup(result.content, 'html.parser')

    title = soup.find("strong", {"class": "name"})
    title = str(title.string).strip()

    category = soup.find("span", {"class": "category"})
    category = str(category.string).strip()

    location = soup.find("span", {"class": "addr"})
    location = str(location.string).strip()

    businessHours = soup.find("span", {"class": "time"})
    if businessHours is not None:
        if businessHours is soup.find("span", {"class": "highlight"}):
            businessHours = str(businessHours.string).strip()
        else:
            businessHours = " "
    else:
        businessHours = " "

    desc = soup.find("div", {"class": "info"})
    description = desc.find("span", {"class": "txt"})
    if description is not None:
        tag = soup.find("span", {"class": "kwd"})
        if tag is not None:
            description = " "
        else:
            description = str(description.string).strip()
    else:
        description = " "

    URL_IMG = 'https://store.naver.com/restaurants/detail?id'
    result_IMG = requests.get(f'{URL_IMG}={pk}&tab=photo')

    soups = BeautifulSoup(result_IMG.content, 'html.parser')
    area = soups.find("div", {"class": "list_photo"})
    a = area.find("a")
    if a is not None:
        imageSrc = a.find("img").get("src")
    else:
        a = area.find("div")
        imageSrc = a.find("img").get("src")

    menuName = []
    list_menu = soup.find("ul", {"class": "list_menu"})
    if list_menu is not None:
        menu = list_menu.find_all("span", {"class": "name"})
        for item in menu:
            menuName.append(item.get_text())
        menuNames = menuName
        menuName=json.dumps(menuName,ensure_ascii=False)
    else:
        menuName = []
        menuNames = ""
    price = soup.find_all("em", {"class": "price"})

    menuPrice = []
    if price is not None:
        for item in price:
            menuPrice.append(item.get_text())
        menuPrices = menuPrice
        menuPrice = json.dumps(menuPrice,ensure_ascii=False)
    else:
        menuPrice = []
        menuPrices = ""

    res = {
        'naverPlaceID': naverPlaceID,
        'title': title,
        'category': category,
        'location': location,
        'businessHours': businessHours,
        'description': description,
        'imageSrc': imageSrc,
        'menuName': menuName,
        'menuNames': menuNames,
        'menuPrices': menuPrices,
        'menuPrice': menuPrice,
    }
    add_to_db(res)

    return res


def add_to_db(crawled_items):
    """
     크롤링한 Hotplace 데이터를 Database(Mysql)에 저장함
    """
    db = pymysql.connect(host='localhost', user='root', password='080799', db='footprint', charset='utf8')
    cursor = db.cursor(pymysql.cursors.DictCursor)
    items_to_insert_into_db = {}
    items_to_insert_into_db = crawled_items
    item_naverPlaceID = items_to_insert_into_db['naverPlaceID']
    item_title = items_to_insert_into_db['title']
    item_category = items_to_insert_into_db['category']
    item_location = items_to_insert_into_db['location']
    item_businessHours = items_to_insert_into_db['businessHours']
    item_description = items_to_insert_into_db['description']
    item_imageSrc = items_to_insert_into_db['imageSrc']
    item_menuName = items_to_insert_into_db['menuName']
    item_menuPrice = items_to_insert_into_db['menuPrice']
    item_count = 0
    # 만약DB에 추가된 naverPlaceID와 동일한id가 없다면 새로 INSERT, 동일한 id 값이 있다면 UPDATE
    sql = "INSERT IGNORE INTO website_hotplace (naverPlaceID, title, category, location, businessHours, description, imageSrc, menuName, menuPrice, counts) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
    val = (item_naverPlaceID, item_title, item_category, item_location, item_businessHours, item_description, item_imageSrc, item_menuName, item_menuPrice, item_count)
    cursor.execute(sql, val)
    db.commit()
    db.close()


def get_hotplace():
    """
     현재 Hotplace인 장소 5개 NaverPlaceID 정보를 제공함
    """
    hotplaces = Place.objects.order_by('-count')[:5]
    res = []
    for item in hotplaces:
        res.append(item.naver_place_id)
        place_detail_crawl(item.naver_place_id)
    return res