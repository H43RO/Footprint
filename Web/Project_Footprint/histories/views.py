from django.shortcuts import render, redirect, get_object_or_404
from .models import History
from .forms import HistoryForm, UpdateHistoryForm
from django.http import HttpResponse, HttpResponseRedirect, request
from django.utils.translation import gettext_lazy as _
from django.utils import timezone, dateformat
from django.contrib import messages

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