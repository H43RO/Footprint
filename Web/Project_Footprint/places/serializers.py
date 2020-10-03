from rest_framework import serializers
from .models import Place, HotPlace

class PlaceSerializer(serializers.ModelSerializer):
    class Meta:
        model = Place
        fields = ['naver_place_id']

class HotplaceSerializers(serializers.ModelSerializer):
    class Meta:
        model = HotPlace
        fields = '__all__'