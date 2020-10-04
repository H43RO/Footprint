from rest_framework import viewsets
from .serializers import NoticeSerializer
from .models import Post


class NoticeViewSet(viewsets.ModelViewSet):
    queryset = Post.objects.filter(post_div=1)
    serializer_class = NoticeSerializer

class EditorViewSet(viewsets.ModelViewSet):
    queryset = Post.objects.filter(post_div=0)
    serializer_class = NoticeSerializer
