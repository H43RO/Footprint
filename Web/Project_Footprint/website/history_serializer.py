from rest_framework import serializers
from .models import History, User
from django.utils import timezone, dateformat
from .user_serializers import UserLoginSerializer


class HistorySerializer(serializers.ModelSerializer):
    """
    History 조회, 생성, 삭제 Serializer
    'created_at','mood' field가 null일 경우, 해당 인스턴스에 자동으로 값을 넣어줌.
    """
    class Meta:
        model = History
        fields = '__all__'

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

    def to_representation(self, instance):
        data = super(HistorySerializer, self).to_representation(instance)
        if not data['created_at']:
            data['created_at'] = timezone.now()
            instance.created_at = timezone.now()
        if not data['mood']:
            data['mood'] = "기분 좋았던 순간"
            instance.mood = "기분 좋았던 순간"
        return data



class HistoryPutSerializer(serializers.ModelSerializer):
    """
    History 수정 Serializer
    'created_at','mood' field가 null일 경우, 해당 인스턴스에 자동으로 값을 넣어줌.
    """
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

