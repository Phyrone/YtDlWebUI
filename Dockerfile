FROM openjdk:11-jdk
WORKDIR /app/
COPY . /build/
RUN apt-get update -y
RUN apt-get install atomicparsley ffmpeg maven python-pip -y
RUN pip install --upgrade youtube-dl
RUN cd /build/ && mvn install && cp /build/target/YtDlServer.jar /bin/Server.jar
RUN apt-get remove maven -y
RUN rm /build/ -R
CMD java -jar /bin/Server.jar