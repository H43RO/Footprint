from django.db import models

# Create your models here..


class Place(models.Model):
    title = models.CharField(max_length=100,default=None,null=True)

    created_at = models.DateTimeField(auto_now_add=True)  # 글 작성 시 (이 모델의 데이터(레코드) 저장 시) 생성 시각
    updated_at = models.DateTimeField(auto_now=True)


class History(models.Model):  # Restaurant 라는 상점을 나타내는 모델을 정의
    #user_id = models.ForeignKey(User, on_delete=models.CASCADE)  # 이름

    img = models.CharField(max_length=600, default=None, null=True)
    title = models.CharField(max_length=100)
    comment = models.CharField(max_length=1000)

    place = models.ForeignKey(Place, on_delete=models.CASCADE)

    created_at = models.DateTimeField(auto_now_add=True)  # 글 작성 시 (이 모델의 데이터(레코드) 저장 시) 생성 시각
    updated_at = models.DateTimeField(auto_now=True)  # 저장된 레코드 수정 시 수정 시각


