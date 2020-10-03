from django.contrib import admin
from .models import Place, HotPlace

class PlaceAdmin(admin.ModelAdmin):
    model = Place
    list_per_page = 10
    list_display = ('beacon_uuid', 'title', 'place_div', 'naver_place_id', 'count', 'created_at',)
    list_display_links = ('title', 'naver_place_id', 'beacon_uuid')
    list_filter = ('place_div',)
    search_fields = ('title', 'naver_place_id', 'beacon_uuid')
    ordering = ('-beacon_uuid', 'title', '-count',)

    actions = ['']

class HotPlaceAdmin(admin.ModelAdmin):
    model = HotPlace
    list_display = ('naverPlaceID', 'title', 'category','location','counts')
    list_display_links = ('naverPlaceID',)


admin.site.register(Place, PlaceAdmin)
admin.site.register(HotPlace, HotPlaceAdmin)  