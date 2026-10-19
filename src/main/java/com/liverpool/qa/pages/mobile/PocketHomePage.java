package com.liverpool.qa.pages.mobile;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PocketHomePage extends BaseMobilePage {

    // --- Selectores Barra Inferior y Header (Liverpool Pocket Android) ---
    private final By explorarTab               = By.xpath("//*[@content-desc='Explorar' or @text='Explorar']");
    private final By headerBackBtn             = By.id("mx.com.liverpool.shoppingapp:id/btn_header_back");
    private final By headerBagBtn              = By.id("mx.com.liverpool.shoppingapp:id/btn_header_bag");
    private final By headerBagBadge            = By.id("mx.com.liverpool.shoppingapp:id/badge");

    // --- Selectores Menú de Categorías -> Electrónica -> Celulares ---
    private final By categoriesGrid            = By.id("mx.com.liverpool.shoppingapp:id/lst_menu_items");
    private final By electronicaCategory       = By.xpath("//android.widget.TextView[@resource-id='mx.com.liverpool.shoppingapp:id/titleMenu' and @text='Electrónica']");
    private final By subcategoriesRecycler     = By.id("mx.com.liverpool.shoppingapp:id/recycler_drop_blp");
    private final By celularesYTelefoniaOption = By.xpath("//android.widget.TextView[@resource-id='mx.com.liverpool.shoppingapp:id/item_dropdown_child_textview' and contains(@text, 'Celulares')]");
    private final By celularesSubcategoryCell  = By.xpath("//android.widget.TextView[@resource-id='mx.com.liverpool.shoppingapp:id/clp_title_cell' and @text='Celulares']");

    public PocketHomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Espera a que termine el Splash inicial (hasta 35s en emulador) y asegura que la barra inferior ('Explorar') esté visible.
     */
    private void ensureOnMainNavigationScreen() {
        new WebDriverWait(driver, Duration.ofSeconds(35)).until(d ->
                isVisibleNow(explorarTab) || isVisibleNow(headerBackBtn)
        );
        int backAttempts = 0;
        while (!isVisibleNow(explorarTab) && isVisibleNow(headerBackBtn) && backAttempts < 5) {
            log.info("Regresando a la pantalla principal desde vista interna...");
            click(headerBackBtn);
            backAttempts++;
        }
        waitForVisibility(explorarTab);
    }

    @Step("Limpiar bolsa previamente si existen artículos residuales de ejecuciones anteriores")
    public PocketHomePage cleanBagIfNotEmpty() {
        ensureOnMainNavigationScreen();
        if (isVisibleNow(headerBagBadge)) {
            log.info("Se detectaron artículos previos en la bolsa. Limpiando bolsa antes de iniciar el flujo...");
            click(headerBagBtn);
            PocketCartPage cartPage = new PocketCartPage(driver);
            cartPage.emptyBag();
            navigateBackAndroid();
            ensureOnMainNavigationScreen();
        }
        return this;
    }

    @Step("Navegar al menú de Categorías ('Explorar')")
    public PocketHomePage openCategories() {
        click(explorarTab);
        waitForVisibility(categoriesGrid);
        return this;
    }

    @Step("Seleccionar la categoría 'Electrónica'")
    public PocketHomePage selectElectronica() {
        scrollDownUntilVisible(electronicaCategory, 3);
        click(electronicaCategory);
        waitForVisibility(subcategoriesRecycler);
        return this;
    }

    @Step("Seleccionar subcategoría 'Celulares y Telefonía' -> 'Celulares'")
    public PocketProductListPage selectCelulares() {
        scrollDownUntilVisible(celularesYTelefoniaOption, 3);
        click(celularesYTelefoniaOption);
        waitForVisibility(celularesSubcategoryCell);
        click(celularesSubcategoryCell);
        return new PocketProductListPage(driver);
    }

    @Step("Navegar por Categorías -> Electrónica -> Celulares en Liverpool Pocket")
    public PocketProductListPage navigateToCelulares() {
        return cleanBagIfNotEmpty()
                .openCategories()
                .selectElectronica()
                .selectCelulares();
    }
}
