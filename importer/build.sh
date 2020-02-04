#!/bin/bash

set -o nounset
set -o errexit

echo -e "\nBuilding image"
docker build -t gentics/mesh-musetech-demo-importer .

echo -e "\nReady to push. Press any key to continue"
read
docker push gentics/mesh-musetech-demo-importer