FROM adoptopenjdk:11-jre-openj9
WORKDIR /app
COPY build/libs/chat-server.jar /app
CMD java -jar chat-server.jar
