from django.contrib.auth.backends import ModelBackend
from .models import User


class EmailAuthBackend(object):
    """
    커스텀 이메일 백엔드
    유저 인증 시, email field를 username 대신 사용
    """

    def authenticate(self, request, username, password):
        try:
            user = User.objects.get(email=username)
            if user.check_password(password):  # check valid password
                return user  # return user to be authenticated
        except User.DoesNotExist:  # no matching user exists
            return None

    def get_user(self, user_id):
        try:
            return User.objects.get(pk=user_id)
        except:
            return None