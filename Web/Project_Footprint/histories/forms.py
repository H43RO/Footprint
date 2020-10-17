from django import forms
from .models import History
from django.utils.translation import gettext_lazy as _
from django.core.exceptions import ValidationError
from django.contrib.admin import widgets
from functools import partial

DateInput = partial(forms.DateInput, {'class': 'datepicker'})
MOOD_POINT_CHOICES = (
    ('기분 좋았던 순간', "기분 좋았던 순간"),
    ('기뻤던 순간', "기뻤던 순간"),
    ('평화로웠던 순간', "평화로웠던 순간"),
    ('황홀했던 순간', "황홀했던 순간"),
    ('행복했던 순간', "행복했던 순간"),
    ('뭉클했던 순간', "뭉클했던 순간"),
    ('우울했던 순간', "우울했던 순간"),
    ('당황했던 순간', "당황했던 순간"),
    ('화났던 순간', "화났던 순간"),
    ('아쉬웠던 순간', "아쉬웠던 순간"),
    ('최악이었던 순간', "최악이었던 순간"),
)


class HistoryForm(forms.ModelForm):
    class Meta:
        model = History
        fields = ['title', 'mood', 'img', 'comment', 'place', 'custom_place', 'created_at', 'user']
        labels = {
            'title': _('제목'),
            'mood':_('내 기분'),
            'img': _('사진'),
            'comment': _('코멘트'),
            'place': _('장소'),
            'custom_place': _('장소'),
            'created_at':_('작성 시간'),
        }
        widgets = {
            'mood': forms.Select(choices=MOOD_POINT_CHOICES),
            'user': forms.HiddenInput(),
            'place' : forms.HiddenInput(),
            'created_at' : DateInput(),
        }
        help_texts = {
            'comment': _('일기를 작성해주세요.'),
            'created_at': _('날짜를 선택해 주세요.'),
        }


class UpdateHistoryForm(HistoryForm):
    class Meta:
        model = History
        exclude = ['user']

    def __init__(self, *args, **kwargs):
        self.request = kwargs.pop('request', None)
        super(HistoryForm, self).__init__(*args, **kwargs)

    def save(self, commit=True):
        instance = super(HistoryForm, self).save(commit=False)

        if commit:
            instance.save()
        return instance
