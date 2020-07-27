from django.db import models

# Create your models here..

class Place(models.Model):
    beacon_uuid = models.CharField(max_length=100)
    title = models.CharField(max_length=30)
    place_div = models.PositiveSmallIntegerField()
    naver_place_id = models.CharField(max_length=30)
    created_at = models.DateTimeField(auto_now_add=True)


class History(models.Model):
    #user_id = models.ForeignKey(User, on_delete=models.CASCADE)

    img = models.CharField(max_length=600, default=None, null=True)
    title = models.CharField(max_length=100)
    comment = models.CharField(max_length=1000)

    place = models.ForeignKey(Place, on_delete=models.CASCADE)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)



