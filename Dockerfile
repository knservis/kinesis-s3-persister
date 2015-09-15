FROM java:7-jre
COPY target/*jar-with-dependencies.jar /opt/kinesis-s3-persister/kinesis-s3-persister.jar
WORKDIR /opt/kinesis-s3-persister/
CMD java -jar kinesis-s3-persister.jar
