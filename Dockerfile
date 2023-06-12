FROM eclipse-temurin:17.0.5_8-jre-alpine
WORKDIR /app
COPY build/libs/chat-server.jar /app
CMD java -jar chat-server.jar
