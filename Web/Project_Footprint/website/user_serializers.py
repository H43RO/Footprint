from rest_framework import serializers
from .models import User

class UserSerializer(serializers.ModelSerializer):
    def create(self, validated_data, *args, **kwargs):
        email = validated_data['email']
        password = validated_data['password']
        birth_date = validated_data['birth_date']
        nickname = validated_data['nickname']
        age = validated_data['age']
        # user = User(**validated_data)
        gender = validated_data['gender']
        # user = User.objects.create(email, password, birth_date, nickname, age, gender)
        user = User.objects.create(email=email, password=password, birth_date=birth_date, nickname=nickname, age=age, gender=gender)
        user.save()
        return user
    
    class Meta:
        model = User
        fields = ("email", "password", "birth_date", "nickname", "age", "gender")
        write_only_fields = ['password']
        # read_only_fields = ['id']