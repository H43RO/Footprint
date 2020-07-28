from django.http import HttpResponse, HttpResponseRedirect
from django.contrib.auth import login, authenticate
from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from django.db import transaction
from django.contrib import messages
from .forms import CustomUserCreationForm, RegisterForm
from .models import User


# Create your views here.

def index(request):
    return HttpResponse('발자취')


def list(request):
    user = User.objects.all()
    context = {
        'users': user
    }
    return render(request, 'list.html', context)


def register(request):
    if request.method == 'POST':
        form = RegisterForm(request.POST)
        if form.is_valid():
            form.save()
            email = form.cleaned_data.get('email')
            raw_password = form.cleaned_data.get('password1')
            user = authenticate(email=email, password=raw_password)
            login(request, user)
        return HttpResponseRedirect('/website/list/')
    form = RegisterForm()
    return render(request, 'register.html', {'form': form})
