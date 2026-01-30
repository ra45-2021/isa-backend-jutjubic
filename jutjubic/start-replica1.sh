#!/bin/bash
echo "========================================"
echo "Starting Replica 1 on port 8081"
echo "Replica ID: replica_1"
echo "========================================"
echo

mvn spring-boot:run -Dspring-boot.run.profiles=replica1
