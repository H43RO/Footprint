from django.forms import ModelForm
from .models import Place
from django.utils.translation import gettext_lazy as _

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