FROM ubuntu:18.10
WORKDIR /app/
COPY . /build/
RUN apt-get update -y
RUN apt-get install atomicparsley ffmpeg openjdk-11-jdk maven -y
RUN cd /build/ && mvn install && cp /build/target/YtDlServer.jar /bin/Server.jar
RUN apt-get remove openjdk-11-jdk -y
RUN apt-get install openjdk-11-jre -y
CMD java -jar /bin/Server.jar