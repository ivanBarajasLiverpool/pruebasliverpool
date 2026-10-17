#!/usr/bin/env bash
set -e

RESULTS_DIR="target/allure-results"
REPORT_DIR="target/allure-report"
THEME_FILE="src/test/resources/liverpool-theme.css"
CATEGORIES_FILE="src/test/resources/categories.json"

echo "========================================================"
echo "Generando Reporte QA — El Puerto de Liverpool"
echo "========================================================"

# 1. Verificar que existan resultados de ejecución
mkdir -p "$RESULTS_DIR"

# 2. Copiar categories.json si existe en resources
if [ -f "$CATEGORIES_FILE" ]; then
    cp "$CATEGORIES_FILE" "$RESULTS_DIR/categories.json"
    echo "Categorías de fallos (categories.json) cargadas."
fi

# 3. Asegurar widget de Environment con métricas de arquitectura (si no fue creado aún)
if [ ! -f "$RESULTS_DIR/environment.properties" ]; then
cat <<EOF > "$RESULTS_DIR/environment.properties"
Empresa=El Puerto de Liverpool - Desarrollo Digital
Area=Comercio Unificado - QA Automation
URL_Liverpool=https://www.liverpool.com.mx/tienda/home
URL_Banana_Republic=https://bananarepublic.liverpool.com.mx/
Java_Version=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
Motor_Web=Selenium WebDriver 4.25.0 (W3C)
Motor_Mobile=Appium Java Client 9.3.0 (UiAutomator2)
Patron_Diseno=Page Object Model (POM) + ThreadLocal
EOF
    echo "Métricas de entorno (environment.properties) inyectadas."
fi

# 4. Asegurar widget Executor
cat <<EOF > "$RESULTS_DIR/executor.json"
{
  "name": "Framework QA Automation - Comercio Unificado",
  "type": "maven",
  "buildName": "Certificación E2E: Liverpool Web, Banana Republic & Liverpool Pocket",
  "reportName": "El Puerto de Liverpool — Certificación QA"
}
EOF

# 5. Generar el reporte estático con Allure CLI
echo "⚙️  Compilando reporte HTML desde $RESULTS_DIR..."
allure generate "$RESULTS_DIR" --clean -o "$REPORT_DIR"

# 6. Personalizar título en el Dashboard y en la pestaña del navegador
if [ -f "$REPORT_DIR/widgets/summary.json" ]; then
    sed -i 's/"reportName"[[:space:]]*:[[:space:]]*"[^"]*"/"reportName": "El Puerto de Liverpool — Certificación QA"/g' "$REPORT_DIR/widgets/summary.json"
fi

if [ -f "$REPORT_DIR/index.html" ]; then
    sed -i 's/<title>Allure Report<\/title>/<title>Liverpool QA — Reporte de Automatización<\/title>/g' "$REPORT_DIR/index.html"
fi

# 7. Inyectar estética Rosa Liverpool (#E10098) en styles.css
if [ -f "$THEME_FILE" ]; then
    cat "$THEME_FILE" >> "$REPORT_DIR/styles.css"
else
cat << 'EOF' >> "$REPORT_DIR/styles.css"
/* === TEMA CORPORATIVO: EL PUERTO DE LIVERPOOL (#E10098) === */
.side-nav {
    background: linear-gradient(180deg, #8a005b 0%, #3d0028 100%) !important;
}
.side-nav__brand-text {
    font-size: 0 !important;
}
.side-nav__brand-text::after {
    content: "LIVERPOOL QA";
    font-size: 15px !important;
    font-weight: 800 !important;
    letter-spacing: 1px;
    color: #ffffff !important;
}
.side-nav__item_active,
.side-nav__link:hover {
    background-color: rgba(225, 0, 152, 0.35) !important;
    border-left: 4px solid #E10098 !important;
}
.pane__title,
.widget__title {
    color: #9e006a !important;
    font-weight: 700 !important;
}
EOF

fi

echo "🚀 Abriendo reporte en el navegador (Presiona Ctrl+C para cerrar el servidor)..."
allure open "$REPORT_DIR"