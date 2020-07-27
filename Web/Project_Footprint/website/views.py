from django.http import HttpResponse
from django.shortcuts import render, get_object_or_404, redirect
from .models import History, Place
from django.core.paginator import Paginator
from django.http import HttpResponseRedirect
from .forms import PlaceRegisterForm
from django.db.models import Count, Avg

# Create your views here.

def index(request):
    return HttpResponse('발자취')


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
        return HttpResponseRedirect('/website/place_list/')
    form = PlaceRegisterForm()
    return render(request, 'place_register.html', {'form': form})