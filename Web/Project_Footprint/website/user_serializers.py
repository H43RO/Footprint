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



# def get_ok_response(message, status=200, extra_data=None):
#     builder = registration_settings.SUCCESS_RESPONSE_BUILDER
#     return builder(message=message, status=status, extra_data=extra_data)

from rest_registration.api.views import login
def build_default_success_response(message, status, extra_data):
    data = message
    if extra_data:
        data.update(extra_data)
    return Response(data, status=status)

    if user.is_active is False:
        raise BadRequest(_("This user is not activated."))