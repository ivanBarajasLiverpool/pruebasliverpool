package main.java.com.liverpool.qa.pages.mobile;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

public abstract class BaseMobilePage extends BasePage {

    protected final AndroidDriver androidDriver;

    protected BaseMobilePage(WebDriver driver) {
        super(driver);
        if (!(driver instanceof AndroidDriver)) {
            throw new IllegalArgumentException(
                    "BaseMobilePage requiere una instancia de AndroidDriver, pero recibió: " + driver.getClass().getName()
            );
        }
        this.androidDriver = (AndroidDriver) driver;
    }

    // =========================================================================
    // 1. SCROLL NATIVO DE ANDROID 
    // =========================================================================

    /**
     * Desplaza automáticamente el contenedor scrollable hasta encontrar un elemento
     * que contenga el texto indicado y lo devuelve.
     * Ideal para buscar "Electrónica", "Celulares", "Garantía" o "Agregar a mi bolsa".
     */
    protected WebElement scrollToText(String visibleText) {
        log.info("Haciendo scroll nativo en Android hasta el texto: '{}'", visibleText);
        String uiAutomatorExpression = String.format(
                "new UiScrollable(new UiSelector().scrollable(true).instance(0))" +
                ".scrollIntoView(new UiSelector().textContains(\"%s\").instance(0))",
                visibleText
        );
        return waitForVisibility(AppiumBy.androidUIAutomator(uiAutomatorExpression));
    }

    /**
     * Hace scroll hasta encontrar el texto y ejecuta clic sobre él.
     */
    protected void scrollAndClickText(String visibleText) {
        scrollToText(visibleText).click();
    }


    /**
     * Hace scroll hacia abajo hasta que el localizador indicado sea visible
     * o se alcance el número máximo de intentos de swipe.
     */
    protected WebElement scrollDownUntilVisible(By locator, int maxSwipes) {
        for (int i = 0; i < maxSwipes; i++) {
            if (isDisplayed(locator)) {
                return waitForVisibility(locator);
            }
            log.info("Elemento {} aún no visible, ejecutando swipe {}/{}...", locator, i + 1, maxSwipes);
            swipeVertical(0.75, 0.30);
        }
        return waitForVisibility(locator);
    }

    // =========================================================================
    // 3. UTILIDADES DEL TECLADO Y NAVEGACIÓN ANDROID
    // =========================================================================

    /**
     * Oculta el teclado en pantalla si está abierto (evita que tape botones inferiores como 'Agregar a mi bolsa').
     */
    protected void hideKeyboardIfVisible() {
        try {
            if (androidDriver.isKeyboardShown()) {
                androidDriver.hideKeyboard();
            }
        } catch (Exception e) {
            log.debug("No fue necesario ocultar el teclado: {}", e.getMessage());
        }
    }

    /**
     * Presiona la tecla ENTER del teclado Android.
     */
    protected void pressAndroidEnter() {
        androidDriver.pressKey(new KeyEvent(AndroidKey.ENTER));
    }

    /**
     * Presiona el botón 'Atrás' de Android.
     */
    protected void navigateBackAndroid() {
        androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
    }
}