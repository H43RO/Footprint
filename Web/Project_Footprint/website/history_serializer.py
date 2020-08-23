from rest_framework import serializers
from .models import History
from django.utils import timezone, dateformat

class HistorySerializer(serializers.ModelSerializer):
    def to_representation(self, instance):
        data = super().to_representation(instance)
        if not data['created_at']:
            formatted_date = dateformat.format(timezone.now(), 'Y-m-d H:i:s')
            data['created_at'] = formatted_date
        if not data['mood']:
            data['mood'] = 0
        return data

    class Meta:
        model = History
        fields = '__all__'



class HistoryPutSerializer(serializers.ModelSerializer):
    class Meta:
        model = History
        fields = ('img','title','mood','comment','place', 'custom_place')

