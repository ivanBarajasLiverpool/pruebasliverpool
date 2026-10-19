package com.liverpool.qa.pages.mobile;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class PocketProductDetailPage extends BaseMobilePage {

    private final By pdpComposeContainer = By.id("mx.com.liverpool.shoppingapp:id/pdpLayoutCompose");
    private final By productCodeLabel    = By.xpath("//android.widget.TextView[starts-with(@text, 'Código de producto:')]");
    private final By extraWarrantyOption = By.xpath("//android.widget.TextView[contains(@text, 'Plan Robo') or contains(@text, 'Plan Solo Robo')]/parent::android.view.View");
    private final By addToBagBtn         = By.xpath("//android.widget.TextView[@text='Agregar a mi bolsa']/parent::android.view.View");
    private final By headerBagBadge      = By.id("mx.com.liverpool.shoppingapp:id/badge");
    private final By headerBagBtn        = By.id("mx.com.liverpool.shoppingapp:id/btn_header_bag");

    public PocketProductDetailPage(WebDriver driver) {
        super(driver);
    }

    @Step("Seleccionar garantía extra (Liverpool Care - Plan Robo + Daño) para el celular")
    public PocketProductDetailPage addExtraWarranty() {
        waitForVisibility(pdpComposeContainer);
        waitForVisibility(productCodeLabel);
        scrollDownUntilVisible(extraWarrantyOption, 6);
        click(extraWarrantyOption);
        return this;
    }

    @Step("Hacer clic en 'Agregar a mi bolsa'")
    public PocketProductDetailPage addToBag() {
        if (!isVisibleNow(addToBagBtn)) {
            scrollUpUntilVisible(addToBagBtn, 3);
        }
        click(addToBagBtn);
        return this;
    }

    @Step("Validar que el contador de la bolsa en el header confirme que el producto se agregó")
    public boolean isProductAddedToBagBadgeVisible() {
        return isDisplayed(headerBagBadge);
    }

    @Step("Navegar a 'Mi bolsa' desde el ícono del header")
    public PocketCartPage goToBag() {
        click(headerBagBtn);
        return new PocketCartPage(driver);
    }
}
