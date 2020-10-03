from django.urls import path
from . import views
from . import viewsets

urlpatterns = [
    path('history/', views.history, name='history'),
    path('history/update/',views.history_update,name='history-update'),
    path('history/create/',views.history_create,name='history-create'),
    path('history/<int:id>/delete/', views.history_delete,name='history-delete'),
]