from rest_framework import serializers
from .models import User


class UserListSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('email', 'nickname', 'birth_date', 'age', 'gender')


class UserUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('nickname', 'birth_date', 'age', 'gender')

