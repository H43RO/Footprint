from .models import User, Place, History, Post
from .place_info_serializers import PlaceSerializer
from .history_serializer import HistorySerializer
from .user_info_serializer import UserListSerializer, UserUpdateSerializer
from .user_serializers import UserLoginSerializer
from .place_id_serializers import PlaceIdSerializer
from .notice_serializers import NoticeSerializer
from rest_framework import viewsets, permissions, generics, status, mixins
from rest_framework.response import Response
from rest_framework.viewsets import ModelViewSet, ReadOnlyModelViewSet
from rest_framework.views import APIView
from rest_framework.decorators import api_view
from django_filters import rest_framework as filters
from rest_framework.viewsets import ModelViewSet, ReadOnlyModelViewSet
from .backends import EmailAuthBackend
from .token import account_activation_token, message
from django.utils.translation import gettext_lazy as _
from django_filters import FilterSet, CharFilter, NumberFilter, DateFilter
from rest_framework.decorators import action
from django.contrib.auth.decorators import login_required
from rest_framework.generics import (
    ListAPIView,
    UpdateAPIView,
    RetrieveUpdateAPIView,
    DestroyAPIView,
    CreateAPIView,
)
from django.http import Http404


class HistoryViewSet(viewsets.ModelViewSet):
    queryset = History.objects.all()
    serializer_class = HistorySerializer


class HistoryUpdateAPIView(UpdateAPIView):
    queryset = History.objects.all()
    serializer_class = HistorySerializer
    lookup_field = 'id'


class HistoryDeleteAPIView(DestroyAPIView):
    queryset = History.objects.all()
    serializer_class = HistorySerializer
    lookup_field = 'id'


class PlaceFilter(filters.FilterSet):
    class Meta:
        model = Place
        fields = {
            'title': ['icontains'],
            'beacon_uuid': ['exact'],
        }


class ApiPlaceId(viewsets.ModelViewSet):
    queryset = Place.objects.all()
    serializer_class = PlaceSerializer
    filter_backends = [filters.DjangoFilterBackend]
    filterset_class = PlaceFilter


class UserListView(viewsets.ModelViewSet):
    queryset = User.objects.all()
    serializer_class = UserListSerializer
    filter_backends = (filters.DjangoFilterBackend,)
    filter_fields = ('id',)


class UserUpdateView(UpdateAPIView):
    queryset = User.objects.all()
    serializer_class = UserUpdateSerializer
    lookup_field = 'id'


class UserDeleteView(DestroyAPIView):
    queryset = User.objects.all()
    serializer_class = UserListSerializer
    lookup_field = 'id'


class HistoryFilter(filters.FilterSet):
    class Meta:
        model = History
        fields = {
            'title': ['icontains'],
            'created_at': ['date', 'date__lte', 'date__gte'],
            'user': ['exact'],
        }


class HistoryViewSet(viewsets.ModelViewSet):
    queryset = History.objects.all()
    serializer_class = HistorySerializer
    filterset_class = HistoryFilter
    filter_backends = [filters.DjangoFilterBackend]

    # filter_fields = ['title', 'created_at']

    @action(methods=['get'], detail=False)
    def newest(self, request):
        newest = self.get_queryset().order_by('created_at').last()
        serializer = self.get_serializer_class()(newest)
        return Response(serializer.data)


class NoticeViewSet(viewsets.ModelViewSet):
    queryset = Post.objects.filter(post_div=1)
    serializer_class = NoticeSerializer


class ApiHotPlace(viewsets.ModelViewSet):
    queryset = Place.objects.order_by('-count')[:5]
    serializer_class = PlaceSerializer
