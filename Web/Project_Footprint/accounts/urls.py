from django.urls import path
from . import views
from . import viewsets
from django.contrib.auth import views as auth_views
from django.views.generic import TemplateView

urlpatterns = [
    path('signin/', views.signin, name='signin'),
    path('signup/', views.signup, name='signup'),
    path('signout/', views.signout, name='signout'),
    path('activate/<str:uidb64>/<str:token>', views.user_activate, name='user_activate'),
    path('user_info_update/', views.user_info_update, name='user_info_update'),
    path('user_delete/', views.user_delete, name='user_delete'),
    path('user_password_update/', views.user_password_update, name='user_password_update'),
    path('myinfo/', views.myinfo, name='myinfo'),
    path('user_password_find/', views.user_password_find, name='user_password_find'),
    path('password_reset/done/', auth_views.PasswordResetDoneView.as_view(template_name='password_email_confirm.html'), name='password_email_confirm'),
    path('reset/<uidb64>/<token>/', auth_views.PasswordResetConfirmView.as_view(template_name='password_reset.html'), name='password_reset'),
    path('accounts/reset/done/', auth_views.PasswordResetCompleteView.as_view(template_name='password_reset_complete.html'), name='password_reset_complete'),
    
    path('api_activate/', views.api_user_activate, name='api_user_activate'),
    path('api_password/', views.api_password_reset, name='api_password_reset'),

    path('signup_email_confirm/', TemplateView.as_view(template_name="signup_email_confirm.html"), name='signup_email_confirm'),
    path('accounts/kakao/login', views.kakao_login, name='kakao_login'),
    path('accounts/kakao/logout', views.kakao_logout, name='kakao_logout'),
    path('accounts/kakao/login/callback/', views.kakao_callback, name='kakao_callback'),

]