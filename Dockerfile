FROM eclipse-temurin:11-jre
WORKDIR /app
COPY build/libs/chat-server.jar /app
CMD java -jar chat-server.jar
