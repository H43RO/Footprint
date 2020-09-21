# 발자취 (Project Footprint)
**2020 공개 SW 개발자 대회 1차 심사 통과 출품작**


발자취는 BLE(Bluetooth Low Energy) 핵심 기술인 'Beacon' 을 활용하여, 사용자의 하루 일과를 자동으로 기록해주는 서비스입니다. 자동으로 기록된 일과는 사용자의 마음대로 자신만의 다이어리 처럼 꾸밀 수 있습니다. 또한 임의로 일과를 추가할 수도 있습니다. 이렇게 쌓인 사용자의 기록 하나하나가 서버에 저장되어, 자신만의 추억으로 간직하거나 SNS에 공유할 수 있습니다.

## Development Environment

- Web Server : Python 3.7 + **Django** 3.0 with PyCharm 2019 ( + Django Templates으로 Front-end 구현 )
- **Android App** : **Kotlin** 1.4 with Android Studio 4.0
- **Beacon Module** : **Raspberry Pi** 3B+, **Arduino UNO** with HM-10 BLE Module
- **Database** : **MySQL** 8.0.21

## System Architecture

![_](https://user-images.githubusercontent.com/30336663/91571271-45d71d00-e981-11ea-9503-d3fefaa475dc.png)

![_2](https://user-images.githubusercontent.com/30336663/91571330-496aa400-e981-11ea-8c0c-9da38bc408bc.png)

![_3](https://user-images.githubusercontent.com/30336663/91571344-4b346780-e981-11ea-8f6e-75a721c40c0d.png)

## Features

### Android App

- Foreground에서 동작하는 **Beacon Module Scanning 기능 제공**
    1. **가까운 장소(Beacon Module) 리스트 조회** 및 상세 정보 열람 가능
    2. **특정 장소가 주변에 있으면** 사용자에게 해당 **장소 상세 정보 푸시 알림** 발송
    3. **특정 장소에 방문하면** (거리가 매우 가까우면) **자동으로 방문** **히스토리 생성** 
- Retrofit2 Library를 활용하여 **Django REST framework API 전면 대응**

### Django Web Server

- 회원 로그인 및 회원가입, 비밀번호 초기화 API 제공
- **히스토리 조회** (날짜 별, 키워드 별 등), **생성, 수정** API 제공
- **Beacon Module 정보 조회** (UUID 기반) API 제공
- 사용자들이 **가장 많이 방문한 장소 리스트 (핫 플레이스)** 조회 API 제공
- 에디터가 직접 작성한 **추천 관광 루틴, 맛집 게시글 조회** API 제공

## Usage

필요한 코드 사용이 용이하도록 코드에 동작 설명 주석이 첨부 되어있습니다. 기본적인 사용법은 다음과 같습니다. 

순서대로 환경을 설정해야 정상적으로 동작합니다.

### Django Web Server

1. 'Web' 폴더에 들어있는 프로젝트 폴더를 내려받아 'PyCharm'을 통해 오픈 합니다.
2. Venv 에 진입한 다음, 아래의 명령어를 실행합니다.

    ```bash
    pip install —upgrade pip 
    pip install six
    pip install Pillow
    pip install djangorestframework
    pip install django-admin-rangefilter
    pip install djangoframework-fiters
    pip install django-filter
    pip install django-rest-registeration
    pip install django-grappelli
    pip install requests
    pip install django-crispy-forms
    pip install django-ckeditor
    pip install beautifulsoup4
    pip install jsonfield
    pip install mysqlclient  # MySQL을 사용할 경우
    ```

3. `settings.py` 에서 사용할 데이터베이스 설정을 완료해줍니다. (MySQL으로 기본 세팅 되어있습니다.)
4. 아래 명령어를 실행하여 관리자 계정을 만듭니다. 관리자 페이지 URL은 "localhost:8000/admin" 입니다.

    ```bash
    python manage.py createsuperuser
    ```

5. 아래 명령어를 실행하여 데이터베이스 마이그레이션을 한 뒤, 서버 구동을 시작합니다.

    ```bash
    python manage.py makemigrations website
    python manage.py migrate
    python manage.py runserver
    ```

6. 웹 사이트가 잘 뜨고, 모바일 앱의 동작이 원활하게 이루어질 시 적용 성공입니다.

7. 모바일 앱 대응을 위하여 호스팅을 해줘야하는데, 실제 서비스를 런칭하거나 배포를 하는 것이 아닌 테스트용 Self-Hosting을 할 때는 Ngrok 같은 서비스를 이용하여 외부에서도 접속가능한 URL을 부여할 수 있습니다. 

### Server Admin Page

1. 웹 서버가 구동되면,```127.0.0.1:8000/admin``` (관리자 페이지)으로 접속합니다.
2. 생성했던 관리자 계정을 통해 로그인 하며 간편한 데이터베이스 관리가 가능합니다.
3. Beacon Module을 등록하고 싶으면 Place 모델 인스턴스를 생성하면 됩니다.
   - 이 때, Beacon Module의 UUID와 등록하고 싶은 장소의 네이버 플레이스 ID를 기입합니다.
   - Beacon Module은 라즈베리파이, 아두이노가 없어도 Beacon Signal을 Advertising 할 수 있는 앱을 설치하여 그 기능을 대체할 수 있으며 해당 앱에서 Beacon Module의 UUID 값을 찾아 입력하시면 됩니다.
   - 네이버 플레이스 ID는 ```map.naver.com```에서 등록할 장소를 검색하고 브라우저 상 주소 경로의 ```/place/``` 뒤의 ID를 적어주시면 됩니다.
   
### Android App

1. 'App' 폴더에 들어있는 프로젝트 폴더를 내려 받아 'Android Studio'를 통해 오픈 합니다.
2. 'Network' 폴더에 들어있는 Website 클래스의 BaseURL을 구동하는 서버의 URL로 변경합니다.
3. 실제 기기를 통해 앱을 구동합니다. ( 에뮬레이터는 Bluetooth 관련 기능을 사용하지 못합니다. )

## Contributors

- **H43RO** : 프로젝트 총괄, 아키텍처 설계, 안드로이드 앱 개발 및 디자인
- **hyywon** : DB Place Model 설계 및 Django Place 관련 API 개발, Place 관련 Front-end 구현
- **yulhee741** : DB History Model 설계 및 Django History 관련 API 개발, History 관련 Front-end 구현
- **Chanjongp** : DB User Model 설계 및 Django User 관련 API 개발, User 관련 Front-end 구현
- **jinsol0330** : DB User Model 설계 및 Django User 관련 API 개발, User 관련 Front-end 구현
