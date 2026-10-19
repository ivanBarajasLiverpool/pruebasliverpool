package com.liverpool.qa.pages.web;

import com.liverpool.qa.pages.base.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

    // --- Selectores Menú y Categorías (Compatibles con Pantalla Completa y Responsive) ---
    private final By categoriesMenuBtn      = By.xpath("//button[contains(@data-testid, '-header-button-category')] | //span[normalize-space()='Categorías'] | //span[contains(@data-testid, '-header-icon-menu')]");
    private final By lineaBlancaOption      = By.xpath("//*[normalize-space()='Línea Blanca y Electrodomésticos']");
    private final By lavadoYSecadoLink      = By.xpath("//a[contains(@href, '/tienda/lavado-y-secado') or contains(normalize-space(), 'Lavado y Secado')]");
    private final By lavadorasYSecadorasImg = By.cssSelector("img[data-testid='Lavadoras y secadoras-image'], img[alt='Lavadoras y secadoras']");

    // --- Selectores Otros Sitios y Buscador (Flujo 2 - Banana Republic) ---
    private final By otrosSitiosBtn         = By.xpath("//button[contains(@data-testid, '-header-header-nav-top-link-1-button')] | //*[normalize-space()='Otros sitios']");
    private final By bananaRepublicMenuLink = By.xpath("//a[contains(@data-testid, 'Banana Republic') or contains(@href, 'bananarepublic.com.mx') or .//*[normalize-space()='Banana Republic']]");
    private final By searchInput            = By.cssSelector("input[data-testid$='-header-search-input'], input[placeholder*='Buscar']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Step("Abrir menú de Categorías")
    public HomePage openHeaderMenu() {
        click(categoriesMenuBtn);
        return this;
    }

    @Step("Navegar por Categorías -> Línea Blanca y Electrodomésticos -> Lavado y Secado -> Lavadoras y secadoras")
    public ProductListPage navigateToLavadoras() {
        openHeaderMenu();
        // En pantalla completa, al pasar el cursor o hacer clic en Línea Blanca se despliega Lavado y Secado
        hoverOver(lineaBlancaOption);
        click(lineaBlancaOption);
        click(lavadoYSecadoLink);
        if (isDisplayed(lavadorasYSecadorasImg)) {
            click(lavadorasYSecadorasImg);
        }
        return new ProductListPage(driver);
    }

    @Step("Navegar a 'Otros sitios' -> 'Banana Republic'")
    public HomePage navigateToBananaRepublic() {
        String originalWindow = driver.getWindowHandle();
        // Si 'Otros sitios' no está visible directamente en la barra superior, abrimos primero el menú
        if (!isDisplayed(otrosSitiosBtn)) {
            openHeaderMenu();
        }
        hoverOver(otrosSitiosBtn);
        click(otrosSitiosBtn);
        try {
            click(bananaRepublicMenuLink);
        } catch (Exception e) {
            jsClick(bananaRepublicMenuLink);
        }
        switchToNewTabIfOpened(originalWindow);
        waitForUrlContains("bananarepublic");
        return this;
    }

    @Step("Buscar el producto '{searchTerm}' en la barra de búsqueda")
    public ProductListPage searchProduct(String searchTerm) {
        typeAndEnter(searchInput, searchTerm);
        return new ProductListPage(driver);
    }
}