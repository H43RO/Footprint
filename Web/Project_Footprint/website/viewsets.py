from .models import User, Place, History
from .place_info_serializers import PlaceSerializer
from .history_date_serializer import HistoryDateSerializer
from .history_serializer import HistorySerializer
from .user_info_serializer import UserListSerializer, UserUpdateSerializer
from .user_serializers import UserLoginSerializer
from rest_framework import viewsets, permissions, generics, status, mixins
from rest_framework.response import Response
from rest_framework.viewsets import ModelViewSet, ReadOnlyModelViewSet
from rest_framework.views import APIView
from rest_framework.decorators import api_view
from django_filters import rest_framework as filters
from rest_framework.viewsets import ModelViewSet, ReadOnlyModelViewSet
from .backends import EmailAuthBackend
from .token import account_activation_token, message
from django.utils.translation import gettext_lazy as _
from django_filters import FilterSet, CharFilter, NumberFilter, DateFilter
from rest_framework.decorators import action
from django.contrib.auth.decorators import login_required
from rest_framework.generics import (
    ListAPIView,
    UpdateAPIView,
    RetrieveUpdateAPIView,
    DestroyAPIView
)


class UserListView(viewsets.ModelViewSet):
    queryset = User.objects.all()
    serializer_class = UserListSerializer
    filter_backends = (filters.DjangoFilterBackend,)
    filter_fields = ('id',)


def history(request):
    if request.method == 'POST' and 'id' in request.POST:
        item = get_object_or_404(History, id=id)
        item.delete()
        return redirect('history-delete')
    historys = History.objects.all()
    # paginator = Paginator(historys, 5)  # 한 페이지에 5개씩 표시

    # page = request.GET.get('page')  # query params에서 page 데이터를 가져옴
    # items = paginator.get_page(page)  # 해당 페이지의 아이템으로 필터링
    context = {
        'historys': historys,
    }
    return render(request, 'history_list.html', context)


def history_create(request):
    if request.method == 'POST':
        form = HistoryForm(request.POST, request.FILES)  # request의 POST 데이터들을 바로 PostForm에 담을 수 있습니다.
        if form.is_valid():  # 데이터가 form 클래스에서 정의한 조건 (max_length 등)을 만족하는지 체크합니다.
            new_item = form.save()  # save 메소드로 입력받은 데이터를 레코드로 추가합니다.
        return HttpResponseRedirect('../')  # 리스트 화면으로 이동합니다.
    form = HistoryForm(request.FILES)  # 만약에 POST방식이 아니라면
    return render(request, 'history_create.html', {'form': form})


def history_delete(request, id):
    item = get_object_or_404(History, pk=id)
    if request.method == 'POST':
        item.delete()
        return redirect('history')  # 리스트 화면으로 이동합니다.

    return render(request, 'history_delete.html', {'item': item})


def history_update(request):
    if request.method == 'POST' and 'id' in request.POST:
        item = get_object_or_404(History, pk=request.POST.get('id'))
        form = UpdateHistoryForm(request.POST, request.FILES, instance=item)
        if form.is_valid():
            item = form.save()
    elif 'id' in request.GET:
        item = get_object_or_404(History, pk=request.GET.get('id'))
        form = HistoryForm(instance=item)
        form.password = ''  # password 데이터를 비웁니다.
        return render(request, 'history_update.html', {'form': form})
    return HttpResponseRedirect("../")


class HistoryViewSet(viewsets.ModelViewSet):
    queryset = History.objects.all()
    serializer_class = HistorySerializer


class HistoryUpdateAPIView(UpdateAPIView):
    queryset = History.objects.all()
    serializer_class = HistorySerializer
    lookup_field = 'id'


class HistoryDeleteAPIView(DestroyAPIView):
    queryset = History.objects.all()
    serializer_class = HistorySerializer
    lookup_field = 'id'


class PlaceTitleFilter(filters.FilterSet):
    class Meta:
        model = Place
        fields = {
            'title': ['icontains']
        }


class ApiPlaceId(viewsets.ModelViewSet):
    queryset = Place.objects.all()
    serializer_class = PlaceSerializer
    filter_backends = [filters.DjangoFilterBackend]
    filterset_class = PlaceTitleFilter
    # filter_backends = [SearchFilter]
    # search_fields = ['title']


@login_required
def user_info_update(request):
    if request.method == 'POST':
        form = UpdateUserInfoForm(request.POST, instance=request.user)
        if form.is_valid():
            form.save()
    elif 'id' in request.GET:
        form = UpdateUserInfoForm(instance=request.user)
        return render(request, 'user_info_update.html', {'form': form})
    return HttpResponseRedirect("../myinfo")


@login_required
def user_delete(request):
    if request.method == 'POST':
        password_form = CheckPasswordForm(request.user, request.POST)
        if password_form.is_valid():
            request.user.delete()
            logout(request)
            return redirect('../list')
        else:
            messages.error(request, '비밀번호를 다시 입력해주세요')
            return HttpResponseRedirect('../user_delete/')
    else:
        password_form = CheckPasswordForm(request.user)
        return render(request, 'user_delete.html', {'password_form': password_form})
    return HttpResponseRedirect("../list")


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


class HistoryFilter(FilterSet):
    title = CharFilter(lookup_expr='icontains')

    class Meta:
        model = History
        fields = ('title', 'created_at')


class HistoryDateFilter(filters.FilterSet):
    class Meta:
        model = History
        fields = {
            'title': ['icontains'],
            'created_at': ['date', 'date__lte', 'date__gte']
        }


class HistoryDateViewSet(viewsets.ModelViewSet):
    queryset = History.objects.all()
    serializer_class = HistorySerializer
    filterset_class = HistoryDateFilter
    filter_backends = [filters.DjangoFilterBackend]

    # filter_fields = ['title', 'created_at']

    @action(methods=['get'], detail=False)
    def newest(self, request):
        newest = self.get_queryset().order_by('created_at').last()
        serializer = self.get_serializer_class()(newest)
        return Response(serializer.data)
