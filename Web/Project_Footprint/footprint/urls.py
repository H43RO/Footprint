"""
footprint URL Configuration
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
from django.conf.urls import url
from django.contrib import admin
from django.urls import path, include
from rest_framework import routers
# from website import views
from accounts import views as acc_views
# from place import views as pla_views
# from history import views as his_views
# from post import views as pos_views
from django.urls import path, include
from django.conf.urls.static import static
from django.conf import settings
from rest_framework import routers

from accounts.viewsets import UserListView, UserUpdateView, UserDeleteView
from histories.viewsets import HistoryUpdateAPIView, HistoryDeleteAPIView, HistoryCreateViewSet, HistoryViewSet
from django_filters.views import FilterView
from places.viewsets import ApiPlaceId, HotPlcaeViewSet
from histories.viewsets import HistoryViewSet
from posts.viewsets import NoticeViewSet, EditorViewSet

from django.views.static import serve
from django.urls import re_path


router = routers.DefaultRouter()
router.register('histories', HistoryViewSet)
router.register('places', ApiPlaceId, basename='places')
router.register('noticelist', NoticeViewSet)
router.register('hotplaces', HotPlcaeViewSet, basename='hotplaces')
router.register('editorlist', EditorViewSet)
api_urlpatterns = [
    path('accounts/', include('rest_registration.api.urls')),
]

urlpatterns = [
    path('', include('accounts.urls')),
    path('', include('places.urls')),
    path('', include('histories.urls')),
    path('', include('posts.urls')),
    path('api/', include(router.urls)),
    path('admin/', admin.site.urls),
    path('api-auth/', include('rest_framework.urls', namespace='rest_framework')),
    path('api/v1/', include(api_urlpatterns)),
    url('api/', include(router.urls)),
    url('userinfo/(?P<id>[\w-]+)/update/$', UserUpdateView.as_view(), name='user_update'),
    url('userinfo/(?P<id>[\w-]+)/delete/$', UserDeleteView.as_view(), name='user_delete'),
    url('api/histories/(?P<id>[\w-]+)/edit/$', HistoryUpdateAPIView.as_view(), name='update'),
    url('api/histories/(?P<id>[\w-]+)/delete/$', HistoryDeleteAPIView.as_view(), name='delete'),
    path('grappelli/', include('grappelli.urls')),  # grappelli URLS
    path('accounts/', include('django.contrib.auth.urls')),
    path('ckeditor/', include('ckeditor_uploader.urls')),
    url('api/histories/create/$', HistoryCreateViewSet.as_view(), name='history_create'),
    re_path(r'^media/(?P<path>.*)$', serve, {'document_root':settings.MEDIA_ROOT}),
]+ static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)


if settings.DEBUG:
    import debug_toolbar
    urlpatterns += [
        url(r'^__debug__/', include(debug_toolbar.urls)),
    ]