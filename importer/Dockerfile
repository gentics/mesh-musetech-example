FROM java:8-jdk AS build-env

# Step 1 - Cache deps based on pom.xml
RUN mkdir -p /importer/build
WORKDIR /importer/build
ADD pom.xml .
ADD .mvn ./.mvn
ADD mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Step 2 - Build importer
ADD src /importer/build/src
RUN ./mvnw -B -U clean package

# Step 3 - Build package image
FROM adoptopenjdk/openjdk11:x86_64-alpine-jre-11.0.5_10
ENV MESH_WAIT_TIMEOUT 200
ENV MUSETECH_MESH_HOST cr
ENV MUSETECH_MESH_PORT 8080
RUN mkdir /importer
COPY --from=build-env /importer/build/target/mesh-musetech-importer-0.0.1-SNAPSHOT.jar /importer/importer.jar

# Add content
WORKDIR /importer
ADD data ./data
CMD [ "java", "-jar" , "importer.jar"]
