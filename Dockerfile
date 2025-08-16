FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/*.jar app.jar
#createing for making a virtual environment
ENTRYPOINT ["java", "-jar", "app.jar"]
