from django.contrib.auth.forms import PasswordChangeForm

from .backends import EmailAuthBackend
from django.http import HttpResponse, HttpResponseRedirect, request
from django.core.exceptions import ValidationError
from django.core.validators import validate_email
from django.contrib.sites.shortcuts import get_current_site
from django.utils.http import urlsafe_base64_decode, urlsafe_base64_encode
from django.core.mail import EmailMessage
from django.utils.encoding import force_bytes, force_text
from django.shortcuts import render, get_object_or_404, redirect

from django.contrib.auth import login, authenticate, logout, update_session_auth_hash
from django.contrib.auth.decorators import login_required

from django.contrib import messages, auth
from django.db import transaction
from django.db.models import Count, Avg
from django.core.paginator import Paginator
from .forms import SignUpForm, PlaceRegisterForm, SignInForm, HistoryForm, UpdateHistoryForm, UpdateUserInfoForm, \
    CheckPasswordForm, UserPasswordUpdateForm
from .models import User, History, Place
from rest_framework.response import Response
from .backends import EmailAuthBackend
from .token import account_activation_token, message
from django.utils.translation import gettext_lazy as _
import requests


def index(request):
    context = {
        'items': '발자취'
    }
    return render(request, 'index.html', context)


def list(request):
    user = User.objects.all()
    context = {
        'users': user
    }
    return render(request, 'list.html', context)


def signup(request):
    if request.method == 'POST':
        form = SignUpForm(request.POST)
        if form.is_valid():
            form.save()
            user = authenticate(username=form.cleaned_data['email'], password=form.cleaned_data['password1'])
            if user is not None:
                current_site = get_current_site(request)
                domain = current_site.domain
                uid64 = urlsafe_base64_encode(force_bytes(user.pk))
                token = account_activation_token.make_token(user)
                message_data = message(domain, uid64, token)
                mail_title = _("이메일 인증을 완료해 주세요")
                mail_to = form.cleaned_data['email']
                email = EmailMessage(mail_title, message_data, to=[mail_to])
                email.send()
                # login(request, user)
                return HttpResponseRedirect('../list/')
    else:
        form = SignUpForm()
    return render(request, 'signup.html', {'form': form})


def signin(request):
    if request.method == 'POST':
        form = SignInForm(data=request.POST)
        if form.is_valid():
            user = authenticate(username=form.cleaned_data['username'], password=form.cleaned_data['password'])
            if user is not None:
                login(request, user)
                return HttpResponseRedirect('../index/')
        else:
            messages.error(request, '이메일 혹은 비밀번호를 다시 입력해주세요')
            return HttpResponseRedirect('../signin/')

    else:
        form = SignInForm()
    return render(request, 'signin.html', {'form': form})


def signout(request):
    auth.logout(request)
    return HttpResponseRedirect('../index/')


def user_activate(request, uidb64, token):
    try:
        uid = force_text(urlsafe_base64_decode(uidb64))
        user = User.objects.get(pk=uid)

        if account_activation_token.check_token(user, token):
            user.is_active = True
            user.save()
            return redirect('../place_search/')

    except ValidationError:
        return HttpResponse({"messge": "TYPE_ERROR"}, status=400)


def api_user_activate(request):
    if request.method == 'GET':
        user_id = request.GET.get('user_id')
        timestamp = request.GET.get('timestamp')
        signature = request.GET.get('signature')
        requests.post('http://127.0.0.1:8000/api/v1/accounts/verify-registration/', data={'user_id' : user_id, 'timestamp' : timestamp, 'signature' : signature  })
    return HttpResponseRedirect('../index/')


def myinfo(request):
    if request.user.is_authenticated:
        user_id = request.user.id
        context = {
            'users': User.objects.filter(id=user_id)
        }
        return render(request, 'myinfo.html', context)


def history(request):
    historys = History.objects.all()
    paginator = Paginator(historys, 5)  # 한 페이지에 5개씩 표시
    # page = request.GET.get('page')  # query params에서 page 데이터를 가져옴
    # items = paginator.get_page(page)  # 해당 페이지의 아이템으로 필터링
    place = Place.objects.all()
    context = {
        'historys': historys,
        'places' : place
    }
    return render(request, 'history_list.html', context)



def place_list(request):
    context = {
        'places': Place.objects.all()
    }
    return render(request, 'place_list.html', context)


def place_register(request):
    if request.method == 'POST':
        form = PlaceRegisterForm(request.POST)
        if form.is_valid():
            new_item = form.save()
        return HttpResponseRedirect('../place_list')
    form = PlaceRegisterForm()
    return render(request, 'place_register.html', {'form': form})


def place_restaurant(request):
    context = {
        'restaurants': Place.objects.filter(place_div=1)
    }
    return render(request, 'place_restaurant_list.html', context)


def place_sights(request):
    context = {
        'sights': Place.objects.filter(place_div=0)
    }
    return render(request, 'place_sights_list.html', context)


def place_search(request):
    place_search = Place.objects.all().order_by('-id')
    q = request.POST.get('q', "")

    if q:
        place_search = place_search.filter(title__icontains=q)
        return render(request, 'place_search.html', {'place_search': place_search, 'q': q})
    else:
        return render(request,'place_search.html')

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


def user_password_update(request):
    if request.method == 'POST':
        form = UserPasswordUpdateForm(request.user, request.POST)
        try:
            if form.is_valid():
                user = form.save()
                update_session_auth_hash(request, user)  # 변경된 비밀번호로 자동으로 로그인 시켜줌, 중요!
                return redirect('../index')
        except ValidationError as e:
            messages.error(request, e)
            return HttpResponseRedirect("../user_password_update")
    else:
        form = UserPasswordUpdateForm(request.user)
    return render(request, 'user_password_update.html', {'form': form})

