#!/bin/sh
set -e

export ANDROID_HOME=/opt/android-sdk
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"

if [ ! -d "$ANDROID_HOME/platforms/android-35" ]; then
  if [ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    cd "$ANDROID_HOME/cmdline-tools"
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O tools.zip
    unzip -q tools.zip && rm tools.zip && mv cmdline-tools latest
  fi
  yes | sdkmanager --licenses > /dev/null
  sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" > /dev/null
fi

mkdir -p /project && cd /project
cp -r /src/settings.gradle.kts /src/build.gradle.kts /src/gradle.properties /src/app .
rm -rf app/build
gradle assembleDebug --no-daemon
