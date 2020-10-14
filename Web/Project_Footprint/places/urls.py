from django.urls import path
from . import views


urlpatterns = [
    path('', views.index, name='index'),
    path('index/', views.index, name='index'),
    path('place/<int:id>', views.place_detail, name="place-detail"),
]


