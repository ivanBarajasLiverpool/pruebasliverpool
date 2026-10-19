# Framework de Automatización QA — Comercio Unificado (Liverpool & Pocket)

Repositorio de pruebas automatizadas E2E para certificar los flujos críticos de compra en **Liverpool Web**, **Banana Republic** y la aplicación móvil **Liverpool Pocket (Android)**.

El proyecto fue diseñado buscando mantener una base de código limpia, fácil de leer por cualquier integrante del equipo de QA o desarrollo, y preparada para crecer sin duplicar lógica entre pruebas web y móviles.

---

## 1. Decisiones Técnicas y Arquitectura

### ¿Por qué Java 17 (LTS)?
Para este proyecto elegí **Java 17 LTS** como versión base por tres razones prácticas:

1. **Compatibilidad con el ecosistema actual de automatización:** A partir de Selenium 4 (versión `4.25.0` en este proyecto) y Appium Java Client `9.3.0`, el soporte y las mejoras de cliente HTTP nativo están optimizados para versiones LTS modernas de Java. Quedarse en Java 8 u 11 limita la actualización de dependencias clave a mediano plazo.
2. **Expresividad y menor código repetitivo:** Características nativas del lenguaje como los *Switch Expressions* (usados en `DriverManager` para instanciar navegadores) y el *Pattern Matching for `instanceof`* (utilizado en `TestListener` al distinguir entre un `WebDriver` web y un `AndroidDriver` móvil) hacen que el código de infraestructura sea más directo y seguro.
3. **Estabilidad y soporte a largo plazo (LTS):** Es el estándar actual en pipelines corporativos de CI/CD (GitHub Actions, Jenkins, GitLab CI), ofreciendo un recolector de basura más eficiente al ejecutar suites con múltiples sesiones de navegador y Appium.

---

### Estructura del Proyecto (Page Object Model)
El framework implementa el patrón **Page Object Model (POM)** separando estrictamente la interacción con la interfaz gráfica de las validaciones de negocio. Ningún Page Object contiene `Asserts`; las páginas devuelven estados o datos (como textos o montos en `BigDecimal`) y las clases de prueba dentro de `src/test/java` toman la decisión de aprobar o fallar el flujo.

```text
pruebasliverpool/
├── pom.xml
├── levanta-emulador.sh                  # Script auxiliar para iniciar el AVD Android
├── levanta-appium.sh                    # Script auxiliar para iniciar Appium 2 tras verificar ADB
├── ver-reporte.sh                       # Script para compilar y abrir el reporte de Allure
└── src/
    ├── main/java/com/liverpool/qa/
    │   ├── config/
    │   │   └── ConfigReader.java        # Lector de propiedades con soporte de override por CLI (-D)
    │   ├── drivers/
    │   │   └── DriverManager.java       # Gestión ThreadLocal para WebDriver (Web) y AndroidDriver (Appium)
    │   ├── pages/
    │   │   ├── base/
    │   │   │   └── BasePage.java        # Wrappers comunes de Selenium y sincronización explícita
    │   │   ├── web/
    │   │   │   ├── HomePage.java
    │   │   │   ├── ProductListPage.java
    │   │   │   ├── ProductDetailPage.java
    │   │   │   └── CartPage.java
    │   │   └── mobile/
    │   │       ├── BaseMobilePage.java  # Gestos W3C (swipe/scroll) y utilidades nativas Android
    │   │       ├── PocketHomePage.java
    │   │       ├── PocketProductListPage.java
    │   │       ├── PocketProductDetailPage.java
    │   │       └── PocketCartPage.java
    │   └── utils/
    │       └── PriceUtils.java          # Normalización de precios a BigDecimal (escala 2)
    └── test/
        ├── java/com/liverpool/qa/
        │   ├── base/
        │   │   └── BaseTest.java        # Ciclo de vida (@BeforeMethod / @AfterMethod) y hooks de Allure
        │   ├── listeners/
        │   │   └── TestListener.java    # Captura automática de pantalla y contexto en caso de fallo
        │   └── tests/
        │       ├── LiverpoolWebTest.java
        │       ├── BananaRepublicWebTest.java
        │       └── LiverpoolPocketTest.java
        └── resources/
            ├── config.properties        # Parámetros centralizados (URLs, timeouts, capabilities)
            ├── testing.xml              # Suite TestNG con los 3 flujos principales
            └── allure.properties
```

---

### Estrategia de Localizadores
Dado que tanto el portal de Liverpool como Banana Republic utilizan componentes dinámicos (React / Material UI en web y Jetpack Compose junto con vistas XML tradicionales en Android), se definió el siguiente orden de prioridad para evitar selectores frágiles:

1. **Atributos de prueba y semánticos (`data-testid` / `resource-id`):** En web se priorizan selectores como `button[data-testid='add-to-bag-button']` o `div[data-testid='checkout-payment-summary-total']`. En Android se priorizan IDs nativos del paquete (`mx.com.liverpool.shoppingapp:id/btn_header_bag`, `mx.com.liverpool.shoppingapp:id/badge`).
2. **Accesibilidad (`content-desc`) y jerarquía relativa:** En las pantallas de la app móvil construidas con Jetpack Compose (`plpResultsLayoutCompose`, `pdpLayoutCompose`), donde los nodos no exponen un `resource-id` individual, se utilizan descriptores de accesibilidad (`@content-desc='Explorar'`, `@content-desc='Product Image'`) y ejes XPath relativos (`following-sibling`, `parent`) anclados a etiquetas estáticas del negocio.
3. **Cero XPaths absolutos o clases autogeneradas:** Se descartó el uso de rutas posicionales largas o clases CSS ofuscadas que cambian en cada despliegue.

---

### Estrategia de Sincronización (Implicit vs. Explicit Waits)
En `DriverManager` se demuestra la configuración del `implicitlyWait` leyendo el valor desde `config.properties` (`timeout.implicit.seconds`). Sin embargo, siguiendo las recomendaciones oficiales de la documentación de Selenium 4, **el valor por defecto se mantiene en `0` segundos** para no mezclar esperas implícitas con explícitas (lo cual provoca tiempos de espera impredecibles).

Toda la sincronización recae sobre **Explicit Waits (`WebDriverWait`)** encapsulados en `BasePage` y `BaseMobilePage`:
* **Manejo de grillas asíncronas:** Al paginar en el listado de lavadoras (paso a la página 4), se espera explícitamente la obsolescencia del DOM anterior (`ExpectedConditions.stalenessOf`) antes de seleccionar el producto.
* **Transiciones de modales y backdrops:** En web, se gestiona el cierre de drawers de Material UI (`MuiBackdrop-root`) antes de interactuar con el botón de agregar a la bolsa o el ícono del carrito.
* **Desplazamiento controlado en móvil:** En `BaseMobilePage` se implementan gestos táctiles mediante la API estándar W3C (`PointerInput` y `Sequence`) verificando la visibilidad progresiva del elemento en cada desplazamiento.

---

### Validaciones Financieras con `BigDecimal`
En pruebas de comercio electrónico, usar `double` o `float` para comparar montos suele generar falsos fallos por precisión de punto flotante (por ejemplo, al restar descuentos con centavos como `.15` o `.85`). Todas las validaciones de precios en `CartPage` y `PocketCartPage` pasan por `PriceUtils.parsePrice()`, que limpia el formato de moneda y opera con `BigDecimal` a 2 decimales (`RoundingMode.HALF_UP`).

---

## 2. Requisitos Previos del Entorno

Para ejecutar la suite completa en local (probado en Ubuntu 22.04 LTS / Linux, compatible con macOS y Windows):

* **Java JDK 17** (`java -version` y variable `JAVA_HOME` configurada).
* **Apache Maven 3.8+** (`mvn -version`).
* **Google Chrome Stable** (Selenium Manager descarga automáticamente el `chromedriver` correspondiente).
* **Para el Flujo 3 (Mobile Android):**
  * **Node.js 20+** y **Appium 2.x** (`npm install -g appium && appium driver install uiautomator2`).
  * **Android SDK** (`platform-tools`, `emulator`) con variables `ANDROID_HOME` y `ANDROID_SDK_ROOT` exportadas.
  * Un emulador Android activo (o dispositivo físico conectado por ADB) con la aplicación **Liverpool Pocket** (`mx.com.liverpool.shoppingapp`) instalada.
* **Allure CLI** (opcional si se usa el plugin de Maven `mvn allure:serve`).

---

## 3. Configuración de Appium (Flujo 3 - Liverpool Pocket)

La configuración móvil está parametrizada en `src/test/resources/config.properties` y se carga mediante `UiAutomator2Options` en `DriverManager.initMobileDriver()`:

```properties
appium.server.url=http://127.0.0.1:4723
appium.platform.name=Android
appium.automation.name=UiAutomator2
appium.device.name=emulator-5554
appium.app.package=mx.com.liverpool.shoppingapp
appium.app.activity=mx.com.liverpool.shoppingapp.icon.AppIcon
appium.app.wait.activity=*
appium.no.reset=true
appium.auto.grant.permissions=true
appium.new.command.timeout=60
```

**Detalles importantes de esta configuración:**
* **`appium.app.activity` y `appium.app.wait.activity=*`:** El APK de Liverpool Pocket registra el alias `mx.com.liverpool.shoppingapp.icon.AppIcon` como `MAIN/LAUNCHER`, el cual redirige internamente hacia `.splash.view.SplashActivity` y posteriormente a `.MainActivity`. Configurar `appWaitActivity=*` junto con `activateApp` al inicio del test evita bloqueos de permisos o timeouts durante esa transición de pantallas.
* **`noReset=true` y limpieza preventiva:** Permite conservar los permisos y omitir el onboarding inicial en cada corrida. Además, `PocketHomePage` incluye el método `cleanBagIfNotEmpty()`, que revisa si quedaron artículos en la bolsa de una ejecución previa y los elimina antes de iniciar el flujo para no alterar el cálculo del Total con IVA.

### Scripts rápidos para levantar el entorno móvil
En la raíz del repositorio se incluyen dos scripts para facilitar el arranque en Linux/Mac:
```bash
# 1. Levanta el emulador Android con parámetros optimizados de RAM y espera a sys.boot_completed=1
./levanta-emulador.sh

# 2. En otra terminal (o si el emulador ya está encendido), inicia el servidor Appium en el puerto 4723
./levanta-appium.sh
```

---

## 4. Instrucciones de Ejecución y Reportes

### Ejecutar toda la suite (Flujos 1, 2 y 3)
Con el emulador Android y el servidor de Appium activos en segundo plano:
```bash
mvn clean test
```

### Ejecutar cada flujo de manera individual
Si deseas correr un flujo específico de forma aislada:

```bash
# Flujo 1: Liverpool Web (Categorías -> Línea Blanca -> Lavadoras -> Pág. 4 -> Garantía -> Bolsa)
mvn clean test -Dtest=LiverpoolWebTest

# Flujo 2: Banana Republic (Otros sitios -> Buscador -> Click & Collect -> Bolsa)
mvn clean test -Dtest=BananaRepublicWebTest

# Flujo 3: Liverpool Pocket Android (Categorías -> Electrónica -> Celulares -> Garantía -> Bolsa)
mvn clean test -Dtest=LiverpoolPocketTest
```

### Ejecución en modo Headless (Web)
El lector `ConfigReader` prioriza argumentos pasados por consola (`-D`) sobre el archivo `config.properties`. Para correr las pruebas web sin interfaz gráfica:
```bash
mvn clean test -Dbrowser.headless=true
```

### Generar y visualizar el reporte de Allure
Al finalizar cualquier ejecución, los resultados y las capturas de pantalla automáticas en caso de fallo se guardan en `target/allure-results`. Para abrir el reporte interactivo en el navegador:
```bash
./ver-reporte.sh
```
*(O directamente mediante Maven con `mvn allure:serve`).*

---

## 5. Diseño de Pruebas: Casos Funcionales, Negativos, Edge Cases y Riesgos

### A. Casos Funcionales Certificados (Happy Paths)
| ID | Canal | Flujo Certificado | Criterio de Aceptación / Validación |
| :--- | :--- | :--- | :--- |
| **CP-01** | Liverpool Web | Navegar por Categorías → Línea Blanca → Lavadoras → Página 4 → Seleccionar lavadora → Agregar garantía → Ir a Mi Bolsa. | El contador del header se actualiza, el producto aparece en Mi Bolsa y se cumple `Subtotal - Descuento = Total con IVA`. |
| **CP-02** | Banana Republic Web | Desde Liverpool Home ir a Otros sitios → Banana Republic → Buscar prenda → Seleccionar talla y Click & Collect (tienda por C.P.) → Agregar a bolsa. | El modal de tiendas permite elegir sucursal, la prenda se agrega a la bolsa y el precio cuadra con el `Total con IVA`. |
| **CP-03** | Liverpool Pocket (Android) | Navegar en Explorar → Electrónica → Celulares → Seleccionar equipo → Agregar cobertura **Liverpool Care** → Ir a Mi Bolsa. | Tanto el celular como la póliza aparecen en la bolsa y se cumple `Precio Final Celular + Precio Garantía = Total (IVA incluido)`. |

---

### B. Casos de Prueba Negativos Identificados
| ID | Escenario Negativo | Pasos Principales | Resultado Esperado |
| :--- | :--- | :--- | :--- |
| **CN-01** | **Búsqueda de tienda Click & Collect con Código Postal inválido o sin cobertura** | En el PDP de Banana Republic, abrir el drawer de Click & Collect ("Elige una tienda") e ingresar un C.P. inexistente (`"00000"`, `"99999"`) o alfanumérico (`"ABCDE"`). | El sistema no debe renderizar radio buttons de tiendas seleccionables, debe mostrar el mensaje de estado vacío (*"No se encontraron tiendas cercanas"*) y no permitir confirmar el cambio de tienda. |
| **CN-02** | **Intentar agregar prenda a la bolsa sin seleccionar talla (Banana Republic)** | Entrar al detalle de una prenda con múltiples tallas y hacer clic en *"Agregar a mi bolsa"* sin haber elegido ninguna talla previamente. | El botón *"Agregar a mi bolsa"* debe permanecer deshabilitado (`disabled`) o desplegar la validación requerida sin incrementar el contador de la bolsa en el header. |
| **CN-03** | **Búsqueda sin coincidencias en catálogo** | Ingresar una cadena sin resultados en la barra de búsqueda (ej. `"xyzproducto9999inexistente"`). | El sistema debe mostrar la vista informativa de cero resultados sin provocar errores 500 ni romper el layout de filtros. |
| **CN-04** | **Superar el límite máximo de unidades permitidas por SKU en la bolsa** | En Mi Bolsa, incrementar repetidamente la cantidad (`+`) de un celular o línea blanca por encima del tope de inventario o política de compra. | El sistema debe bloquear el incremento adicional y mostrar una alerta indicando el máximo de piezas permitidas por pedido. |

---

### C. Casos Frontera (Edge Cases)
1. **Cálculo de Total con IVA combinando producto con descuento fraccionado + garantía sin descuento:**
   * *Observación real durante las pruebas:* En productos como el *Samsung Galaxy A57* (precio lista `$11,999.00`, precio con descuento `$8,499.15`) al sumar la garantía **Liverpool Care** (`$2,036.00`), el descuento del equipo tiene centavos fraccionados (`.85`) pero la garantía no tiene descuento. El caso frontera valida que el descuento porcentual del artículo no afecte el costo de la póliza y que no existan diferencias de redondeo de 1 centavo en el Total con IVA (`$10,535.15`).
2. **Eliminación del producto principal dejando una garantía huérfana:**
   * Si el usuario agrega un celular o lavadora con garantía extendida y dentro de *Mi Bolsa* elimina únicamente el producto físico, el carrito debe eliminar en cascada la garantía asociada (no debe permitir llegar al checkout pagando solo una póliza de garantía sin producto).
3. **Diferencia de elegibilidad de garantía entre marcas y vendedores (1P vs. Marketplace 3P):**
   * En la categoría de Celulares conviven productos vendidos directamente por Liverpool (que ofrecen *Liverpool Care*) con productos de vendedores externos o marcas con esquema propio donde el bloque de garantía no se despliega. El flujo automatizado contempla esta condición seleccionando productos elegibles.
4. **Persistencia de sesión y carrito compartido entre ejecuciones móviles:**
   * Al abrir la aplicación con sesión persistente, los artículos agregados en corridas previas permanecen en la bolsa y alteran la validación del `Total (IVA incluido)`. Se resolvió implementando una verificación al inicio del flujo móvil que vacía la bolsa si detecta el badge activo en el header.
5. **Cambio de modalidad de entrega después de haber seleccionado tienda en Click & Collect:**
   * Seleccionar una sucursal en Click & Collect y, antes de agregar a la bolsa, cambiar la tarjeta de entrega a *"Recibe a domicilio"*. El carrito debe respetar la última selección del usuario.

---

### D. Riesgos Identificados y Estrategia de Mitigación
* **Volatilidad de catálogo, precios y stock en ambiente productivo:**
  * *Riesgo:* Al ejecutar pruebas sobre el sitio productivo (`liverpool.com.mx`), un SKU fijo puede agotarse de un día para otro o cambiar de página en el listado.
  * *Mitigación:* Los Page Objects no dependen de SKUs ni precios quemados en el código; seleccionan dinámicamente productos disponibles en la grilla y leen los montos en tiempo real desde la interfaz para comprobar la consistencia matemática del resumen de compra.
* **Protección Anti-Bots (WAF / Akamai / Cloudflare):**
  * *Riesgo:* Ejecuciones repetitivas o muy rápidas contra producción pueden ser bloqueadas o presentar retos CAPTCHA.
  * *Mitigación:* Se configuraron banderas limpias en `ChromeOptions`, tiempos de espera explícitos naturales y para ejecución continua en CI/CD se recomienda contar con un ambiente de *Staging/QA* o whitelist de IPs del runner.
* **Diferencias de rendimiento en emuladores Android (Cold Boot y renderizado Compose):**
  * *Riesgo:* El arranque en frío de `Liverpool Pocket` en un emulador x86_64 puede tardar más de 15 segundos en superar el *Splash Screen* dependiendo de la GPU y RAM del host, provocando fallos por timeout antes de llegar al Home.
  * *Mitigación:* Se incrementaron `adbExecTimeout` y `appWaitDuration` en `DriverManager`, se añadió `activateApp` al inicio del test y se implementó una espera específica de transición en `PocketHomePage` antes de interactuar con la barra inferior de navegación.
