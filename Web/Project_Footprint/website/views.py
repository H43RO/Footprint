from django.http import HttpResponse
from django.shortcuts import render
from .models import Place

# Create your views here.

def index(request):
    return HttpResponse('발자취')


def place_list(request):
    context = {
        'places': Place.objects.all()
    }
    return render(request, 'place_list.html', context)