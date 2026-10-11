#!/bin/sh
set -e
cd "$(dirname "$0")"

IMAGE=gradle:8.11.1-jdk17
NAME=buvette-facile-build

docker rm -f "$NAME" > /dev/null 2>&1 || true
docker run --name "$NAME" -u root \
  -v buvette-facile-sdk:/opt/android-sdk \
  -v buvette-facile-gradle:/home/gradle/.gradle \
  -v "$PWD":/src:ro \
  "$IMAGE" sh /src/docker/build-in-container.sh

mkdir -p out
docker cp "$NAME":/project/app/build/outputs/apk/debug/app-debug.apk out/buvette-facile.apk
docker rm "$NAME" > /dev/null
echo "APK: $PWD/out/buvette-facile.apk"
