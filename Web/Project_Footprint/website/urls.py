from django.urls import path
from . import views


urlpatterns = [
    path('', views.index, name='index'),
    path('signup/', views.signup, name='signup'),
    path('list/', views.list, name='list'),
    path('history/', views.history, name='history'),
    path('place_list/', views.place_list, name='place_list'),
    path('place_register/', views.place_register, name='place_register')
]

