from .models import User
from django.contrib import auth
from django.utils.translation import gettext as _
from rest_framework import serializers
from rest_framework.response import Response
from rest_registration.settings import registration_settings
from django.contrib.auth import authenticate
from rest_framework.response import Response
from django.contrib import messages

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

def build_default_success_response(message, status, extra_data):
    """
    django-rest-registration에 사용되는 함수 오버라이드
    """
    data = message
    if extra_data:
        data.update(extra_data)
        return Response(data, status=status)

    if user.is_active is False:
        raise BadRequest(_("This user is not activated."))