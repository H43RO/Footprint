from django.contrib.postgres.fields import JSONField
from django.db import models
import jsonfield


class Place(models.Model):
    beacon_uuid = models.CharField(max_length=100)
    title = models.CharField(max_length=30)
    place_div = models.PositiveSmallIntegerField()
    naver_place_id = models.CharField(primary_key=True, max_length=30)
    created_at = models.DateTimeField(auto_now_add=True)
    img = models.ImageField(upload_to="place/%Y/%m/%d", default="Img")
    count = models.IntegerField(null=True, default=0)

    beacon_uuid.db_index = True
    title.db_index = True
    place_div.db_index = True
    naver_place_id.db_index = True

    def __str__(self):
        return self.title


class HotPlace(models.Model):
    naverPlaceID = models.IntegerField(primary_key=True)
    title = models.CharField(max_length=30)
    category = models.CharField(max_length=30, default='')
    location = models.CharField(max_length=200, default='')
    businessHours = models.CharField(max_length=100, blank=True, null=True, )
    description = models.CharField(max_length=200, blank=True, null=True, )
    imageSrc = models.CharField(max_length=1000, blank=True, null=True)
    menuName = JSONField(blank=False, null=True)
    menuPrice = JSONField(blank=True, null=True)
    counts = models.IntegerField(default=0, null=True)

    def __int__(self):
        return self.naverPlaceID
