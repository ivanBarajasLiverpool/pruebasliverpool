#!/usr/bin/env bash
set -e

# =========================================================
# 1. EXPORTAR VARIABLES DE ENTORNO (JAVA + ANDROID SDK)
# =========================================================
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=$HOME/Android/Sdk
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
export ANDROID_AVD_HOME=$HOME/.android/avd
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$JAVA_HOME/bin:$PATH

# Si existe NVM (Node.js para Appium), cargarlo también
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

echo "========================================================"
echo " 📱 Iniciando Entorno Mobile (Emulador Android + Appium)"
echo "========================================================"

# =========================================================
# 2. VERIFICAR O LEVANTAR EL EMULADOR ANDROID
# =========================================================
RUNNING_DEVICE=$(adb devices | grep -w "device" | awk '{print $1}' | head -n 1 || true)

if [ -n "$RUNNING_DEVICE" ]; then
    echo "✅ Emulador ya encendido y conectado: $RUNNING_DEVICE"
else
    # Puedes pasar el nombre del AVD como parámetro (ej. ./levantar-mobile.sh Pixel_API_30)
    # Si no lo pasas, toma automáticamente el primer AVD instalado
    AVD_NAME="${1:-$($ANDROID_HOME/emulator/emulator -list-avds | head -n 1)}"

    if [ -z "$AVD_NAME" ]; then
        echo "❌ No se encontró ningún AVD creado. Verifica con: emulator -list-avds"
        exit 1
    fi

    echo "🚀 Levantando emulador '$AVD_NAME' (RAM: 2048MB | GPU: swiftshader_indirect)..."
    nohup "$ANDROID_HOME/emulator/emulator" \
        -avd "$AVD_NAME" \
        -memory 2048 \
        -gpu swiftshader_indirect \
        -no-snapshot-load > /tmp/emulator.log 2>&1 &

    echo "⏳ Esperando a que ADB detecte el dispositivo..."
    adb wait-for-device

    echo "⏳ Esperando a que Android termine de iniciar el sistema (sys.boot_completed=1)..."
    while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
        sleep 2
        echo -n "."
    done
    echo ""
    echo "✅ Emulador '$AVD_NAME' 100% encendido y listo."
fi

# =========================================================
# 3. INICIAR SERVIDOR APPIUM 2 (PUERTO 4723)
# =========================================================
if ss -tuln | grep -q ":4723 "; then
    echo "✅ El servidor Appium ya está corriendo en el puerto 4723."
else
    echo "🔥 Iniciando Appium Server en http://127.0.0.1:4723 (--allow-cors)..."
    echo "   (Presiona Ctrl+C cuando quieras detener el servidor Appium)"
    appium --allow-cors
fi