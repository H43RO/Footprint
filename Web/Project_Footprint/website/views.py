from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render
from django.contrib.auth import login, authenticate
from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from django.db import transaction
from django.contrib import messages
from .forms import SignUpForm
from .models import User


# Create your views here.


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
            email = form.cleaned_data.get('email')
            raw_password = form.cleaned_data.get('password1')
            user = authenticate(email=email, password=raw_password)
            if user is not None:
                login(request, user, backend='Django.contrib.auth.backends.ModelBackend')
                return HttpResponseRedirect('../list/')
    else:
        form = SignUpForm()
    return render(request, 'signup.html', {'form': form})
