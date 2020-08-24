from rest_framework import serializers
from .models import History
from django.utils import timezone, dateformat


class HistorySerializer(serializers.ModelSerializer):
    def to_representation(self, instance):
        data = super(HistorySerializer, self).to_representation(instance)
        if not data['created_at']:
            data['created_at'] = timezone.now()
        if not data['mood']:
            data['mood'] = "기분 좋았던 순간"
        return data

    class Meta:
        model = History
        fields = '__all__'




class HistoryPutSerializer(serializers.ModelSerializer):
    class Meta:
        model = History
        fields = ('img','title','mood','comment','place', 'custom_place')

