#!/usr/bin/env bash
type nerdctl
result=$?
[ $result -ne 0 ] && {
  echo "No 'nerdctl' is found. Exiting..."
  exit
}

nerdctl ps | grep kafka && {
  echo "Stopping Kafka container..."
  nerdctl compose down
}