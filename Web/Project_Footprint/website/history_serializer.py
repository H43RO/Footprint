from rest_framework import serializers
from .models import History, Place

class HistorySerializer(serializers.ModelSerializer):
    class Meta:
        model = History
        fields = ('place', 'created_at')