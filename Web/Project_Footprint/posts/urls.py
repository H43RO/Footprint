from django.urls import path
from . import views
from . import viewsets

urlpatterns = [
    path('notice/', views.notice_list, name='notice'),
    path('notice/notice_view/<int:id>', views.noticeview, name='notice_view'),
    path('notice/', views.notice_list, name='notice'),
    path('editor/', views.editor_list, name='editor'),
    path('editor/editor_view/<int:id>', views.editorview, name='editor_view'),
]