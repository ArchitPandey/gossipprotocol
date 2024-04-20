FROM openjdk:17-jdk-alpine
LABEL authors="Archit"
COPY target/gossipprotocol-0.0.3-SNAPSHOT.jar gossipprotocol-0.0.3-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/gossipprotocol-0.0.3-SNAPSHOT.jar"]
EXPOSE 8080