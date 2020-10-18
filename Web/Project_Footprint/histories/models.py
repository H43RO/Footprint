from django.db import models
from django.utils.translation import ugettext_lazy as _
from accounts.models import User
from places.models import Place, HotPlace
from django.db.models import F
from django.core.exceptions import ValidationError
from django.contrib.admin import widgets
from django.forms.models import model_to_dict


DEFAULT_HISTORY = 1

GENDER_CHOICES = (
    (0, 'male'),
    (1, 'female'),
    (2, 'not specified'),
)


class History(models.Model):
    img = models.ImageField(blank=True, null=True, upload_to="blog/%Y/%m/%d")
    title = models.CharField(max_length=100, blank=True, null=True)
    mood = models.CharField(max_length=30, default=DEFAULT_HISTORY, blank=True)
    comment = models.TextField(max_length=1000, blank=True, null=True)
    place = models.ForeignKey(Place, on_delete=models.CASCADE, blank=True, null=True)
    custom_place = models.CharField(max_length=500, blank=True)
    user = models.ForeignKey(User, on_delete=models.CASCADE, blank=True)
    created_at = models.DateTimeField(auto_now_add=False, blank=True, null=True)
    updated_at = models.DateTimeField(auto_now=True)

    def save(self, *args, **kwargs):
        if not self.pk:
            Place.objects.filter(pk=self.place_id).update(count=F('count')+1)
            print(Place.objects.values('naver_place_id', 'title'))
            new = self.custom_place.replace(" ","")
            cmplist = []
            for item in Place.objects.values('naver_place_id','title'):
                places = item
                cmplist.append(places)

            for cmp in cmplist:
                if new == cmp['title'].replace(" ",""):
                    naverid = cmp['naver_place_id']

            HotPlace.objects.filter(pk=naverid).update(counts=F('counts')+1)

        super().save(*args, **kwargs)


    def __str__(self):
        return self.title + ': ' + self.comment[:3]


