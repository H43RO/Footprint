from . import views
from django.urls import path



urlpatterns = [
    path('', views.index, name='index'),
    path('signup/', views.signup, name='signup'),
    path('signin/', views.signin, name='signin'),
    path('signout/', views.signout, name='signout'),
    path('myinfo/', views.myinfo, name='myinfo'),
    path('list/', views.list, name='list'),
    path('history/', views.history, name='history'),
    path('place_list/', views.place_list, name='place_list'),
    path('place_register/', views.place_register, name='place_register'),
    path('place_restaurant_list/', views.place_restaurant, name='place_restaurant_list'),
    path('place_sights_list/', views.place_sights, name='place_sights_list'),
    path('index/', views.index, name='index'),
    path('place_search/', views.place_search, name='place_search',),

]

