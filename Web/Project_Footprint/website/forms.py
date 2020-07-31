from django.forms import ModelForm
from django import forms
from django.contrib.auth.forms import UserCreationForm, AuthenticationForm
from .models import User, Place
from django.utils.translation import gettext_lazy as _
from django.contrib.auth import password_validation


class SignUpForm(UserCreationForm):
    password1 = forms.CharField(
        label="비밀번호",
        strip=False,
        widget=forms.PasswordInput,
        help_text='8~25글자 사이의 비밀번호를 입력해주세요.',
        error_messages={'max_length': _("비밀번호가 너무 깁니다. 8자 이하로 해주세요."), }
    )
    password2 = forms.CharField(
        label="비밀번호 확인",
        strip=False,
        widget=forms.PasswordInput,
        help_text='비밀번호를 재입력해주세요.',
    )

    class Meta:
        model = User
        fields = ['email', 'password1', 'password2', 'nickname', 'birth_date', 'age', 'gender']
        labels = {
            'email': _('이메일'),
            'birth_date': _('생년월일'),
            'nickname': _('닉네임'),
            'age': _('나이'),
            'gender': _('성별'),
        }
        help_texts = {
            'email': _('이메일을 입력해주세요'),
            'birth_date': _('생년월일을 입력해주세요 (Format : YYYY-MM-DD)'),
            'nickname': _('10자 이내의 닉네임을 입력해주세요'),
            'age': _('나이를 입력해주세요'),
            'gender': _('성별을 입력해주세요'),
        }


class SignInForm(AuthenticationForm):
    username = forms.EmailField(
        label=_("이메일"),
        widget=forms.EmailInput,
    )

    password = forms.CharField(
        label=_("비밀번호"),
        strip=False,
        widget=forms.PasswordInput
    )

    class Meta:
        model = User
        fields = ['email', 'password']


class PlaceRegisterForm(ModelForm):
    class Meta:
        model = Place
        fields = ['title', 'naver_place_id', 'place_div']
        labels = {
            'title': _('가게명 '),
            'naver_place_id': _('Naver Place Id '),
            'place_div': _('장소 구분 ')
        }
        help_texts = {
            'title': _('가게명을 입력해주세요.'),
            'naver_place_id': _('Naver Place Id를 입력해주세요.'),
            'place_div': _('해당하는 숫자를 입력해주세요.(관광지: 0, 식당: 1)')
        }
        error_messages = {
            'title': {
                'max_length': _('가게명을 30자 이내로 적어주세요')
            },
        }

