package com.liverpool.qa.pages.mobile;

import com.liverpool.qa.pages.base.BasePage;
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
    // 1. SCROLL EN ANDROID 
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

    protected void swipeVertical(double startPercentY, double endPercentY) {
        Dimension size = androidDriver.manage().window().getSize();
        int centerX = size.width / 2;
        int startY = (int) (size.height * startPercentY);
        int endY = (int) (size.height * endPercentY);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        // 1. Posicionar el dedo en el punto inicial
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, startY));
        // 2. Presionar la pantalla
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        // 3. Deslizar hacia el punto final en 600ms
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), centerX, endY));
        // 4. Levantar el dedo
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        androidDriver.perform(Collections.singletonList(swipe));
    }

    /**
     * Verifica rápidamente (hasta 1.5 segundos) si el elemento ya es visible en pantalla tras una transición o swipe.
     */
    protected boolean isVisibleNow(By locator) {
        try {
            return new org.openqa.selenium.support.ui.WebDriverWait(androidDriver, Duration.ofMillis(1500))
                    .until(d -> {
                        for (WebElement el : d.findElements(locator)) {
                            try {
                                if (el.isDisplayed()) {
                                    return true;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        return null;
                    });
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hace scroll hacia abajo hasta que el localizador indicado sea visible
     * o se alcance el número máximo de intentos de swipe.
     */
    protected WebElement scrollDownUntilVisible1(By locator, int maxSwipes) {
        for (int i = 0; i < maxSwipes; i++) {
            if (isVisibleNow(locator)) {
                return waitForVisibility(locator);
            }
            log.info("Elemento {} aún no visible, ejecutando swipe hacia abajo {}/{}...", locator, i + 1, maxSwipes);
            swipeVertical(0.78, 0.28);
        }
        return waitForVisibility(locator);
    }

    /**
     * Hace scroll hacia arriba hasta que el localizador indicado sea visible.
     */
    protected WebElement scrollUpUntilVisible(By locator, int maxSwipes) {
        for (int i = 0; i < maxSwipes; i++) {
            if (isVisibleNow(locator)) {
                return waitForVisibility(locator);
            }
            log.info("Elemento {} aún no visible arriba, ejecutando swipe hacia arriba {}/{}...", locator, i + 1, maxSwipes);
            swipeVertical(0.30, 0.78);
        }
        return waitForVisibility(locator);
    }

    /**
     * Hace clic en el elemento visible que ocupa la posición zeroBasedIndex (0 = primero, 1 = segundo).
     */
    protected void clickAt(By locator, int zeroBasedIndex) {
        WebElement target = wait.until(d -> {
            int visibleIdx = 0;
            for (WebElement el : d.findElements(locator)) {
                try {
                    if (el.isDisplayed()) {
                        if (visibleIdx == zeroBasedIndex) {
                            return el;
                        }
                        visibleIdx++;
                    }
                } catch (Exception ignored) {
                }
            }
            return null;
        });
        target.click();
    }

    @Override
    protected void scrollIntoViewCenter(WebElement element) {
        // En contexto nativo Android (UiAutomator2) no se ejecuta window.scrollIntoView de JS
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
     * 
     * Presiona el botón 'Atrás' de Android.
     */
    protected void navigateBackAndroid() {
        androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
    }
}