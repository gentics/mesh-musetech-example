FROM java:8-jdk AS build-env
RUN mkdir -p /importer/build
WORKDIR /importer/build
ADD . /importer/build
RUN chmod +x mvnw 
RUN ./mvnw -B -U clean package

#FROM gcr.io/distroless/java
FROM java:8-jdk
ENV MESH_WAIT_TIMEOUT 200
ENV MESH_HOST cr
ENV MESH_PORT 8080
RUN mkdir /importer
COPY --from=build-env /importer/build/target/mesh-musetech-importer-0.0.1-SNAPSHOT.jar /importer/importer.jar

WORKDIR /importer
CMD [ "java", "-jar" , "importer.jar"]
