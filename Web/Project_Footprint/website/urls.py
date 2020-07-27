from django.conf.urls import url
from django.urls import path


from website import views
urlpatterns = [
    path('signup/', views.signup, name="signup"),
    path('list/', views.list, name="list")
]