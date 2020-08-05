from .backends import EmailAuthBackend
from django.http import HttpResponse, HttpResponseRedirect, request
from django.core.exceptions import ValidationError
from django.core.validators import validate_email
from django.contrib.sites.shortcuts import get_current_site
from django.utils.http import urlsafe_base64_decode, urlsafe_base64_encode
from django.core.mail import EmailMessage
from django.utils.encoding import force_bytes, force_text
from django.shortcuts import render, get_object_or_404, redirect
from django.contrib.auth import login, authenticate, logout
from django.contrib import messages, auth
from django.db import transaction
from django.db.models import Count, Avg
from django.core.paginator import Paginator
from .forms import SignUpForm, PlaceRegisterForm, SignInForm, HistoryForm, UpdateHistoryForm, UpdateUserInfoForm, \
    CheckPasswordForm
from .models import User, History, Place


def index(request):
    context = {
        'items': '발자취'
    }
    return render(request, 'index.html', context)


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
            print(0)
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

def myinfo(request):
    if request.user.is_authenticated:
        user_id = request.user.id
        context = {
            'users': User.objects.filter(id=user_id)
        }
        return render(request, 'myinfo.html', context)


def history(request):
    historys = History.objects.all()
    paginator = Paginator(historys, 5)  # 한 페이지에 5개씩 표시
    # page = request.GET.get('page')  # query params에서 page 데이터를 가져옴
    # items = paginator.get_page(page)  # 해당 페이지의 아이템으로 필터링
    place = Place.objects.all()
    context = {
        'historys': historys,
        'places' : place
    }
    return render(request, 'history_list.html', context)



def place_list(request):
    context = {
        'places': Place.objects.all()
    }
    return render(request, 'place_list.html', context)


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
    place_search = Place.objects.all().order_by('-id')
    q = request.POST.get('q', "")

    if q:
        place_search = place_search.filter(title__icontains=q)
        return render(request, 'place_search.html', {'place_search': place_search, 'q': q})
    else:
        return render(request,'place_search.html')

