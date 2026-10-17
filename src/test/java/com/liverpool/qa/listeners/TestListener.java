package com.liverpool.qa.listeners;

import com.liverpool.qa.drivers.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        log.info("=== Iniciando Suite de Pruebas: {} ===", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info(">>> Iniciando Test: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("<<< Test EXITOSO: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        log.error("<<< Test FALLIDO: {} | Causa: {}", testName, result.getThrowable().getMessage());

        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            // 1. Adjunta captura de pantalla en PNG al reporte de Allure (Web o Mobile)
            attachScreenshotToAllure(testName, driver);

            // 2. Adjunta contexto adicional (URL actual si es Web, o Activity actual si es Android)
            attachExecutionContext(driver);
        } else {
            log.warn("No se pudo tomar captura de pantalla porque el WebDriver es null.");
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("<<< Test OMITIDO (Skipped): {}", result.getMethod().getMethodName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("=== Finalizando Suite de Pruebas: {} ===", context.getName());
    }

    // =========================================================================
    // MÉTODOS INTERCEPTADOS POR ALLURE + ASPECTJ (@Attachment)
    // =========================================================================

    /**
     * Toma la captura en memoria como arreglo de bytes y la incrusta en el reporte de Allure.
     */
    @Attachment(value = "Screenshot en Fallo - {testName}", type = "image/png")
    public byte[] attachScreenshotToAllure(String testName, WebDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.error("Error al capturar pantalla para Allure: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Adjunta un bloque de texto en Allure indicando en qué URL o pantalla de Android ocurrió el fallo.
     */
    @Attachment(value = "Contexto donde ocurrió el fallo", type = "text/plain")
    public String attachExecutionContext(WebDriver driver) {
        try {
            if (driver instanceof AndroidDriver androidDriver) {
                return "Plataforma: Android (Appium)\n"
                        + "Paquete: " + androidDriver.getCurrentPackage() + "\n"
                        + "Activity: " + androidDriver.currentActivity();
            } else {
                return "Plataforma: Web (Selenium)\n"
                        + "Título: " + driver.getTitle() + "\n"
                        + "URL actual: " + driver.getCurrentUrl();
            }
        } catch (Exception e) {
            return "No se pudo obtener el contexto de ejecución: " + e.getMessage();
        }
    }
}