from django.contrib.auth.backends import ModelBackend
from .models import User


class EmailAuthBackend(object):
    """
    Custom Email Backend to perform authentication via email
    """

    def authenticate(self, request, email, password):
        try:
            user = User.objects.get(email=email)
            if user.check_password(password):  # check valid password
                return user  # return user to be authenticated
        except User.DoesNotExist:  # no matching user exists
            return None

    def get_user(self, user_id):
        try:
            return User.objects.get(pk=user_id)
        except:
            return None