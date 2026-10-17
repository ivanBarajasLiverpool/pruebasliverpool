package main.java.com.liverpool.qa.drivers;

import com.liverpool.qa.config.ConfigReader;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

public final class DriverManager {

    private static final Logger log = LoggerFactory.getLogger(DriverManager.class);

    // Contenedor ThreadLocal para permitir ejecución paralela 
    private static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    // Constructor privado para evitar instanciación (Utility/Factory class)
    private DriverManager() {
    }

    /**
     * Obtiene la instancia activa del driver (Web o Mobile) para el hilo actual.
     */
    public static WebDriver getDriver() {
        return driverThread.get();
    }

    /**
     * Inicializa el navegador Web (Chrome por defecto) para las pruebas de Liverpool y Banana Republic.
     */
    public static void initWebDriver(String browser) {
        if (driverThread.get() != null) {
            return;
        }

        String targetBrowser = (browser != null && !browser.isBlank())
                ? browser.toLowerCase()
                : ConfigReader.get("browser", "chrome").toLowerCase();

        log.info("Inicializando WebDriver para navegador: {}", targetBrowser);

        WebDriver driver = switch (targetBrowser) {
            case "firefox" -> new FirefoxDriver();
            case "edge" -> new EdgeDriver();
            default -> {
                ChromeOptions options = new ChromeOptions();
                options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                options.addArguments("--start-maximized");
                options.addArguments("--disable-notifications");
                options.addArguments("--disable-popup-blocking");
                // Reduce bloqueos básicos de detección de automatización en e-commerce
                options.addArguments("--disable-blink-features=AutomationControlled");
                yield new ChromeDriver(options);
            }
        };

        // Configuración documentada de Implicit Wait (leída desde config.properties)
        int implicitWaitSec = Integer.parseInt(ConfigReader.get("implicit.wait", "0"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitSec));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        driverThread.set(driver);
    }

    /**
     * Inicializa AndroidDriver 
     */
    public static void initMobileDriver() {
        if (driverThread.get() != null) {
            return;
        }

        log.info("Inicializando AndroidDriver (Appium UiAutomator2) para Liverpool Pocket...");

        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName(ConfigReader.get("android.device.name", "emulator-5554"))
                .setAppPackage(ConfigReader.get("android.app.package", "mx.com.liverpool.shoppingapp"))
                .setAppActivity(ConfigReader.get("android.app.activity", "mx.com.liverpool.shoppingapp.splash.SplashActivity"))
                .setNoReset(Boolean.parseBoolean(ConfigReader.get("android.no.reset", "true")))
                .setAutoGrantPermissions(true)
                .setNewCommandTimeout(Duration.ofSeconds(120));

        String appiumServerUrl = ConfigReader.get("appium.server.url", "http://127.0.0.1:4723");

        try {
            AndroidDriver androidDriver = new AndroidDriver(URI.create(appiumServerUrl).toURL(), options);

            int implicitWaitSec = Integer.parseInt(ConfigReader.get("implicit.wait", "0"));
            androidDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitSec));

            driverThread.set(androidDriver);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL del servidor Appium inválida: " + appiumServerUrl, e);
        }
    }

    /**
     * Cierra la sesión activa y limpia el ThreadLocal
     */
    public static void quitDriver() {
        WebDriver driver = driverThread.get();
        if (driver != null) {
            try {
                log.info("Cerrando sesión del driver...");
                driver.quit();
            } catch (Exception e) {
                log.warn("Error al cerrar el driver: {}", e.getMessage());
            } finally {
                driverThread.remove();
            }
        }
    }
}