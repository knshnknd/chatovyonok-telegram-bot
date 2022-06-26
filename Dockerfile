FROM adoptopenjdk/openjdk16:alpine-jre
ARG JAR_FILE=chatovyonok.jar
WORKDIR /opt/app
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]


