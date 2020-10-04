from django.contrib import admin
from .models import History
from rangefilter.filter import DateRangeFilter

class HistoryAdmin(admin.ModelAdmin):
    model = History
    list_display = ('user', 'title', 'mood', 'place', 'created_at', 'updated_at')
    list_display_links = ('user', 'place', 'title', 'created_at')
    list_filter = ('user',
                   ('created_at', DateRangeFilter),
                   ('updated_at', DateRangeFilter),)
    search_fields = ('user', 'place')

admin.site.register(History, HistoryAdmin)