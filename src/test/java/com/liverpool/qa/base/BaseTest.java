package com.liverpool.qa.base;

import com.liverpool.qa.config.ConfigReader;
import com.liverpool.qa.drivers.DriverManager;
import com.liverpool.qa.listeners.TestListener;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

@Listeners(TestListener.class)
public abstract class BaseTest implements IHookable {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final TestListener testListener = new TestListener();

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    /**
     * Intercepta la ejecución del @Test mientras el contexto de Allure sigue abierto.
     * Si el test lanza excepción o falla un Assert, adjunta el screenshot inmediatamente.
     */
    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        callBack.runTestMethod(testResult);
        if (testResult.getThrowable() != null && getDriver() != null) {
            String testName = testResult.getMethod().getMethodName();
            testListener.attachScreenshotToAllure(testName, getDriver());
            testListener.attachExecutionContext(getDriver());
        }
    }

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

    @AfterMethod(alwaysRun = true)
    @Step("Cerrar sesión del driver y liberar recursos")
    public void tearDown() {
        DriverManager.quitDriver();
    }
}