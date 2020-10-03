from django.shortcuts import render
from .models import Post
from django.http import request

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
