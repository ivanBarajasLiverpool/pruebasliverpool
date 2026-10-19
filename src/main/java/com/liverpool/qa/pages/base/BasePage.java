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
     * Espera hasta que al menos un elemento que coincida con el localizador sea visible
     * y devuelve la instancia visible (resuelve elementos duplicados mobile/desktop en el DOM).
     */
        protected WebElement waitForVisibility(By locator) {
            return wait.until(d -> {
                for (WebElement el : d.findElements(locator)) {
                    try {
                        if (el.isDisplayed()) {
                            return el;
                        }
                    } catch (StaleElementReferenceException ignored) {
                    }
                }
                return null;
            });
        }
    
        /**
         * Espera hasta que exista una instancia visible y habilitada del localizador.
         */
        protected WebElement waitForClickable(By locator) {
            return wait.until(d -> {
                for (WebElement el : d.findElements(locator)) {
                    try {
                        if (el.isDisplayed() && el.isEnabled()) {
                            return el;
                        }
                    } catch (StaleElementReferenceException ignored) {
                    }
                }
                return null;
            });
        }

        /**
         * Espera hasta que existan suficientes elementos visibles y devuelve el que ocupa
         * la posición zeroBasedIndex (0 = primero en pantalla, 1 = segundo en pantalla, etc.).
         */
        protected WebElement waitForVisibleElementAt(By locator, int zeroBasedIndex) {
            return wait.until(d -> {
                int visibleIndex = 0;
                for (WebElement el : d.findElements(locator)) {
                    try {
                        if (el.isDisplayed()) {
                            if (visibleIndex == zeroBasedIndex) {
                                return el;
                            }
                            visibleIndex++;
                        }
                    } catch (StaleElementReferenceException ignored) {
                    }
                }
                return null;
            });
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

    /**
     * Espera a que el elemento exista en el DOM (incluso si tiene opacity: 0 como los inputs de Material UI).
     */
    protected WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // =========================================================================
    // 2. ACCIONES BASE REUTILIZABLES 
    // =========================================================================

    /**
     * Hace clic seguro esperando primero a que el elemento sea clickeable.
     * Si un backdrop en transición o header fijo intercepta el clic, centra el elemento y aplica fallback JS.
     */
    protected void click(By locator) {
        try {
            WebElement element = waitForClickable(locator);
            scrollIntoViewCenter(element);
            element.click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Clic interceptado en {}. Esperando cierre de posibles modales y reintentando...", locator);
            dismissActiveBackdropIfPresent();
            WebElement element = waitForVisibility(locator);
            scrollIntoViewCenter(element);
            try {
                element.click();
            } catch (ElementClickInterceptedException ex) {
                log.warn("Segundo intento interceptado en {}. Ejecutando jsClick de respaldo.", locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }
        }
    }

    /**
     * Hace clic en el elemento visible que ocupa la posición zeroBasedIndex (0 = primero, 1 = segundo).
     */
    protected void clickAt(By locator, int zeroBasedIndex) {
        WebElement element = waitForVisibleElementAt(locator, zeroBasedIndex);
        scrollIntoViewCenter(element);
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            dismissActiveBackdropIfPresent();
            scrollIntoViewCenter(element);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Fuerza un clic mediante JavaScript (ideal para inputs radio/checkbox con opacity: 0 en Material UI).
     */
    protected void jsClick(By locator) {
        WebElement element = waitForPresence(locator);
        scrollIntoViewCenter(element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Si un backdrop de Material UI (MuiBackdrop-root) quedó abierto sobre la página,
     * envía ESCAPE o hace clic en el botón de cierre del drawer y espera a que desaparezca.
     */
    protected void dismissActiveBackdropIfPresent() {
        By backdropLocator = By.cssSelector("div.MuiBackdrop-root, div.MuiModal-backdrop");
        try {
            List<WebElement> backdrops = driver.findElements(backdropLocator);
            if (!backdrops.isEmpty() && backdrops.get(0).isDisplayed()) {
                actions.sendKeys(Keys.ESCAPE).perform();
                new WebDriverWait(driver, Duration.ofSeconds(4))
                        .until(ExpectedConditions.invisibilityOfElementLocated(backdropLocator));
            }
        } catch (Exception ignored) {
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