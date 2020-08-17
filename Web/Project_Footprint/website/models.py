from django.db import models
from django.db.models.signals import post_save
from django.dispatch import receiver
from django.conf import settings
from django.contrib.auth.models import AbstractUser, PermissionsMixin
from django.utils.translation import ugettext_lazy as _
from django.contrib.auth.base_user import BaseUserManager
from django.utils import timezone
from django.db.models import F


DEFAULT_HISTORY = 1

GENDER_CHOICES = (
    (0, 'male'),
    (1, 'female'),
    (2, 'not specified'),
)


class UserManager(BaseUserManager):
    use_in_migrations = True
    """
    Custom user model manager where email is the unique identifiers
    for authentication instead of usernames.
    """

    def create_user(self, email, password, **extra_fields):
        """
        Create and save a User with the given email and password.
        """
        if not email:
            raise ValueError(_('The Email must be set'))
        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)
        user.set_password(password)
        user.save()
        return user

    def create_superuser(self, email, password, **extra_fields):
        """
        Create and save a SuperUser with the given email and password.
        """
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
        help_text=_('Designates whether the user can log into this admin site.'),
    )
    is_active = models.BooleanField(
        _('active'),
        default=True,  # 기본값을 False 로 변경
        help_text=_(
            'Designates whether this user should be treated as active. '
            'Unselect this instead of deleting accounts.'
        ),
    )

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

    def __str__(self):
        return self.title + ': ' + self.naver_place_id


class History(models.Model):
    img = models.ImageField(blank=True, null=True, upload_to="blog/%Y/%m/%d")
    title = models.TextField(max_length=100, blank=True, null=True)
    mood = models.CharField(max_length=30, default=DEFAULT_HISTORY)
    comment = models.TextField(max_length=1000, blank=True, null=True)
    place = models.ForeignKey(Place, on_delete=models.CASCADE, blank=True, null=True)
    custom_place = models.TextField(max_length=500, blank=True, null=True)
    user = models.ForeignKey(User, on_delete=models.CASCADE,default=DEFAULT_HISTORY)
    created_at = models.DateTimeField(auto_now_add=False)
    updated_at = models.DateTimeField(auto_now=True)

    def save(self, *args, **kwargs):
        if not self.pk:
            Place.objects.filter(pk=self.place_id).update(count=F('count')+1)
        super().save(*args, **kwargs)

    def __str__(self):
        return self.title + ': ' + self.comment[:3]


class Notice(models.Model):
    contents = models.CharField(max_length=5000)
    title = models.CharField(max_length=30)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
