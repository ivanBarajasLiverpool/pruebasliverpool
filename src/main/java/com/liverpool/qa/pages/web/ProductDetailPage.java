package com.liverpool.qa.pages.web;

import com.liverpool.qa.pages.base.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProductDetailPage extends BasePage {

    // --- Selectores Tallas y Click & Collect (Flujo 2) ---
    private final By availableSizeLabels  = By.cssSelector("div[data-testid^='ml-radio-group-size-picker-'] label");
    private final By clickAndCollectBtn   = By.cssSelector("button[data-testid='product-configurator-delivery-selection-card-Click & Collect']");
    private final By selectStoreBtn       = By.cssSelector("button[data-testid='product-configurator-delivery-Click & Collect-option-subselection-button']");
    private final By storePostalCodeInput = By.xpath("//label[contains(normalize-space(), 'C.P Ciudad o Tienda')]/parent::*//input");
    private final By firstStoreRadioSpan  = By.xpath("(//input[@type='radio' and @name='radio-buttons-group']/parent::*)[1]");
    private final By firstStoreRadioInput = By.cssSelector("input[type='radio'][name='radio-buttons-group']");
    private final By drawerCloseBtn       = By.xpath("//div[contains(@class,'MuiDrawer') or contains(@class,'MuiModal')]//button[.//span[contains(normalize-space(),'close')]]");
    private final By modalBackdrop        = By.cssSelector("div.MuiBackdrop-root, div.MuiModal-backdrop");

    // --- Selectores Agregar a la Bolsa, Garantía/Protección y Header (Compartidos Liverpool & Banana Republic) ---
    private final By addToBagBtn             = By.cssSelector("button[data-testid='add-to-bag-button']:not([disabled])");
    private final By addProtectionBtn        = By.cssSelector("button[data-testid*='protection'], button[data-testid*='warranty'], div[role='dialog'] button.bg-cta-primary, div.MuiModal-root button.bg-cta-primary, div.MuiDrawer-root button.bg-cta-primary");
    private final By headerCartQuantityBadge = By.cssSelector("div[data-testid$='-header-shopping-cart-header-cart-quantity']");
    private final By headerBagLink           = By.cssSelector("a[data-testid$='-header-shopping-cart-shopping-link']");
    private final By headerBagIcon           = By.xpath("//span[contains(@class, 'material-icons') and normalize-space()='shopping_bag']");

    public ProductDetailPage(WebDriver driver) {
        super(driver);
    }

    @Step("Configurar entrega Click & Collect buscando con C.P./Tienda '{postalCodeOrCity}' y seleccionar la primera tienda")
    public ProductDetailPage selectClickAndCollectStore(String postalCodeOrCity) {
        // Si el producto tiene selector de tallas y ninguna está marcada, selecciona la primera disponible
        if (isDisplayed(availableSizeLabels)) {
            click(availableSizeLabels);
        }

        scrollIntoViewCenter(clickAndCollectBtn);
        click(clickAndCollectBtn);

        if (isDisplayed(selectStoreBtn)) {
            click(selectStoreBtn);
        }

        typeAndEnter(storePostalCodeInput, postalCodeOrCity);

        // Esperar a que cargue el primer radio button de resultados de tiendas y seleccionarlo
        waitForPresence(firstStoreRadioInput);
        try {
            click(firstStoreRadioSpan);
        } catch (Exception e) {
            jsClick(firstStoreRadioInput);
        }

        // Asegurar que el drawer/modal 'Elige una tienda' se cierre completamente antes de continuar
        try {
            if (isDisplayed(drawerCloseBtn)) {
                click(drawerCloseBtn);
            }
            dismissActiveBackdropIfPresent();
        } catch (Exception ignored) {
        }
        return this;
    }

    @Step("Hacer clic en 'Agregar a mi bolsa'")
    public ProductDetailPage addToBag() {
        dismissActiveBackdropIfPresent();
        scrollIntoViewCenter(addToBagBtn);
        click(addToBagBtn);
        return this;
    }

    @Step("Hacer clic en el botón 'Agregar protección' tras seleccionar la garantía")
    public ProductDetailPage clickAddProtection() {
        scrollIntoViewCenter(addProtectionBtn);
        click(addProtectionBtn);
        return this;
    }

    @Step("Validar que el contador de la bolsa en el header muestre al menos 1 producto")
    public boolean isProductAddedToBagBadgeVisible() {
        return isDisplayed(headerCartQuantityBadge);
    }

    @Step("Navegar a 'Mi Bolsa' desde el ícono del header")
    public CartPage goToBag() {
        dismissActiveBackdropIfPresent();
        if (isDisplayed(headerCartQuantityBadge)) {
            click(headerCartQuantityBadge);
        } else if (isDisplayed(headerBagLink)) {
            click(headerBagLink);
        } else {
            click(headerBagIcon);
        }
        return new CartPage(driver);
    }
}