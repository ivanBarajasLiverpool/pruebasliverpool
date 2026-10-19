package com.liverpool.qa.pages.mobile;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class PocketProductListPage extends BaseMobilePage {

    private final By plpComposeContainer   = By.id("mx.com.liverpool.shoppingapp:id/plpResultsLayoutCompose");
    private final By productCardImages     = By.xpath("//android.view.View[@content-desc='Product Image']");
    // Seleccionamos preferentemente un equipo Samsung Galaxy (elegible para garantía extendida Liverpool Care en la app)
    private final By preferredCellphoneCard = By.xpath("//android.widget.TextView[contains(@text, 'Galaxy') or contains(@text, 'SAMSUNG')]/parent::android.view.View[@clickable='true']");

    public PocketProductListPage(WebDriver driver) {
        super(driver);
    }

    @Step("Seleccionar el celular en la posición {position} (1-based) del listado")
    public PocketProductDetailPage selectProductByPosition(int position) {
        waitForVisibility(plpComposeContainer);
        waitForVisibility(productCardImages);
        clickAt(productCardImages, position - 1);
        return new PocketProductDetailPage(driver);
    }

    @Step("Seleccionar celular de preferencia del catálogo con cobertura de garantía Liverpool Care")
    public PocketProductDetailPage selectPreferredCellphone() {
        waitForVisibility(plpComposeContainer);
        waitForVisibility(productCardImages);
        scrollDownUntilVisible(preferredCellphoneCard, 3);
        click(preferredCellphoneCard);
        return new PocketProductDetailPage(driver);
    }
}
