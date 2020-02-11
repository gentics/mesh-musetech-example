#!/bin/bash

set -o nounset
set -o errexit

PROJECT_DIR="`dirname \"$0\"`"
cd $PROJECT_DIR

IMAGE=gentics/mesh-musetech-demo-alexa-skill

echo "Building docker image"
docker build -t $IMAGE .

echo "Ready for deployment to docker hub"
read
docker push $IMAGE