#!/bin/sh

echo "###########################"
echo "#       BUILD STARTS      #"
echo "# made by ikb4stream team #"
echo "###########################"

echo "=============================================="
echo "Docker image => producer:latest will be build."
echo "=============================================="

cd producer/

docker build -t producer:latest

echo "=============================================="
echo "Docker image => consumer:latest will be build."
echo "=============================================="

cd ../consumer/

docker build -t consumer:latest

echo "################"
echo "#  BUILD ENDS  #"
echo "################"