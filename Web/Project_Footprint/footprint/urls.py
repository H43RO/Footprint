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
from django.urls import path
from rest_framework import routers
from website import views
from django.urls import path, include
from django.conf.urls.static import static
from django.conf import settings
from django.conf.urls import url
from rest_framework import routers

from website import views
from django_filters.views import FilterView

from website.views import HistoryViewSet, UserUpdateView, UserDeleteView

router = routers.DefaultRouter()
router.register('historys',HistoryViewSet)
router.register('places', views.ApiPlaceId)
router.register('userinfo', views.UserListView)

urlpatterns = [
    path('', include('website.urls')),
    path('api/', include(router.urls)),
    path('admin/', admin.site.urls),
    url('api/', include(router.urls)),
    url('api-auth/', include('rest_framework.urls', namespace='rest_framework')),
    path('api-auth/', include('rest_framework.urls', namespace='rest_framework')),
    url(r'^userinfo/(?P<id>[\w-]+)/update/$', UserUpdateView.as_view(), name='user_update'),
    url(r'^userinfo/(?P<id>[\w-]+)/delete/$', UserDeleteView.as_view(), name='user_delete'),
]+ static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

