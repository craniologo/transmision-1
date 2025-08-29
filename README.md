# wsonpe
wsonpe - SEA


Version de wsonpe con Java 17

Para levantar con el profile de desarrollo ejecutar. 

```
-Dspring.profiles.active=dev
```


### SSL Server

Execute in terminal

```
keytool -genkey -alias wsonpe -storetype JKS -keyalg RSA -keysize 2048 -validity 365 -keystore ssl-server.jks
```


### Running Docker

#### wsonpe
docker build . -t wsonpe
docker run -p 3000:3000 wsonpe (-d option could be used too)

#### apache-wsonpe
docker build . -t apache-wsonpe
docker run -d -p 80:80 -p 443:443 apache-wsonpe

#### Considerations
- Is needed to change the date (10OCT2021)
- Is needed to install the certificate .p12 on the browser (tested on chrome)
- Access is guaranteed from https://localhost and https://localhost/wsonpe , access through http is not allowed
- Needed to install certificate on java cacerts where code is running
- sea.onpe.gob.pe.crt y sea.onpe.gob.pe.key son los correctos, ese certificado apunta a venp.pe pero tiene como alternative subjectname a sea.onpe.gob.pe
- Needed to modify /etc/hosts to add: 127.0.0.1 sea.onpe.gob.pe

