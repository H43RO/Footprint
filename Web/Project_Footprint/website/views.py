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

from .models import User, History, Place, Post, HotPlace
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
from multiprocessing import Pool, Manager
import logging
logger = logging.getLogger('test')
import pymysql
# import os
# os.environ.setdefault('DJANGO_SETTINGS_MODULE', "ClienCrawlingDjango.settings")
import django
django.setup()
logger.error('되는가?디비셋업')
import json


def index(request):
    sights = Place.objects.filter(place_div=0).order_by('-count')[:6]
    restaurants = Place.objects.filter(place_div=1).order_by('-count')[:6]
    user = request.user
    return render(request, 'index.html', {'sights': sights, 'restaurants': restaurants, 'user' : user})


def list(request):
    user = User.objects.all()
    context = {
        'users': user
    }
    return render(request, 'list.html', context)


# 회원가입
# 회원가입 폼 양식이 유효하면, 입력한 이메일로 회원가입 인증 메일을 발송함
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


# 로그인
# 로그인 폼 양식이 유효하면, 로그인 인증 과정을 거치고 인증이 완료되면 메인 페이지로 돌아감
def signin(request):
    if request.method == 'POST':
        form = SignInForm(data=request.POST)
        if form.is_valid():
            user = authenticate(username=form.cleaned_data['username'], password=form.cleaned_data['password'])
            # authenticate()를 통해 DB의 username과 password를 클라이언트가 요청한 값과 비교함
            if user is not None:
                login(request, user)
                return HttpResponseRedirect('../index/')
        else:
            messages.error(request, '이메일 혹은 비밀번호를 다시 입력해주세요')
            return HttpResponseRedirect('../signin/')
    else:
        form = SignInForm()
    return render(request, 'signin.html', {'form': form})


# 로그아웃 한 뒤 메인페이지로 이동함
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


# 회원 정보 조회
# 현재 로그인 되어있는 사용자의 정보를 사용자의 pk값으로 렌더링해서 보여줌
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
    context = {
        'places': place_detail_crawl(pk=id)
    }
    return render(request, 'place_detail.html', context)


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


# 회원 정보 수정
# 회원 정보 수정 폼 양식이 유효하다면, 변경사항을 저장하고 변경된 회원정보를 다시 보여줌
@login_required
def user_info_update(request):
    if request.method == 'POST':
        form = UpdateUserInfoForm(request.POST, request.FILES, instance=request.user)
        if form.is_valid():
            form.save()
    elif 'id' in request.GET:
        form = UpdateUserInfoForm(instance=request.user)
        return render(request, 'user_info_update.html', {'form': form})
    return HttpResponseRedirect("../myinfo")


# 회원 탈퇴
# 회원 탈퇴를 하기 위해서는 비밀번호를 재확인 받는 절차가 선행됨
# 폼 양식이 유효하다면(비밀번호 인증이 완료되면) 현재 로그인되어있는 사용자를 삭제하고 자동으로 로그아웃 시켜줌
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


# 회원 비밀번호 변경
# 비밀번호 변경 폼이 유효하다면, 사용자의 비밀번호 정보를 새로 업데이트하고, 변경된 비밀번호로 자동으로 로그인 시켜줌
def user_password_update(request):
    if request.method == 'POST':
        form = UserPasswordUpdateForm(request.user, request.POST)
        try:
            if form.is_valid():
                user = form.save()
                update_session_auth_hash(request, user)
                # 변경된 비밀번호로 자동으로 로그인 시켜줌
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


# 비밀번호 찾기
# 회원가입 시 사용했던 메일을 입력받아 존재하는 사용자일 경우 비밀번호 찾기 링크 전송
def user_password_find(request):
    if request.method == "POST":
        password_reset_form = PasswordResetForm(request.POST)
        if password_reset_form.is_valid():
            data = password_reset_form.cleaned_data['email']
            # form.cleaned_data : 입력값의 유효성을 검사해주는 기본 도구. 안전하지 않을 수 있는 입력값을 정화시키고, 해당 입력값에 맞는 표준 형식으로 바꿔줌
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
                    # 보내려는 이메일 템플릿과 함께 이메일 제목을 지정함
                    # 이메일, 도메인, uid 및 토큰과 같은 이메일 콘텐츠에 필요한 모든 정보를 전달할 수 있는데,
                    # 딕셔너리의의 마지막 두 값은 사용자가 암호만 재설정 할 수 있는 고유한 도메인 슬러그를 생성하는 것
                    email = render_to_string(email_template_name, c)
                    try:
                        send_mail(subject, email, 'pcj980@gmail.com', [user.email], fail_silently=False)
                        # send_mail사용자를 /password_reset/done 페이지로 리디렉션하기 전에 제목,이메일,주소를 보내고 받는 기능을 추가해야 함
                        # 이 기능은 실제 이메일 주소가 아닌 터미널로 재설정 이메일을 보내도록 구성되어 있음
                        # 프로덕션 중에 도메인, 사이트 이름, 프로토콜 및 보낸 사람 이메일 주소를 변경해야함
                    except BadHeaderError:
                        return HttpResponse('Invalid header found.')
                    return redirect("/password_reset/done/")
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


# HotPlace 크롤링 함수
def place_detail_crawl(pk):
    URL = 'https://store.naver.com/restaurants/detail?id'
    naverPlaceID = int(pk)

    result = requests.get(f'{URL}={pk}')
    soup = BeautifulSoup(result.content, 'html.parser')
    title = soup.find("strong", {"class": "name"})
    title = title.get_text()

    category = soup.find("span", {"class": "category"})
    category = category.get_text()

    location = soup.find("span", {"class": "addr"})
    location = location.get_text()

    businessHours = soup.find("div", {"class": "biztime"})
    if businessHours is not None:
        businessHours = businessHours.get_text()
    else:
        businessHours = " "

    desc = soup.find("div", {"class": "info"})
    description = desc.find("span", {"class": "txt"})
    if description is not None:
        tag = soup.find("span", {"class": "kwd"})
        if tag is not None:
            description = " "
        else:
            description = description.get_text()
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
    else:
        menuName = ""
    json.dumps(menuName)
    print(menuName)

    # price = soup.find_all("em", {"class": "price"})
    # menuPrice = []
    # for item in price:
    #     menuPrice.append(item.get_text())

    res = {
        'naverPlaceID': naverPlaceID,
        'title': title,
        'category': category,
        'location': location,
        'businessHours': businessHours,
        'description': description,
        'imageSrc': imageSrc,
        'menuName': menuName,
        # 'menuName': menuName,
        # 'menuPrice': menuPrice,
    }
    logger.error('되는가?크롤링')

    add_new_items(res)
    return res


def add_new_items(crawled_items):
    db = pymysql.connect(host='localhost', user = 'root', password='111111', db = 'jinsol',charset = 'utf8')
    cursor = db.cursor(pymysql.cursors.DictCursor)
    logger.error('되는가?add함수')
    last_inserted_items = HotPlace.objects.last()
    logger.error(last_inserted_items)

    # HotPlace에 아무런데이터가없다면""로초기화를시켜주고, 그렇지않다면'naverPlaceID'를 가져옴
    if last_inserted_items is None:
        last_inserted_id = ""
    else:
        last_inserted_id = getattr(last_inserted_items, 'naverPlaceID')
    logger.error(last_inserted_id)
    items_to_insert_into_db = {}

    # 만약DB에 추가된 naverPlaceID와 동일한id를 가졌다면 db 값 UPDATE 작업 진행
    for item in crawled_items:
        if crawled_items['naverPlaceID'] == last_inserted_id:
            try:
                sql = 'UPDATE website_hotplace SET title = %s, category = %s, location = %s, businessHours = %s, description = %s, imageSrc = %s, menuName = %s WHERE naverPlaceID = %s'
                val = (crawled_items['title'],
                       crawled_items['category'], crawled_items['location'],
                       crawled_items['businessHours'], crawled_items['description'],
                       crawled_items['imageSrc'],crawled_items['menuName'], crawled_items['naverPlaceID'])
                cursor.execute(sql, val)
                db.commit()
                # menuNames =
                # sql= 'update website_hotplace set menuName = json_replace(menuName,'menuNames') where id = 1 ;'
                db.close()
                logger.error('되는가?db update 상태')
            except:
                print('error')
            return
        items_to_insert_into_db = crawled_items

    logger.error(last_inserted_id)
    logger.error('되는가?db 최신 상태')
    logger.error(items_to_insert_into_db)

    item_naverPlaceID = items_to_insert_into_db['naverPlaceID']
    item_title = items_to_insert_into_db['title']
    item_category = items_to_insert_into_db['category']
    item_location = items_to_insert_into_db['location']
    item_businessHours = items_to_insert_into_db['businessHours']
    item_description = items_to_insert_into_db['description']
    item_imageSrc = items_to_insert_into_db['imageSrc']

    # 만약DB에 추가된 naverPlaceID와 동일한id가 없다면 새로 INSERT
    sql2 = "INSERT INTO website_hotplace (naverPlaceID, title, category, location, businessHours, description, imageSrc) VALUES (%s, %s, %s, %s, %s, %s, %s);"
    val = (item_naverPlaceID, item_title, item_category, item_location, item_businessHours, item_description, item_imageSrc)
    cursor.execute(sql2, val)
    db.commit()
    db.close()
    logger.error('되는가?db input 상태')


def get_hotplace():
    hotplaces = Place.objects.order_by('-count')[:5]
    res = []
    for item in hotplaces:
        res.append(item.naver_place_id)
        place_detail_crawl(item.naver_place_id)
    return res


if __name__ == '__main__':
    pool = Pool(processes=4) #4개의 프로세스 동시에 작동
    logger.error('되는가?멀티')
    pool.map(place_detail_crawl,range(1,end,10)) #title_to_list라는 함수에 1 ~ end까지 10씩늘려가며 인자로 적용
