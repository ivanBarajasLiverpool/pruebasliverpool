package com.liverpool.qa.base;

import com.liverpool.qa.config.ConfigReader;
import com.liverpool.qa.drivers.DriverManager;
import com.liverpool.qa.listeners.TestListener;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

@Listeners(TestListener.class)
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Devuelve la instancia activa del WebDriver (ChromeDriver o AndroidDriver) para el hilo actual.
     */
    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    /**
     * Se ejecuta antes de cada método @Test.
     * Determina si debe levantar Selenium (Web) o Appium (Android) según:
     * 1. El parámetro <parameter name="platform" .../> de testng.xml
     * 2. La variable de consola -Dexecution.platform=...
     * 3. El valor por defecto en config.properties
     */
    @BeforeMethod(alwaysRun = true)
    @Parameters({"platform", "browser"})
    public void setUp(@Optional String platform, @Optional String browser) {
        String targetPlatform = (platform != null && !platform.isBlank())
                ? platform.trim()
                : ConfigReader.get("execution.platform", "web").trim();

        log.info("Configurando entorno de prueba para plataforma: [{}]", targetPlatform);

        if (targetPlatform.equalsIgnoreCase("android")) {
            startMobileSession();
        } else {
            String targetBrowser = (browser != null && !browser.isBlank())
                    ? browser.trim()
                    : ConfigReader.get("browser.name", "chrome").trim();
            String baseUrl = ConfigReader.get("url.liverpool", "https://www.liverpool.com.mx/tienda/home");
            startWebSession(targetBrowser, baseUrl);
        }
    }

    @Step("Iniciar sesión móvil en Android (Liverpool Pocket)")
    private void startMobileSession() {
        DriverManager.initMobileDriver();
    }

    @Step("Iniciar navegador [{browser}] y abrir URL base: {baseUrl}")
    private void startWebSession(String browser, String baseUrl) {
        DriverManager.initWebDriver(browser);
        getDriver().get(baseUrl);
    }

    /**
     * Se ejecuta después de cada método @Test (incluso si falló, después de que TestListener tomó la captura).
     */
    @AfterMethod(alwaysRun = true)
    @Step("Cerrar sesión del driver y liberar recursos")
    public void tearDown() {
        DriverManager.quitDriver();
    }
}