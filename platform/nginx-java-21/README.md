<div id="top-header" style="with:100%;height:auto;text-align:right;"></div>

# NGINX + JAVA 21

- [./back](../../README.md)
- [Contents](#container)
- [Installation](#installation)
- [Start Up](#start-up)
- [Management](#management)
<br>

## <a id="container"></a>Contents

Linux
```bash
/var/www $ cat /etc/os-release
NAME="Alpine Linux"
ID=alpine
VERSION_ID=3.23.3
PRETTY_NAME="Alpine Linux v3.23"
HOME_URL="https://alpinelinux.org/"
BUG_REPORT_URL="https://gitlab.alpinelinux.org/alpine/aports/-/issues"
```

Nginx
```bash
/var/www $ nginx -v
nginx version: nginx/1.28.2
```

Java
```bash
/var/www $ java -version
openjdk version "21.0.10" 2026-01-20 LTS
OpenJDK Runtime Environment Temurin-21.0.10+7 (build 21.0.10+7-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.10+7 (build 21.0.10+7-LTS, mixed mode, sharing)
```

Maven
```bash
$ mvn --version
Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
Maven home: /usr/share/java/maven-3
Java version: 21.0.10, vendor: Eclipse Adoptium, runtime: /opt/java/openjdk
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "6.12.38+kali-amd64", arch: "amd64", family: "unix"
```
<br>

## <a id="installation"></a>Installation

### Before building the container

If no Java Application is on `./apirest` *(Or your custom binded directory)*,  and you want a quick test by browser, you can copy the content of the REST API sample.

This is the REST API sample `./resources/apirest-sample/` content
```sh
└── resources
    ├── apirest
    └── apirest-sample
        ├── pom.xml
        ├── src
        │   └── main
        │       ├── java
        │       │   └── com
        │       │       └── example
        │       │           └── api
        │       │               ├── ApiApplication.java
        │       │               ├── controller
        │       │               │   ├── ApiController.java
        │       │               │   ├── UserController.java
        │       │               │   └── WebController.java
        │       │               ├── entity
        │       │               │   └── User.java
        │       │               └── repository
        │       │                   └── UserRepository.java
        │       └── resources
        │           ├── application.properties
        │           ├── db
        │           │   ├── changelog
        │           │   │   └── db.changelog-master.yaml
        │           │   └── migrations
        │           │       └── V20251026031708__create_users_table.sql
        │           ├── static
        │           │   └── files
        │           │       └── images
        │           │           ├── spring-boot.svg
        │           │           └── spring.svg
        │           └── templates
        │               └── home.html
        ├── supervisord.log
        └── target # ignored
```

```sh
$ cp -a ./resources/apirest-sample ./apirest
```

### Set up container resources

While the Java is compiling the application, container can reach machine CPU +100% and +95% MiB of of the giving RAM to the container. The more CPU, RAM, SWAP and PIDS given to the container, the less resources consume.

- https://docs.docker.com/reference/compose-file/deploy/#resources
- https://docs.docker.com/engine/containers/resource_constraints/#cpu

It is important to take into account remote server characteristics, so try to set on local the same resources. PIDS *("Process IDentifier." It is a unique numerical identifier assigned to each running process on the system.)* is not neccessary to reduce them.

- https://docs.docker.com/reference/cli/docker/container/run/#pid
- Checkout the local machine resources: `$ sudo docker info`

Set up your required runtime constraints on resources - **Set them up only when remote server has less than local machine** -

- https://docs.docker.com/engine/containers/run/#runtime-constraints-on-resources

Compiled consumtion with:

- CPU: 2.00
- RAM: 256M
- SWAP: 512M

```sh
$ sudo docker stats

CONTAINER ID   NAME                  CPU %     MEM USAGE / LIMIT   MEM %     NET I/O         BLOCK I/O         PIDS
e22707451e11   abbr-mp-apirest-dev   0.22%     188.1MiB / 256MiB   73.47%    1.25kB / 126B   29.3MB / 139kB    40
```

- https://docs.docker.com/reference/cli/docker/container/stats/

### Supervisor

Set the required configuration files by coping and updating them depending on your project in:

#### NGINX

The default example is a server block for a REST API, but it can be use for webapps too

- `./docker/config/nginx/conf.d-sample/default.conf` -> `./docker/config/nginx/conf.d/default.conf`

#### JAVA

This repository comes with two Dockerfiles. If you want to be able to compile Java code (not just run it), you need the Dockerfile.JDK. Otherwise, to just run the end application, the Dockerfile.JRE.

So to set the correct Dockerfile to build the container, choose one of them and copy
```bash
$ cp ./docker/Dockerfile.JDK ./docker/Dockerfile
```

Tu automatically run the JAR application, create the Supervisord service that runs the application. You can choose the dev or production version, and set it according to your project requirements

- `./docker/config/supervisor/conf.d-sample/java-jar.conf` -> `./docker/config/supervisor/conf.d/java-jar.conf`

```bash
[program:java-jar]
command=java -jar /var/www/target/app.jar
stdout_logfile=/dev/stdout
stdout_logfile_maxbytes=0
stderr_logfile=/dev/stderr
stderr_logfile_maxbytes=0
autorestart=false
startretries=0
```
<br>

## <a id="start-up"></a>Start Up

### Create container *(On terminal from this location)*

Once the variables required by `docker-compose.yml` file, build the container
```sh
$ make set
$ make create
```

After building the container, you can checkout its main information
```sh
$ make info
PLATFORMS DOCKER: NGINX - JAVA 21
Container ID.: 8c715d860551         # Container ID
Name.........: abbr-mp-apirest-dev  # Container name
Image........: abbr-mp-apirest-dev:alpine3.23-nginx-28-java-21  # container image
Memory.......: 256M                 # Max. memory use by container
Host.........: 127.0.0.1:7714       # Localhost + port to access container
Hostname.....: 192.168.1.41:7714    # Local machine hostname + port to access container
Docker.Host..: 172.18.0.2           # Docker assigned IP in local machine (PORT 80)
NetworkID....: 595a93e6a93d52075cb10fa64237a59c1059b9394f9c23647285b33238976d25
```

You can checkout the container logs by its name
```sh
$ sudo docker logs abbr-mp-apirest-dev
```

If you let opened a container port in the Dockerfile and it is not declared in the `docker-compose.yml` nor not binded between local machine and container, you can access through the Docker host. In thise example: http://172.18.0.2:3000
<br>

### Create the Java Application / REST API

To access into container you can use the Makefile recipe
```sh
$ make ssh
```

Once inside, you ca test the application
```bash
/var/www $ mvn spring-boot:run
```

Delete the target directory, where compiled files and build artifacts are stored. Essentially, mvn clean removes all previously compiled files and other artifacts, ensuring that the next build starts from a clean slate.
```sh
/var/www $ mvn clean package

[INFO] Scanning for projects...
Downloading ...
[INFO] Replacing main artifact /var/www/target/api-springboot-0.0.1-SNAPSHOT.jar with repackaged archive, adding nested dependencies in BOOT-INF/.
[INFO] The original artifact has been renamed to /var/www/target/api-springboot-0.0.1-SNAPSHOT.jar.original
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  24.314 s
[INFO] Finished at: 2025-10-25T13:41:49Z
[INFO] ------------------------------------------------------------------------
```

Set a proper JAR file name
```sh
/var/www $ java -jar target/api-springboot-0.0.1-SNAPSHOT.jar
/var/www $ cp target/api-springboot-0.0.1-SNAPSHOT.jar target/app.jar
```

Browser test:

- http://192.168.1.41:7501

All commands together:
```bash
/var/www $ mvn clean package && cp target/api-springboot-0.0.1-SNAPSHOT.jar target/app.jar && supervisorctl -c /etc/supervisor/supervisord.conf reload
```
<br>


## <a id="management"></a>Container Management

To manage the container, run the GNU Make recipes
```bash
$ make help
Usage: $ make [target]
Targets:
$ make help                           shows this Makefile help message
$ make port-check                     shows this project port availability on local machine
$ make env                            checks if docker .env file exists
$ make env-set                        sets docker .env file
$ make info                           shows container information
$ make ssh                            enters the container shell
$ make build                          builds the container from Dockerfile
$ make up                             attaches to containers for a service and also starts any linked services
$ make start                          starts the container and put on running
$ make stop                           stops the running container but data will not be destroyed
$ make restart                        restarts the running container
$ make clear                          removes container from Docker running containers
$ make destroy                        delete container image from Docker cache
$ make dev                            sets a development enviroment
$ make supervisord-conf               lists supervisord services set on the running container
$ make supervisord-update             updates supervisord services without the need of stoping or rebuilding the container
$ make nginx-conf                     shows nginx configuration set on the running container
$ make nginx-update                   updates nginx configuration without the need of stoping or rebuilding the container
$ make nginx-default-conf             shows nginx default server block set on the running container
$ make nginx-default-update           updates nginx default server block without the need of stoping or rebuilding the container
```
<br>

## Contributing

Contributions are very welcome! Please open issues or submit PRs for improvements, new features, or bug fixes.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes (`git commit -am 'feat: Add new feature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Create a new Pull Request
<br><br>

## License

This project is open-sourced under the [MIT license](LICENSE).

<!-- FOOTER -->
<br>

---

<br>

- [GO TOP ⮙](#top-header)