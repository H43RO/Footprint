from django import forms
from django.contrib.auth.forms import UserCreationForm
from website.models import User
import factory
from django.utils.translation import gettext_lazy as _
from django.contrib.auth import password_validation





class RegisterForm(UserCreationForm):
    password1 = forms.CharField(
        label="비밀번호",
        strip=False,
        widget=forms.PasswordInput(),
        help_text='8글자 이내의 비밀번호를 입력해주세요.',
        error_messages= {'max_length': _("비밀번호가 너무 깁니다. 15자 이하로 해주세요."),}
    )
    password2 = forms.CharField(
        label="비밀번호 확인",
        strip=False,
        widget=forms.PasswordInput(),
        help_text='비밀번호를 재입력해주세요.',
    )  
    class Meta:
        model = User
        fields = ['email', 'password1', 'password2', 'birth_date', 'nickname', 'age', 'gender']
        labels = {
            'email': _('이메일'),
            'birth_date': _('생년월일'),
            'nickname': _('닉네임'),
            'age': _('나이'),
            'gender': _('성별'),
        }
        help_texts = {
            'email': _('이메일을 입력해주세요'),
            'birth_date': _('생년월일을 입력해주세요  (YYYY-MM-DD)'),
            'nickname': _('닉네임을 입력해주세요(10자 이내)'),
            'age': _('나이를 입력해주세요'),
            'gender': _('성별을 입력해주세요'),
        }
    