from django.db import models
from ckeditor_uploader.fields import RichTextUploadingField

class Post(models.Model):
    contents = models.CharField(max_length=5000, blank=True,null=True)
    title = models.CharField(max_length=30)
    img = models.ImageField(blank=True, null=True, upload_to="Post/%Y/%m/")
    post_div = models.PositiveSmallIntegerField()
    description = RichTextUploadingField(blank=True,null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
