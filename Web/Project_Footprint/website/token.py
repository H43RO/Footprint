import six
from django.contrib.auth.tokens import PasswordResetTokenGenerator
from django.shortcuts import redirect


class AccountActivationTokenGenerator(PasswordResetTokenGenerator):
    def _make_has_value(self, user, timestamp):
        return (six.text_type(user.pk) + six.text_type(timestamp)) + six.text_type(user.is_active)


def message(domain, uidb64, token):
    return f"아래 링크를 클릭하면 회원가입 인증이 완료됩니다. \n\n회원가입 링크 : http://{domain}/activate/{uidb64}/{token}\n\n감사합니다."


account_activation_token = AccountActivationTokenGenerator()
