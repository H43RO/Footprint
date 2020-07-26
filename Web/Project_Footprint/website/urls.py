from django.urls import path

from . import views

urlpatterns = [
    path('history/', views.history, name='history'),
]