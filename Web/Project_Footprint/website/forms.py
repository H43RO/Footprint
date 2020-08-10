from django.forms import ModelForm
from django import forms
from django.contrib.auth.forms import UserCreationForm, AuthenticationForm, UserChangeForm, PasswordChangeForm, \
    SetPasswordForm
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


class UserPasswordUpdateForm(PasswordChangeForm):
    """
    A form that lets a user change their password by entering their old
    password.
    """
    old_password = forms.CharField(
        label=_("현재 비밀번호"),
        strip=False,
        widget=forms.PasswordInput(attrs={'autocomplete': 'current-password', 'autofocus': True}),
        error_messages={
            **SetPasswordForm.error_messages,
            'password_incorrect': _("Your old password was entered incorrectly. Please enter it again."),
        }
    )
    new_password1 = forms.CharField(
        label=_("변경할 비밀번호"),
        widget=forms.PasswordInput(attrs={'autocomplete': 'new-password'}),
        strip=False,
    )
    new_password2 = forms.CharField(
        label=_("변경할 비밀번호 재입력"),
        widget=forms.PasswordInput(attrs={'autocomplete': 'new-password'}),
        strip=False,
        error_messages={
            'password_mismatch': _('The two password fields didn’t match.'),
        }
    )
    field_order = ['old_password', 'new_password1', 'new_password2']

    class Meta:
        model = User
        fields = ['old_password', 'new_password1', 'new_password2']


class UserPasswordResetForm(SetPasswordForm):
    """
    A form that lets a user change set his/her password without
    entering the old password
    """
    new_password1 = forms.CharField(label=_("변경할 비밀번호"), widget=forms.PasswordInput)
    new_password2 = forms.CharField(label=_("변경할 비밀번호 재입력"), widget=forms.PasswordInput)

    # def __init__(self, user, *args, **kwargs):
    #     self.user = user
    #     super(SetPasswordForm, self).__init__(*args, **kwargs)

    def clean_new_password2(self):
        password1 = self.cleaned_data.get('new_password1')
        password2 = self.cleaned_data.get('new_password2')
        if password1 and password2:
            if password1 != password2:
                raise forms.ValidationError(_("The two password fields didn't match."))
        return password2

    def save(self, commit=True):
        self.user.set_password(self.cleaned_data['new_password1'])
        if commit:
            self.user.save()
        return self.user


class UserPasswordAuthForm(forms.Form):
    email = forms.EmailField(
        label=_("가입 시 사용한 이메일을 입력해주세요."),
        widget=forms.EmailInput,
    )

    class Meta:
        model = User
        fields = ['email']
