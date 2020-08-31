from django.db import models
from django.db.models.signals import post_save
from django.dispatch import receiver
from django.conf import settings
from django.contrib.auth.models import AbstractUser, PermissionsMixin
from django.utils.translation import ugettext_lazy as _
from django.contrib.auth.base_user import BaseUserManager
from django.utils import timezone
from django.db.models import F
from ckeditor_uploader.fields import RichTextUploadingField
from datetime import date
from django.utils import timezone
from django.contrib.postgres.fields import ArrayField, JSONField

import jsonfield

DEFAULT_HISTORY = 1


GENDER_CHOICES = (
    (0, 'male'),
    (1, 'female'),
    (2, 'not specified'),
)


class UserManager(BaseUserManager):
    use_in_migrations = True
    """
    커스텀 유저모델 매니저 (고유 이메일)
    """
    
    def create_user(self, email, password, **extra_fields):
        if not email:
            raise ValueError(_('The Email must be set'))
        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)
        user.set_password(password)
        user.save()
        return user

    def create_superuser(self, email, password, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)
        extra_fields.setdefault('is_active', True)
        if extra_fields.get('is_staff') is not True:
            raise ValueError(_('Superuser must have is_staff=True.'))
        if extra_fields.get('is_superuser') is not True:
            raise ValueError(_('Superuser must have is_superuser=True.'))
        return self.create_user(email, password, **extra_fields)


class User(AbstractUser):
    username = None
    first_name = None
    last_name = None
    email = models.EmailField(max_length=255, unique=True)
    birth_date = models.DateField(null=True, blank=False)
    nickname = models.CharField(max_length=10, blank=False, null=True)
    age = models.IntegerField(blank=False, null=True)
    gender = models.IntegerField(choices=GENDER_CHOICES, blank=False, null=True)
    is_staff = models.BooleanField(
        _('staff status'),
        default=False,
        help_text=_('Admin 계정 접속을 위한 Boolean 필드'),
    )
    is_active = models.BooleanField(
        _('active'),
        default=False,  # 기본값을 False 로 변경
        help_text=_('유저 활성화를 하기 위한 Boolean 필드'),
    )
    image = models.ImageField(blank=True, null=True)
    
    email.db_index = True
    USERNAME_FIELD = 'email'
    EMAIL_FIELD = 'email'
    REQUIRED_FIELDS = ['birth_date', 'nickname', 'age', 'gender']
    objects = UserManager()

    def __str__(self):
        return self.email


class Place(models.Model):
    beacon_uuid = models.CharField(max_length=100)
    title = models.CharField(max_length=30)
    place_div = models.PositiveSmallIntegerField()
    naver_place_id = models.CharField(primary_key=True, max_length=30)
    created_at = models.DateTimeField(auto_now_add=True)
    img = models.ImageField(blank=True, null=True, upload_to="place")
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
    businessHours = models.CharField(max_length=100, blank=True, null=True,)
    description = models.CharField(max_length=200, blank=True, null=True,)
    imageSrc = models.CharField(max_length=1000,blank=True, null=True)
    menuName = JSONField(blank=True, null=True)
    menuPrice = JSONField(blank=True, null=True)
    counts = models.IntegerField(default=0, null=True)

    def __int__(self):
        return self.naverPlaceID


class History(models.Model):
    img = models.ImageField(blank=True, null=True, upload_to="blog/%Y/%m/%d")
    title = models.CharField(max_length=100, blank=True, null=True)
    mood = models.CharField(max_length=30, default=DEFAULT_HISTORY, blank=True)
    comment = models.TextField(max_length=1000, blank=True, null=True)
    place = models.ForeignKey(Place, on_delete=models.CASCADE, blank=True, null=True)
    custom_place = models.CharField(max_length=500, blank=True, null=True)
    user = models.ForeignKey(User, on_delete=models.CASCADE,default=DEFAULT_HISTORY)
    created_at = models.DateTimeField(auto_now_add=False, blank=True, null=True)
    updated_at = models.DateTimeField(auto_now=True)

    def save(self, *args, **kwargs):
        if not self.pk:
            Place.objects.filter(pk=self.place_id).update(count=F('count')+1)
            HotPlace.objects.filter(pk=self.place_id).update(counts=F('counts')+1)
        super().save(*args, **kwargs)


    def __str__(self):
        return self.title + ': ' + self.comment[:3]


class Post(models.Model):
    contents = models.CharField(max_length=5000, blank=True,null=True)
    title = models.CharField(max_length=30)
    img = models.ImageField(blank=True, null=True, upload_to="Post/%Y/%m/")
    post_div = models.PositiveSmallIntegerField()
    description = RichTextUploadingField(blank=True,null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

