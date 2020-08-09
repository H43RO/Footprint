from .models import User, Place, History, Notice
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


class UserListView(viewsets.ModelViewSet):
    queryset = User.objects.all()
    serializer_class = UserListSerializer
    filter_backends = (filters.DjangoFilterBackend,)
    filter_fields = ('id',)




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


class ApiPlaceList(viewsets.ModelViewSet):
    queryset = Place.objects.all()
    serializer_class = PlaceIdSerializer


class ApiPlaceId(APIView):
    queryset = Place.objects.all()
    serializer_class = PlaceIdSerializer

    def get_object(self, pk):
        try:
            return Place.objects.get(beacon_uuid=pk)
        except Place.DoesNotExist:
            raise Http404

    def get(self, request, pk):
        places = self.get_object(pk)
        serializer = PlaceIdSerializer(places)
        return Response(serializer.data)


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
    queryset = Notice.objects.all()
    serializer_class = NoticeSerializer
