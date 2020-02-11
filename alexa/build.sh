#!/bin/bash

set -o nounset
set -o errexit

PROJECT_DIR="`dirname \"$0\"`"
cd $PROJECT_DIR

IMAGE=gentics/mesh-musetech-demo-alexa-skill

echo -e "\nBuilding docker image"
docker build -t $IMAGE .

echo -e "\nReady to push. Press any key to continue"
read
docker push $IMAGE