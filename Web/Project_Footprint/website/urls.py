from django.urls import path
from django.conf.urls import url
from . import views
from . import viewsets

urlpatterns = [
    path('', views.index, name='index'),
    path('signup/', views.signup, name='signup'),
    path('signin/', views.signin, name='signin'),
    path('signout/', views.signout, name='signout'),
    path('activate/<str:uidb64>/<str:token>', views.user_activate, name='user_activate'),
    path('api_activate/', views.api_user_activate, name='api_user_activate'),
    path('list/', views.list, name='list'),
    path('history/', views.history, name='history'),
    path('place_list/', views.place_list, name='place_list'),
    path('place_register/', views.place_register, name='place_register'),
    path('place_restaurant_list/', views.place_restaurant, name='place_restaurant_list'),
    path('place_sights_list/', views.place_sights, name='place_sights_list'),
    path('index/', views.index, name='index'),
    path('place_search/',views.place_search,name='place_search'),
    path('history/update/',views.history_update,name='history-update'),
    path('history/create/',views.history_create,name='history-create'),
    path('history/<int:id>/delete/', views.history_delete,name='history-delete'),
    path('user_info_update/', views.user_info_update, name='user_info_update'),
    path('user_delete/', views.user_delete, name='user_delete'),
    path('myinfo/', views.myinfo, name='myinfo'),
    path('user_password_update/', views.user_password_update, name='user_password_update'),
    path('user_password_auth/', views.user_password_auth, name='user_password_auth'),
    path('user_password_confirm/', views.user_password_confirm, name='user_password_confirm'),
    path('user_password_reset/', views.user_password_reset, name='user_password_reset'),
    path('email_activate/<str:uidb64>/<str:token>', views.email_activate, name='email_activate'),
]
