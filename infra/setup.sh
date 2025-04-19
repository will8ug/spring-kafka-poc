#!/usr/bin/env bash
type nerdctl
result=$?
[ $result -ne 0 ] && {
  echo "No 'nerdctl' is found. Exiting..."
  exit
}

nerdctl ps | grep kafka || {
  echo "Starting Kafka container..."
  nerdctl compose up -d
  echo "Waiting for a while until the Kafka is warmed up..."
  sleep 15
}