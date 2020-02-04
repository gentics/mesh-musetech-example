FROM java:openjdk-8-jre-alpine

ADD ./target/mesh-alexa-skill*.jar /server.jar

CMD ["java", "-jar", "server.jar"]