from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import *
from rangefilter.filter import DateRangeFilter, DateTimeRangeFilter

# Register your models here..


class CustomUserAdmin(UserAdmin):
    model = User
    list_display = ('email', 'birth_date', 'age', 'gender', 'is_active', 'is_superuser', )
    list_filter = ('email', 'is_staff', 'is_active',)
    fieldsets = (
        (None, {'fields': ('email', 'password')}),
        ('Permissions', {'fields': ('is_staff', 'is_active')}),
    )
    add_fieldsets = (
        (None, {
            'classes': ('wide',),
            'fields': ('email', 'password1', 'password2', 'is_staff', 'is_active')}
        ),
    )
    search_fields = ('email',)
    ordering = ('email',)


class PlaceAdmin(admin.ModelAdmin):
    model = Place
    list_per_page = 10
    list_display = ('beacon_uuid', 'title', 'place_div', 'naver_place_id', 'count', 'created_at',)
    list_display_links = ('title', 'naver_place_id', 'beacon_uuid')
    list_filter = ('place_div',)
    search_fields = ('title', 'naver_place_id', 'beacon_uuid')
    ordering = ('-beacon_uuid', 'title', '-count',)

    actions = ['']


class HistoryAdmin(admin.ModelAdmin):
    model = History
    list_display = ('user', 'title', 'mood', 'place', 'created_at', 'updated_at')
    list_display_links = ('user', 'place', 'title', 'created_at')
    list_filter = ('user',
                   ('created_at', DateRangeFilter),
                   ('updated_at', DateRangeFilter),)
    search_fields = ('user', 'place')


class PostAdmin(admin.ModelAdmin):
    model = Post
    list_display = ('title', 'created_at', 'updated_at', 'post_div', 'description')
    list_display_links = ('title',)
    list_filter = ('post_div',
                   ('created_at', DateRangeFilter),
                   ('updated_at', DateRangeFilter),)


class HotPlaceAdmin(admin.ModelAdmin):
    model = HotPlace
    list_display = ('naverPlaceID', 'title', 'category','location')
    list_display_links = ('naverPlaceID',)


admin.site.register(User, CustomUserAdmin)
admin.site.register(Place, PlaceAdmin)
admin.site.register(History, HistoryAdmin)
admin.site.register(Post, PostAdmin)
admin.site.register(HotPlace, HotPlaceAdmin)

