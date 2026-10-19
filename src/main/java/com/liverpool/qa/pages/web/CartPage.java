package com.liverpool.qa.pages.web;

import com.liverpool.qa.pages.base.BasePage;
import com.liverpool.qa.utils.PriceUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;

public class CartPage extends BasePage {

    // --- Selectores 100% Compartidos (Liverpool & Banana Republic) ---
    private final By bagProductTitleLink = By.cssSelector("a[data-testid^='ml-card-product-mybag-'][data-testid$='-brand-link']");
    private final By subtotalPrice       = By.cssSelector("div[data-testid='checkout-payment-summary-subtotal'] span");
    private final By discountPrice       = By.cssSelector("div[data-testid='checkout-payment-summary-discount'] span");
    private final By totalWithIvaPrice   = By.cssSelector("div[data-testid='checkout-payment-summary-total'] span");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    @Step("Verificar si el producto está presente dentro de Mi Bolsa")
    public boolean isProductPresentInBag() {
        return isDisplayed(bagProductTitleLink);
    }

    @Step("Obtener nombre del producto agregado en Mi Bolsa")
    public String getProductTitleInBag() {
        return getText(bagProductTitleLink);
    }

    @Step("Obtener Subtotal en el resumen de compra")
    public BigDecimal getSubtotal() {
        return PriceUtils.parsePrice(getText(subtotalPrice));
    }

    @Step("Obtener Descuento en el resumen de compra (0.00 si no aplica)")
    public BigDecimal getDiscount() {
        if (isDisplayed(discountPrice)) {
            return PriceUtils.parsePrice(getText(discountPrice));
        }
        return BigDecimal.ZERO.setScale(2);
    }

    @Step("Obtener Total con IVA en el resumen de compra")
    public BigDecimal getTotalWithIVA() {
        return PriceUtils.parsePrice(getText(totalWithIvaPrice));
    }
}