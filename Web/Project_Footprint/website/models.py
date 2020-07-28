from django.db import models
from django.db.models.signals import post_save
from django.dispatch import receiver
from django.conf import settings
from django.contrib.auth.models import AbstractUser

# Create your models here..


class Place(models.Model):
    beacon_uuid = models.CharField(max_length=100)
    title = models.CharField(max_length=30)
    place_div = models.PositiveSmallIntegerField()
    naver_place_id = models.CharField(max_length=30)
    created_at = models.DateTimeField(auto_now_add=True)


class User(AbstractUser):
    birth_date = models.DateField(null=True, blank=True)
    nickname = models.CharField(max_length=15, null=True)
    age = models.IntegerField(null=True)
    gender = models.CharField(max_length=2, null=True)


# @receiver(post_save, sender=User)
# def create_user_profile(sender, instance, created, **kwargs):
#     if created:
#         Profile.objects.create(user=instance)

# @receiver(post_save, sender=User)
# def save_user_profile(sender, instance, **kwargs):
#     instance.profile.save()