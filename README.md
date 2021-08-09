# 🥾 발자취 (Project Footprint)
**2020 공개 SW 개발자 대회 결선 진출작**


발자취는 BLE(Bluetooth Low Energy) 핵심 기술인 'Beacon' 모듈을 활용하여, 사용자가 특정 장소를 방문하면, 이를 저장하여 자동으로 하루 일과를 기록해주는 서비스입니다. 자동으로 기록된 일과는 사용자의 마음대로 자신만의 다이어리 처럼 꾸밀 수 있습니다. 또한 임의로 다른 일과를 추가할 수도 있습니다. 이렇게 쌓인 사용자의 기록 하나하나가 서버에 저장되어, 자신만의 추억으로 간직하거나 SNS에 공유할 수 있습니다.


# 📺 시나리오 소개 동영상

[![Video Label](http://img.youtube.com/vi/Mo7A4ZyVBEg/0.jpg)](https://www.youtube.com/embed/Mo7A4ZyVBEg)


# 📷 Screenshot
<img width="30%" src="https://user-images.githubusercontent.com/30336663/123149524-51f61300-d49c-11eb-9b3b-fca8f1d841be.jpg"> <img width="30%" src="https://user-images.githubusercontent.com/30336663/123149572-5d493e80-d49c-11eb-9690-cb60d6c7dd47.jpg"> <img width="30%" src="https://user-images.githubusercontent.com/30336663/123149578-5e7a6b80-d49c-11eb-9111-c8889e36229f.jpg">

<img width="30%" src="https://user-images.githubusercontent.com/30336663/123151924-f2e5cd80-d49e-11eb-8cc3-2a06a7af54aa.gif"> <img width="30%" src="https://user-images.githubusercontent.com/30336663/123151947-f9744500-d49e-11eb-9d58-ab7aa4131e82.gif"> <img width="30%" src="https://user-images.githubusercontent.com/30336663/123152631-be264600-d49f-11eb-8930-af2d5f2e5626.gif">


# ⚙️ Development Environment
|Part|Version|
|------|---|
|**WAS**|Python 3.7 + **Django** 3.0 with PyCharm 2019 ( + Django Templates으로 Front-end 구현 )
|**Android App**|**Kotlin** 1.4 with Android Studio 4.0
|**Beacon Module**|**Raspberry Pi** 3B+, **Arduino UNO** with HM-10 BLE Module
|**Database**|**MySQL** 8.0.21


# 🖥 System Architecture

![_](https://user-images.githubusercontent.com/30336663/91571271-45d71d00-e981-11ea-9503-d3fefaa475dc.png)

![_2](https://user-images.githubusercontent.com/30336663/91571330-496aa400-e981-11ea-8c0c-9da38bc408bc.png)

![_3](https://user-images.githubusercontent.com/30336663/91571344-4b346780-e981-11ea-8f6e-75a721c40c0d.png)


# 📝 Features
![회원가입](https://user-images.githubusercontent.com/30336663/123148983-bebcdd80-d49b-11eb-8cf0-350081e23281.png)
![로그인](https://user-images.githubusercontent.com/30336663/123148945-b795cf80-d49b-11eb-81be-87de70b3b6f8.png)
![홈화면](https://user-images.githubusercontent.com/30336663/123148980-bcf31a00-d49b-11eb-9f83-95134478e8a6.png)
![핫플레이스](https://user-images.githubusercontent.com/30336663/123148976-bc5a8380-d49b-11eb-805a-7e4b704d3e6f.png)
![에디터픽](https://user-images.githubusercontent.com/30336663/123148968-bb295680-d49b-11eb-9173-2c2aa425ede7.png)
![주변 가까운 장소](https://user-images.githubusercontent.com/30336663/123148975-bc5a8380-d49b-11eb-84e6-0d19a488c1d7.png)
![장소 상세정보](https://user-images.githubusercontent.com/30336663/123148972-bbc1ed00-d49b-11eb-8f58-5b5c49b368ec.png)
![방문 확인](https://user-images.githubusercontent.com/30336663/123148963-ba90c000-d49b-11eb-9836-fb7cb022f477.png)
![발자취 조회](https://user-images.githubusercontent.com/30336663/123148958-b9f82980-d49b-11eb-9feb-6092eabb417b.png)
![발자취 임의 생성](https://user-images.githubusercontent.com/30336663/123148956-b95f9300-d49b-11eb-987b-95eea21440e4.png)
![발자취 공유](https://user-images.githubusercontent.com/30336663/123148951-b95f9300-d49b-11eb-82b7-1e1a19dd76d0.png)
![근접 푸시알림](https://user-images.githubusercontent.com/30336663/123148928-b369b200-d49b-11eb-97f1-9aa48ceb28fc.png)


# 🔥 Getting Started

필요한 코드 사용이 용이하도록 코드에 동작 설명 주석이 첨부 되어있습니다. 기본적인 사용법은 다음과 같습니다. 

순서대로 환경을 설정해야 정상적으로 동작합니다.

### Django Web Server

1. 'Web' 폴더에 들어있는 프로젝트 폴더를 내려받아 'PyCharm'을 통해 오픈 합니다.
2. Venv 에 진입한 다음, 아래의 명령어를 실행합니다.

    ```bash
    pip install -r requirements.txt
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


# ✋ Part
|Name|Part|
|------|---|
|**H43RO**|프로젝트 총괄, 아키텍처 설계, 안드로이드 앱 개발 및 디자인, Beacon 모듈 개발
|**hyywon**|DB Place Model 설계 및 Place 관련 API 개발, Place 관련 Front-end 구현
|**yulhee741**|DB History Model 설계 및 History 관련 API 개발, History 관련 Front-end 구현
|**Chanjongp**|DB User Model 설계 및 User 관련 API 개발, User 관련 Front-end 구현
|**jinsol0330**|DB User Model 설계 및 User 관련 API 개발, User 관련 Front-end 구현
