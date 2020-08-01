"""footprint URL Configuration
The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.conf.urls import url, include
from django.contrib import admin
from django.urls import path, include
from rest_framework import routers
from website import views
from django.conf.urls.static import static
from django.conf import settings
from rest_framework import routers
from website.views import HistoryViewSet
from website.views import (
    HistoryViewSet,
    HistoryUpdateAPIView,
    HistoryDeleteAPIView,
    )
from website import views
from django_filters.views import FilterView

router = routers.DefaultRouter()
router.register('historys',HistoryViewSet)
router.register('places', views.ApiPlaceId)
router.register('userinfo', views.UserListView, basename='userinfo')
router.register('userinfo', views.UserListView)

urlpatterns = [
    path('', include('website.urls')),
    path('api/', include(router.urls)),
    path('admin/', admin.site.urls),
    path('api-auth/', include('rest_framework.urls', namespace='rest_framework')),
    url(r'^historys/(?P<id>[\w-]+)/edit/$', HistoryUpdateAPIView.as_view(), name='update'),
    url(r'^historys/(?P<id>[\w-]+)/delete/$', HistoryDeleteAPIView.as_view(), name='delete'),
]+ static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

