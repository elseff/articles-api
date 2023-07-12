# Articles API

>Backend part of Artyicles Application

<hr/>

### For launch
You need to create the database and specify to specify the url, user and password database variables
in <b> Run/Debug Configurations </b> or in the `application.yaml`

```
spring:
    datasource:
        url: database url
        username: database username
        password: database password 
```
<hr/>

### Run with Docker-compose
You can run the application with the Docker-compose.

```
mvn clean package
docker-compose build
docker-compose up
```
<hr/>

### For use Flyway Plugin
You need to specify the url, user and password database variables in the `flyway.conf`

```
flyway.url= database url
flyway.user= database user
flyway.password= database password
```
