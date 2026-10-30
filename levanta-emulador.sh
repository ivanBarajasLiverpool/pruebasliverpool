#!/usr/bin/env bash
set -e

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=$HOME/Android/Sdk
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
export ANDROID_AVD_HOME=$HOME/.android/avd
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$JAVA_HOME/bin:$PATH

AVD_NAME="${1:-$($ANDROID_HOME/emulator/emulator -list-avds | head -n 1)}"

if [ -z "$AVD_NAME" ]; then
    echo "❌ No se encontró ningún AVD creado. Verifica con: emulator -list-avds"
    exit 1
fi

echo "========================================================"
echo " 📱 Levantando Emulador Android: $AVD_NAME"
echo "    (RAM: 2048MB | GPU: swiftshader_indirect)"
echo "========================================================"

"$ANDROID_HOME/emulator/emulator" \
    -avd "$AVD_NAME" \
    -memory 2048 \
    -gpu swiftshader_indirect \