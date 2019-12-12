FROM openjdk:8u151-jdk-alpine3.7
RUN mkdir /home/COSTER
ADD ./data /home/COSTER/data
ADD ./model /home/COSTER/model
ADD ./logs /home/COSTER/logs
RUN apk update && \
	apk upgrade && \
	apk add bash vim sudo
ADD ./COSTER.jar /home/COSTER/COSTER.jar
WORKDIR /home/COSTER
ENTRYPOINT ["/bin/bash"]
