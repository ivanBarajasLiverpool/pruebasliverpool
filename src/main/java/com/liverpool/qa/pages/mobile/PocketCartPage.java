package com.liverpool.qa.pages.mobile;

import com.liverpool.qa.utils.PriceUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;

public class PocketCartPage extends BaseMobilePage {

    private final By bagHeaderTitle         = By.id("mx.com.liverpool.shoppingapp:id/txt_header_title");
    private final By productImageInBag      = By.xpath("//android.view.View[@content-desc='Imagen de red']");
    private final By productTitleInBag      = By.xpath("//android.view.View[@content-desc='Imagen de red']/parent::android.view.View/following-sibling::android.view.View[1]/android.widget.TextView[1]");
    private final By productFinalPriceInBag = By.xpath("(//android.view.View[@content-desc='Imagen de red']/parent::android.view.View/parent::android.view.View/android.widget.TextView[starts-with(@text, '$')])[last()]");
    private final By warrantyTitleInBag     = By.xpath("//android.widget.TextView[contains(@text, 'Plan ') or contains(@text, 'Garantía') or contains(@text, 'Protección')]");
    private final By warrantyPriceInBag     = By.xpath("//android.widget.TextView[contains(@text, 'Plan ') or contains(@text, 'Garantía') or contains(@text, 'Protección')]/parent::android.view.View/following-sibling::android.widget.TextView[starts-with(@text, '$')][1]");

    private final By subtotalPrice          = By.xpath("//android.widget.TextView[starts-with(@text, 'Subtotal')]/following-sibling::android.widget.TextView[1]");
    private final By discountPrice          = By.xpath("//android.widget.TextView[starts-with(@text, 'Descuento:')]/following-sibling::android.widget.TextView[1]");
    private final By totalWithIvaPrice      = By.xpath("//android.widget.TextView[starts-with(@text, 'Total (IVA incluido):')]/following-sibling::android.widget.TextView[1]");

    private final By deleteItemBtn          = By.xpath("//android.view.View[@content-desc='Eliminar']/parent::android.view.View");
    private final By confirmDeleteBtn       = By.xpath("//android.widget.TextView[@text='Aceptar']");

    public PocketCartPage(WebDriver driver) {
        super(driver);
    }

    @Step("Vaciar artículos existentes en Mi Bolsa")
    public void emptyBag() {
        waitForVisibility(bagHeaderTitle);
        int safetyCounter = 0;
        while (isVisibleNow(deleteItemBtn) && safetyCounter < 5) {
            click(deleteItemBtn);
            if (isDisplayed(confirmDeleteBtn)) {
                click(confirmDeleteBtn);
            }
            safetyCounter++;
        }
    }

    @Step("Verificar que el celular seleccionado está presente en Mi Bolsa")
    public boolean isProductPresentInBag() {
        waitForVisibility(bagHeaderTitle);
        return isDisplayed(productImageInBag);
    }

    @Step("Verificar que la garantía extra (Liverpool Care) está presente en Mi Bolsa")
    public boolean isWarrantyPresentInBag() {
        return isDisplayed(warrantyTitleInBag);
    }

    @Step("Obtener nombre del celular en Mi Bolsa")
    public String getProductTitleInBag() {
        return getText(productTitleInBag);
    }

    @Step("Obtener precio final del celular en Mi Bolsa")
    public BigDecimal getProductPriceInBag() {
        return PriceUtils.parsePrice(getText(productFinalPriceInBag));
    }

    @Step("Obtener nombre de la garantía extra en Mi Bolsa")
    public String getWarrantyTitleInBag() {
        return getText(warrantyTitleInBag);
    }

    @Step("Obtener precio de la garantía extra en Mi Bolsa")
    public BigDecimal getWarrantyPriceInBag() {
        return PriceUtils.parsePrice(getText(warrantyPriceInBag));
    }

    @Step("Desplazar hacia el resumen de pago en Mi Bolsa")
    public PocketCartPage scrollToPaymentSummary() {
        scrollDownUntilVisible(totalWithIvaPrice, 4);
        return this;
    }

    @Step("Obtener Subtotal en el resumen de Mi Bolsa")
    public BigDecimal getSubtotal() {
        scrollToPaymentSummary();
        return PriceUtils.parsePrice(getText(subtotalPrice));
    }

    @Step("Obtener Descuento en el resumen de Mi Bolsa (0.00 si no aplica)")
    public BigDecimal getDiscount() {
        if (isVisibleNow(discountPrice)) {
            return PriceUtils.parsePrice(getText(discountPrice));
        }
        return BigDecimal.ZERO.setScale(2);
    }

    @Step("Obtener Total (IVA incluido) en el resumen de Mi Bolsa")
    public BigDecimal getTotalWithIVA() {
        scrollToPaymentSummary();
        return PriceUtils.parsePrice(getText(totalWithIvaPrice));
    }
}
