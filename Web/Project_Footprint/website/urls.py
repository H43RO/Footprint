from django.urls import path
from . import views
from . import viewsets
from django.contrib.auth import views as auth_views

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
    # path('Hotplace_create/', views.Hotplace_create, name='Hotplace_create'),
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
    path('api_password/', views.api_password_reset, name='api_password_reset'),

    path('user_password_find/', views.user_password_find, name='user_password_find'),
    path('password_reset/done/', auth_views.PasswordResetDoneView.as_view(template_name='password_email_confirm.html'), name='password_email_confirm'),
    path('reset/<uidb64>/<token>/', auth_views.PasswordResetConfirmView.as_view(template_name='password_reset.html'), name='password_reset'),
    path('accounts/reset/done/', auth_views.PasswordResetCompleteView.as_view(template_name='password_reset_complete.html'), name='password_reset_complete'),
    path('notice/', views.noticelist, name='notice'),
    path('notice/notice_view/<int:id>', views.noticeview, name='notice_view'),
    path('place/<int:id>', views.place_detail, name="place-detail"),
    path('notice/', views.noticelist, name='notice'),
    path('editor/', views.editor, name='editor'),
    path('editor/editor_view/<int:id>', views.editorview, name='editor_view'),
]
