FROM java:8-jdk AS build-env

# Step 1 - Cache deps based on pom.xml
RUN mkdir -p /server/build
WORKDIR /server/build
ADD pom.xml .
ADD .mvn ./.mvn
ADD mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Step 2 - Build server
ADD src /server/build/src
RUN ./mvnw -B -U clean package

# Step 3 - Build package image
FROM adoptopenjdk/openjdk11:x86_64-alpine-jre-11.0.5_10
ENV MESH_WAIT_TIMEOUT 200
ENV MUSETECH_MESH_HOST cr
ENV MUSETECH_MESH_PORT 8080
RUN mkdir /server
COPY --from=build-env /server/build/target/mesh-musetech-alexa-skill-0.0.1-SNAPSHOT.jar /server/server.jar

RUN apk add tzdata && \
    cp /usr/share/zoneinfo/Europe/Brussels /etc/localtime && \
    echo "Europe/Brussels" > /etc/timezone && \
    apk del tzdata

# Add content
WORKDIR /server
CMD [ "java", "-jar" , "server.jar"]
