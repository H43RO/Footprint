from django.db import models

# Create your models here..

class Place(models.Model):
    beacon_uuid = models.CharField(max_length=100)
    title = models.CharField(max_length=20)
    place_div = models.PositiveSmallIntegerField()
    naver_place_id = models.CharField(max_length=100)