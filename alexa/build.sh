#!/bin/bash

PROJECT_DIR="`dirname \"$0\"`"
cd $PROJECT_DIR

./mvnw clean package -DskipTests
docker build -t gentics/mesh-alexa-skill-demo:latest .

