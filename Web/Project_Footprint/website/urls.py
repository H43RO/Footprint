from django.urls import path
from . import views
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
    path('myinfo/', views.myinfo, name='myinfo')
]
