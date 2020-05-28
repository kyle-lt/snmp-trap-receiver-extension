#!/bin/bash

echo "Building extension with maven."

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_251.jdk/Contents/Home/ mvn clean install

echo "Building image with docker-compose."

docker-compose build --no-cache

echo "Running container with docker-compose."

docker-compose up -d

exit 0
