package com.liverpool.qa.pages.base;

import com.liverpool.qa.config.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public abstract class BasePage {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final Actions actions;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        int explicitTimeout = Integer.parseInt(ConfigReader.get("timeout.explicit.seconds", "15"));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitTimeout));
        this.actions = new Actions(driver);
    }

    // =========================================================================
    // 1. WRAPPERS DE SINCRONIZACIÓN EXPLÍCITA (Explicit Waits)
    // =========================================================================

    /**
     * Espera hasta que el elemento esté presente en el DOM y sea visible en pantalla.
     */
    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Espera hasta que todos los elementos que coinciden con el localizador sean visibles.
     */
    protected List<WebElement> waitForAllVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    /**
     * Espera hasta que el elemento sea visible y esté habilitado para recibir clics.
     */
    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Espera a que un elemento (ej. spinner/loader o modal) desaparezca del DOM o deje de ser visible.
     */
    protected boolean waitForInvisibility(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Espera a que una referencia de elemento anterior quede obsoleta en el DOM.
     */
    protected boolean waitForStaleness(WebElement oldElement) {
        return wait.until(ExpectedConditions.stalenessOf(oldElement));
    }

    /**
     * Espera a que la URL actual contenga una subcadena específica.
     */
    protected boolean waitForUrlContains(String urlFraction) {
        return wait.until(ExpectedConditions.urlContains(urlFraction));
    }

    // =========================================================================
    // 2. ACCIONES BASE REUTILIZABLES 
    // =========================================================================

    /**
     * Hace clic seguro esperando primero a que el elemento sea clickeable.
     */
    protected void click(By locator) {
        try {
            waitForClickable(locator).click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Clic interceptado en {}. Haciendo scroll al centro y reintentando...", locator);
            WebElement element = waitForVisibility(locator);
            scrollIntoViewCenter(element);
            waitForClickable(locator).click();
        }
    }


    /**
     * Limpia el campo de texto y escribe el valor especificado.
     */
    protected void type(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Escribe texto y presiona ENTER.
     */
    protected void typeAndEnter(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        element.clear();
        element.sendKeys(text, Keys.ENTER);
    }

    /**
     * Obtiene el texto visible de un elemento sin espacios extremos.
     */
    protected String getText(By locator) {
        return waitForVisibility(locator).getText().trim();
    }

    /**
     * Verifica de forma segura si un elemento es visible dentro del tiempo de espera explícito.
     */
    protected boolean isDisplayed(By locator) {
        try {
            return waitForVisibility(locator).isDisplayed();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    // =========================================================================
    // 3. INTERACCIONES AVANZADAS (Hover, Scroll y Pestañas)
    // =========================================================================

    /**
     * Pasa el cursor sobre un elemento (necesario para abrir submenús como Categorías -> Línea Blanca).
     */
    protected void hoverOver(By locator) {
        WebElement element = waitForVisibility(locator);
        actions.moveToElement(element).perform();
    }

    /**
     * Desplaza la ventana para situar el elemento en el centro exacto del viewport,
     * evitando que el header fijo (sticky header) de Liverpool lo cubra.
     */
    protected void scrollIntoViewCenter(By locator) {
        WebElement element = waitForVisibility(locator);
        scrollIntoViewCenter(element);
    }

    protected void scrollIntoViewCenter(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'instant', block: 'center', inline: 'nearest'});",
                element
        );
    }

    /**
     * Cambia el foco del driver a una nueva ventana/pestaña cuando se abre otro sitio (ej. Banana Republic).
     */
    protected void switchToNewTabIfOpened(String originalHandle) {
        Set<String> allHandles = driver.getWindowHandles();
        if (allHandles.size() > 1) {
            for (String handle : allHandles) {
                if (!handle.equals(originalHandle)) {
                    log.info("Cambiando el foco a la nueva pestaña: {}", handle);
                    driver.switchTo().window(handle);
                    break;
                }
            }
        }
    }
}