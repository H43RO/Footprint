from django.contrib import admin
from .models import Post
from rangefilter.filter import DateRangeFilter

class PostAdmin(admin.ModelAdmin):
    model = Post
    list_display = ('title', 'created_at', 'updated_at', 'post_div', 'description')
    list_display_links = ('title',)
    list_filter = ('post_div',
                   ('created_at', DateRangeFilter),
                   ('updated_at', DateRangeFilter),)

admin.site.register(Post, PostAdmin)