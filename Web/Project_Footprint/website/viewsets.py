from .models import User, Place, History
from .place_info_serializers import PlaceSerializer
from .history_date_serializer import HistoryDateSerializer
from .history_serializer import HistorySerializer
from .user_info_serializer import UserListSerializer, UserUpdateSerializer
from .user_serializers import UserLoginSerializer
from .place_id_serializers import PlaceIdSerializer
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
    DestroyAPIView
)


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


class PlaceTitleFilter(filters.FilterSet):
    class Meta:
        model = Place
        fields = {
            'title': ['icontains'],
        }


class ApiPlaceId(viewsets.ModelViewSet):
    queryset = Place.objects.all()
    serializer_class = PlaceIdSerializer
    filter_backends = (filters.DjangoFilterBackend,)
    filter_fields = ('beacon_uuid', 'naver_place_id')


class ApiPlaceTitle(viewsets.ModelViewSet):
    queryset = Place.objects.all()
    serializer_class = PlaceSerializer
    filter_backends = [filters.DjangoFilterBackend]
    filterset_class = PlaceTitleFilter


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


class HistoryFilter(FilterSet):
    title = CharFilter(lookup_expr='icontains')

    class Meta:
        model = History
        fields = ('title', 'created_at')


class HistoryDateFilter(filters.FilterSet):
    class Meta:
        model = History
        fields = {
            'title': ['icontains'],
            'created_at': ['date', 'date__lte', 'date__gte']
        }


class HistoryDateViewSet(viewsets.ModelViewSet):
    queryset = History.objects.all()
    serializer_class = HistorySerializer
    filterset_class = HistoryDateFilter
    filter_backends = [filters.DjangoFilterBackend]

    # filter_fields = ['title', 'created_at']

    @action(methods=['get'], detail=False)
    def newest(self, request):
        newest = self.get_queryset().order_by('created_at').last()
        serializer = self.get_serializer_class()(newest)
        return Response(serializer.data)
