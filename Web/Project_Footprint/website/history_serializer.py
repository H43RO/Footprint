from rest_framework import serializers
from .models import History, Place
from django_filters import FilterSet, CharFilter, DateFromToRangeFilter


class HistorySerializer(serializers.ModelSerializer):
    class Meta:
        model = History
        fields = '__all__'


class HistoryDateSerializer(serializers.ModelSerializer):
    create_date__name = CharFilter(lookup_expr='icontains')

    class Meta:
        model = History
        fields = '__all__'
