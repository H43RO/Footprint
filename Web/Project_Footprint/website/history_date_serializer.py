from rest_framework import serializers
from .models import History, Place
from django_filters import FilterSet, CharFilter, DateFromToRangeFilter


class HistoryDateSerializer(serializers.ModelSerializer):
    class Meta:
        model = History
        fields = '__all__'

