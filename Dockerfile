#FROM openjdk:17
#EXPOSE 8080
#WORKDIR /app
#COPY target/*.jar /app/*.jar
#CMD ["java", "-jar", "*.jar"]


FROM maven:3.8.3-openjdk-17 as build

WORKDIR /app
COPY pom.xml ./
COPY src ./src

RUN mvn -s settings.xml clean package -Dmaven.test.skip

FROM openjdk:17 as runner

WORKDIR /app
COPY --from=build /app/target/*.jar service.jar
COPY --from=build /app/pom.xml .
COPY --from=build /app/src ./src

RUN groupadd -r user && useradd -g user user
RUN chown -R user:user /app
USER user

ENTRYPOINT ["java", "-jar", "service.jar"]

