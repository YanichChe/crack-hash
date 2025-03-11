#!/bin/bash

echo "Starting to add to consul"
curl -X PUT -d '{"id": "mongo1", "name": "mongodb", "address": "mongo1", "port": 27017}' http://localhost:8500/v1/agent/service/register
curl -X PUT -d '{"id": "mongo2", "name": "mongodb", "address": "mongo2", "port": 27017}' http://localhost:8500/v1/agent/service/register
curl -X PUT -d '{"id": "mongo3", "name": "mongodb", "address": "mongo3", "port": 27017}' http://localhost:8500/v1/agent/service/register