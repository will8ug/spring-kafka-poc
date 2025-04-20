#!/usr/bin/env bash

while [ $# -ge 1 ]; do
  if [ "$1"x = "--skip-teardown"x ]; then
    echo "'--skip-teardown' is found. Skipping teardown..."
    exit
  fi

  shift
done

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