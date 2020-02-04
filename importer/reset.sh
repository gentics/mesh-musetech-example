#!/bin/bash

kubectl delete job test-import-001
kubectl create job --from=cronjob/mesh-website-musetech-cronjob test-import-001

