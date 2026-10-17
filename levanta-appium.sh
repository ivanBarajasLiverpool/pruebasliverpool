#!/usr/bin/env bash
set -e

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=$HOME/Android/Sdk
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$JAVA_HOME/bin:$PATH

# Cargar NVM (Node.js / Appium)
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

echo "========================================================"
echo " 🔥 Preparando Servidor Appium 2 (Puerto 4723)"
echo "========================================================"

echo "⏳ Esperando a que el emulador Android esté conectado y encendido..."
adb wait-for-device

while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
    sleep 2
    echo -n "."
done
echo ""
echo "✅ Emulador listo ($(adb devices | grep -w 'device' | awk '{print $1}'))."

echo "🚀 Iniciando Appium Server en http://127.0.0.1:4723 (--allow-cors)..."
appium --allow-cors