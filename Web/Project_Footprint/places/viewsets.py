from .models import Place, HotPlace
from .serializers import PlaceSerializer, HotplaceSerializers
from django_filters.rest_framework import DjangoFilterBackend
from django_filters import FilterSet
from rest_framework.viewsets import ModelViewSet
from .views import place_detail_crawl, get_hotplace
from rest_framework.decorators import action

class PlaceFilter(FilterSet):
    class Meta:
        model = Place
        fields = {
            'title': ['icontains'],
            'beacon_uuid': ['exact'],
        }

class ApiPlaceId(ModelViewSet):
    queryset = Place.objects.all()
    serializer_class = PlaceSerializer
    filter_backends = [DjangoFilterBackend]
    filterset_class = PlaceFilter
    http_method_names = ['get']

class ApiHotPlace(ModelViewSet):
    queryset = Place.objects.order_by('-count')[:5]
    serializer_class = PlaceSerializer

    @action(methods=['get'], detail=False)
    def result(self, request):
        res = get_hotplace()
        places = []
        for item in res:
            places.append(place_detail_crawl(item))
        places = list(places)
        return Response(places,status=200)

class HotPlcaeViewSet(ModelViewSet):
    queryset = HotPlace.objects.order_by('-counts')[:5]
    serializer_class = HotplaceSerializers
