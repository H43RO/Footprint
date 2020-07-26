from django.http import HttpResponse
from django.shortcuts import render, get_object_or_404, redirect
from website.models import History, Place
from django.core.paginator import Paginator
from django.http import HttpResponseRedirect
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
