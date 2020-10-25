from .models import History
from rest_framework.generics import (
    ListCreateAPIView,
    UpdateAPIView,
    DestroyAPIView
)
from django_filters import FilterSet
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework.viewsets import ModelViewSet
from .serializers import HistorySerializer, HistoryPutSerializer, HistoryDateSerializer


class HistoryCreateViewSet(ListCreateAPIView):
    queryset = History.objects.all()
    serializer_class = HistorySerializer


class HistoryUpdateAPIView(UpdateAPIView):
    queryset = History.objects.all()
    serializer_class = HistoryPutSerializer
    lookup_field = 'id'


class HistoryDeleteAPIView(DestroyAPIView):
    queryset = History.objects.all()
    serializer_class = HistorySerializer
    lookup_field = 'id'

class HistoryFilter(FilterSet):
    class Meta:
        model = History
        fields = {
            'title': ['icontains'],
            'created_at': ['date', 'date__lte', 'date__gte'],
            'user': ['exact'],
        }


class HistoryViewSet(ModelViewSet):
    queryset = History.objects.all().order_by('created_at')
    serializer_class = HistorySerializer
    filterset_class = HistoryFilter
    filter_backends = [DjangoFilterBackend]