from django.db import models
from django.contrib.auth.models import AbstractUser
from django.contrib.auth.base_user import BaseUserManager
from django.utils.translation import ugettext_lazy as _
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
    age = models.IntegerField(blank=True, null=True)
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
    USERNAME_FIELD = 'email'
    email.db_index = True
    EMAIL_FIELD = 'email'
    REQUIRED_FIELDS = ['birth_date', 'nickname', 'gender']
    objects = UserManager()

    def __str__(self):
        return self.email

    def now_age(self):
        import datetime
        if self.birth_date is None:
            raise ValueError(_('birth_date must be set'))
        return int((datetime.date.today() - self.birth_date).days / 365.25 + 1)
    age = property(now_age)
        
