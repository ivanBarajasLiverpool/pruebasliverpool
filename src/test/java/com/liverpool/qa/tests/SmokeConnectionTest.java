package com.liverpool.qa.tests;

import com.liverpool.qa.base.BaseTest;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SmokeConnectionTest extends BaseTest {

    @Test(description = "Smoke Test: Validar que Selenium abre Chrome en Liverpool Web")
    @Description("Verifica la inicialización de ChromeDriver y carga de la URL base de Liverpool")
    public void testSeleniumWebConnection() {
        String currentUrl = obtenerUrlActual();
        String title = getDriver().getTitle();
        System.out.println("✅ Selenium Web conectado. Título: " + title + " | URL: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("liverpool.com.mx"),
                "La URL cargada no corresponde a Liverpool: " + currentUrl);
    }

    @Test(description = "Smoke Test: Validar que Appium conecta con el emulador Android y Liverpool Pocket")
    @Description("Verifica la inicialización de AndroidDriver con UiAutomator2Options")
    public void testAppiumMobileConnection() {
        AndroidDriver androidDriver = (AndroidDriver) getDriver();
        String currentPackage = obtenerPaqueteAndroid(androidDriver);
        System.out.println("✅ Appium Mobile conectado. Paquete activo en Android: " + currentPackage);

        Assert.assertNotNull(androidDriver.getPageSource(), "No se pudo obtener el árbol XML de la app");
    }

    @Step("Obtener URL actual del navegador")
    private String obtenerUrlActual() {
        return getDriver().getCurrentUrl();
    }

    @Step("Obtener paquete activo en el dispositivo Android")
    private String obtenerPaqueteAndroid(AndroidDriver driver) {
        return driver.getCurrentPackage();
    }
}