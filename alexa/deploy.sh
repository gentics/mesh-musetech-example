#!/bin/bash

set -o nounset
set -o errexit

SHA1=$(git rev-parse HEAD)
REF=${SHA1:0:4}
PROJECT_DIR="`dirname \"$0\"`"
cd $PROJECT_DIR

PUSH_IMAGE=gentics/mesh-musetech-demo-alexa-skill
PULL_IMAGE=$PUSH_IMAGE
DEPLOYMENT=mesh-website-musetech-alexa

echo -e "\nBuilding docker image"
docker build -t $PUSH_IMAGE .

echo -e "\nReady to push. Press any key to continue"
read
docker push $PUSH_IMAGE

echo -e "\nReady to update on K8S. Press any key to continue"
read
kubectl -n core set image deployment/$DEPLOYMENT alexa=$PULL_IMAGE
kubectl -n core rollout status deployment/$DEPLOYMENT
