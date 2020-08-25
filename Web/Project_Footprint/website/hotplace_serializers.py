from rest_framework import serializers
from .models import HotPlace


class HotplaceSerializers(serializers.ModelSerializer):
    class Meta:
        model = HotPlace
        fields = '__all__'