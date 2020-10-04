from .models import User
from django.contrib import auth
from django.utils.translation import gettext as _
from rest_framework import serializers
from rest_framework.response import Response
from rest_registration.settings import registration_settings
from django.contrib.auth import authenticate
from django.contrib import messages
from rest_registration.utils.responses import get_ok_response

class UserLoginSerializer(serializers.Serializer):
    """
    django-rest-registration에 사용되는 Serializer 오버라이드
    """
    email = serializers.EmailField()
    password = serializers.CharField()
    def get_authenticated_user(self):
        email = self.data.get('email')
        password = self.data.get('password')
        user = authenticate(username=email, password=password)
        return user
    class Meta:
        model = User
        fields = ("email")

class UserListSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('email', 'nickname', 'birth_date', 'age', 'gender')


class UserUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('nickname', 'birth_date', 'age', 'gender')



def build_default_success_response(message, status, extra_data):
    """
    django-rest-registration에 사용되는 함수 오버라이드
    """
    data = message
    if extra_data:
        data.update(extra_data)
        return Response(data, status=status)

    if extra_data.user.is_active is False:
        raise Response(_("This user is not activated."), status=400)

