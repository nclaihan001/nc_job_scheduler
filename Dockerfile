FROM maven:3-adoptopenjdk-11 as build
ADD src /code/src
ADD pom.xml /code/pom.xml
WORKDIR /code
RUN mvn clean install

FROM adoptopenjdk:11-jre
COPY --from=build /code/target/*.jar /app.jar