from rest_framework import serializers
from .models import History
from django.utils import timezone, dateformat


class HistorySerializer(serializers.ModelSerializer):
    class Meta:
        model = History
        fields = '__all__'

    def to_representation(self, instance):
        data = super(HistorySerializer, self).to_representation(instance)
        if not data['created_at']:
            data['created_at'] = timezone.now()
            instance.created_at = timezone.now()
        if not data['mood']:
            data['mood'] = "기분 좋았던 순간"
            instance.mood = "기분 좋았던 순간"
        instance.save()
        return data




class HistoryPutSerializer(serializers.ModelSerializer):
    class Meta:
        model = History
        fields = ('img','title','mood','comment','place', 'custom_place', 'created_at')

    def to_representation(self, instance):
        data = super(HistoryPutSerializer, self).to_representation(instance)
        if not data['created_at']:
            data['created_at'] = timezone.now()
            instance.created_at = timezone.now()
        if not data['mood']:
            data['mood'] = "기분 좋았던 순간"
            instance.mood = "기분 좋았던 순간"
        instance.save()
        return data

