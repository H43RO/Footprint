from django.forms import ModelForm
from django import forms
from django.contrib.auth.forms import UserCreationForm, AuthenticationForm, UserChangeForm
from .models import User, Place, History
from django.utils.translation import gettext_lazy as _
from django.contrib.auth import password_validation, get_user_model
from django.contrib.auth.hashers import check_password

MOOD_POINT_CHOICES = (
    ('angry', "angry"),
    ('soso', "soso"),
    ('happy', "happy"),
)


MOOD_POINT_CHOICES = (
    ('angry', "angry"),
    ('soso', "soso"),
    ('happy', "happy"),
)


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


class HistoryForm(forms.ModelForm):
    class Meta:
        model = History
        fields = ['title', 'mood', 'img', 'comment', 'place', 'user']
        labels = {
            'title': _('제목'),
            'mood':_('내 기분'),
            'img': _('사진'),
            'comment': _('코멘트'),
            'place': _('장소'),
            'user': _('사용자'),
        }
        widgets = {
            'mood': forms.Select(choices=MOOD_POINT_CHOICES),
        }
        help_texts = {
            'comment': _('일기를 작성해주세요.'),
        }


class UpdateHistoryForm(HistoryForm):
    class Meta:
        model = History
        exclude = [' ']


class UpdateUserInfoForm(UserChangeForm):
    password = None

    class Meta:
        model = get_user_model()
        fields = ['birth_date', 'nickname', 'age', 'gender']
        # exclude = ['password']
        # 검증을 위해 넣어놓은 것, 패스워드 틀리면 저장 안되게 하려고(이게 수정된 정보에 올라가서는 안됨)
        labels = {
            'birth_date': _('생년월일'),
            'nickname': _('닉네임'),
            'age': _('나이'),
            'gender': _('성별'),
        }


class CheckPasswordForm(forms.Form):
    password = forms.CharField(
        label='비밀번호', 
        widget=forms.PasswordInput(
            attrs={'class': 'form-control', }
        ),

    )

    def __init__(self, user, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.user = user

    def clean(self):
        cleaned_data = super().clean()
        password = cleaned_data.get('password')
        confirm_password = self.user.password

        if password:
            if not check_password(password, confirm_password):
                self.add_error('password', '비밀번호가 일치하지 않습니다.')

