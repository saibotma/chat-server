./gradlew shadowJar
version=$(./gradlew -q printVersion)
docker build -t saibotma/chat-server:latest .
docker push saibotma/chat-server:latest
docker tag saibotma/chat-server:latest saibotma/chat-server:"$version"
docker push saibotma/chat-server:"$version"
