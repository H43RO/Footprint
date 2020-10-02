from django.forms import ModelForm
from django import forms
from django.contrib.auth.forms import (
    UserCreationForm, 
    AuthenticationForm, 
    UserChangeForm, 
    PasswordChangeForm, 
    SetPasswordForm 
)
from .models import User
from django.utils.translation import gettext_lazy as _
from django.contrib.auth import password_validation, get_user_model
from django.contrib.auth.hashers import check_password






class SignUpForm(UserCreationForm):
    password1 = forms.CharField(
        label="비밀번호",
        strip=False,
        widget=forms.PasswordInput(render_value=True),
        help_text="8~25글자 사이의 비밀번호를 입력해주세요",
    )
    password2 = forms.CharField(
        label="비밀번호 확인",
        strip=False,
        widget=forms.PasswordInput(render_value=True),
        help_text='비밀번호를 재입력해주세요',
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
            'birth_date': _('생년월일을 입력해주세요 (YYYY-MM-DD)'),
            'nickname': _('10자 이내의 닉네임을 입력해주세요'),
            'age': _('나이를 입력해주세요'),
            'gender': _('성별을 입력해주세요'),
        }

class SignInForm(forms.Form):
    email = forms.EmailField(
        label=_("이메일"),
        widget=forms.EmailInput()
    )
    password = forms.CharField(
        label=_("비밀번호"),
        strip=False,
        widget=forms.PasswordInput(attrs={'autocomplete': 'current-password'}, render_value=True),
    )

    def __init__(self, request=None, *args, **kwargs):
        self.request = request
        self.user_cache = None
        super().__init__(*args, **kwargs)
        self.fields['email'].widget = forms.EmailInput(attrs={'placeholder': '이메일'})
        self.fields['email'].widget.attrs['class'] = 'form-control'
        self.fields['password'].widget = forms.PasswordInput(attrs={'placeholder': '비밀번호'})
        self.fields['password'].widget.attrs['class'] = 'form-control'
        
    def clean(self):
        return self.cleaned_data

    def get_user(self):
        return self.user_cache


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
    PasswordChangeForm 을 상속해서 사용해면 변경할 비밀번호 뿐만 아니라
    현재 비밀번호도 입력 받을 수 있으므로 보안을 더 강화할 수 있음
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


class ApiPasswordResetForm(forms.Form):
    new_password1 = forms.CharField(label=_("변경할 비밀번호"), widget=forms.PasswordInput, max_length=25)
    new_password2 = forms.CharField(label=_("변경할 비밀번호 재입력"), widget=forms.PasswordInput, max_length=25)

    def clean_new_password2(self):
        password1 = self.cleaned_data.get('new_password1')
        password2 = self.cleaned_data.get('new_password2')
        if password1 and password2:
            if password1 != password2:
                raise forms.ValidationError(_("The two password fields didn't match."))
        return password2    

    class Meta:
        model = User
        fields = ['password2']