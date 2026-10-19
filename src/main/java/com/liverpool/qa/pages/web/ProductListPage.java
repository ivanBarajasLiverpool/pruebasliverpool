package com.liverpool.qa.pages.web;

import com.liverpool.qa.pages.base.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProductListPage extends BasePage {

    // Selector CSS compartido para las imágenes principales de las tarjetas de producto (Liverpool y Banana Republic)
    private final By productCardImage = By.cssSelector("img[data-testid$='-image-slider-image-0']");

    public ProductListPage(WebDriver driver) {
        super(driver);
    }

    private By getPaginationLink(int pageNumber) {
        return By.xpath(String.format("//a[@data-testid='plp-page-pagination-link' and text()[normalize-space()='%d']]", pageNumber));
    }

    @Step("Navegar a la página {pageNumber} del listado de productos")
    public ProductListPage goToPage(int pageNumber) {
        WebElement firstProductBeforePaging = waitForVisibility(productCardImage);
        By targetPageLocator = getPaginationLink(pageNumber);

        scrollIntoViewCenter(targetPageLocator);
        click(targetPageLocator);

        // Espera explícita a que la grilla anterior se refresque en el DOM
        try {
            waitForStaleness(firstProductBeforePaging);
        } catch (Exception ignored) {
            log.debug("La grilla ya se actualizó a la página {}", pageNumber);
        }
        return this;
    }

    @Step("Seleccionar el producto número {position} (1-based) disponible en la pantalla")
    public ProductDetailPage selectProductByPosition(int position) {
        clickAt(productCardImage, position - 1);
        return new ProductDetailPage(driver);
    }

    @Step("Seleccionar el primer producto disponible del listado")
    public ProductDetailPage selectFirstProduct() {
        return selectProductByPosition(1);
    }

    @Step("Seleccionar el segundo producto disponible del listado")
    public ProductDetailPage selectSecondProduct() {
        return selectProductByPosition(2);
    }
}