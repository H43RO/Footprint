from rest_framework import serializers
from .models import History

class HistorySerializer(serializers.ModelSerializer):
    class Meta:
        model = History
        fields = '__all__'

class HistoryPutSerializer(serializers.ModelSerializer):
    class Meta:
        model = History
        fields = ('img','title','mood','comment','place', 'custom_place')

