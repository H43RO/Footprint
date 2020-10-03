from .models import User
from rest_framework import viewsets
from django_filters import rest_framework as filters
from .serializers import UserListSerializer, UserLoginSerializer, UserUpdateSerializer
from rest_framework.generics import UpdateAPIView, DestroyAPIView


class UserListView(viewsets.ModelViewSet):
    queryset = User.objects.all()
    serializer_class = UserListSerializer
    filter_backends = (filters.DjangoFilterBackend,)
    filter_fields = ('id',)


class UserUpdateView(UpdateAPIView):
    queryset = User.objects.all()
    serializer_class = UserUpdateSerializer
    lookup_field = 'id'


class UserDeleteView(DestroyAPIView):
    queryset = User.objects.all()
    serializer_class = UserListSerializer
    lookup_field = 'id'