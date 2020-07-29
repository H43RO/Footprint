from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render, get_object_or_404, redirect
from django.shortcuts import render, redirect
from django.contrib.auth import login, authenticate
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.db import transaction
from django.db.models import Count, Avg
from django.core.paginator import Paginator
from .forms import SignUpForm, PlaceRegisterForm, SignInForm
from .models import User, History, Place

from .backends import EmailAuthBackend

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
                print(user)
                login(request, user)
                return HttpResponseRedirect('../list')
        else:
            print(0)
    else :
        form = SignInForm()
    return render(request, 'signin.html', {'form': form})





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
    return render(request, 'history.html', context)


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
        return render(request,'place_search.html',{'place_search':place_search,'q':q})
    else:
        return render(request,'place_search.html')