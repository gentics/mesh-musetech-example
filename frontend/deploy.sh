#!/bin/bash

SHA1=$(git rev-parse HEAD)
REF=${SHA1:0:4}


PUSH_IMAGE=gtx-docker-apps.docker.apa-it.at/gentics/apps/musetech-app:$REF
PULL_IMAGE=docker.apa-it.at/gentics/apps/musetech-app:$REF
DEPLOYMENT=mesh-website-musetech-app

echo "Building image: $REF"

docker build -t $PUSH_IMAGE .
docker push  $PUSH_IMAGE
kubectl -n core set image deployment/$DEPLOYMENT app=$PULL_IMAGE
kubectl -n core rollout status deployment/$DEPLOYMENT

