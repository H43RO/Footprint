from django.db import models
from django.db.models.signals import post_save
from django.dispatch import receiver
from django.conf import settings
from django.contrib.auth.models import AbstractUser, PermissionsMixin
from django.utils.translation import ugettext_lazy as _
from django.contrib.auth.base_user import BaseUserManager


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
    email = models.EmailField(_('email address'), max_length=255, unique=True)
    birth_date = models.DateField(null=True, blank=False)
    nickname = models.CharField(max_length=10, blank=False, null=True)
    age = models.IntegerField(blank=False, null=True)
    gender = models.IntegerField(choices=GENDER_CHOICES, blank=False, null=True)

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = ['birth_date', 'nickname', 'age', 'gender']
    objects = UserManager()

    def __str__(self):
        return self.email


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

