package com.liverpool.qa.tests;

import com.liverpool.qa.base.BaseTest;
import com.liverpool.qa.pages.web.CartPage;
import com.liverpool.qa.pages.web.HomePage;
import com.liverpool.qa.pages.web.ProductDetailPage;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

@Epic("Comercio Unificado - El Puerto de Liverpool")
@Feature("Flujo 1: Liverpool Web - Línea Blanca")
public class LiverpoolWebTest extends BaseTest {

    @Test(description = "Certificar compra de Lavadora en página 4 y validar precios con Total con IVA en bolsa")
    @Story("Navegación por categorías, paginación pág. 4, agregado a bolsa y cuadre financiero")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddWasherFromPageFourAndValidateCartTotal() {
        HomePage homePage = new HomePage(getDriver());

        // 1. Navegar Categorías -> Línea Blanca -> Lavado y Secado -> Lavadoras -> Pág. 4 -> Segundo producto
        ProductDetailPage pdp = homePage
                .navigateToLavadoras()
                .goToPage(4)
                .selectSecondProduct();

        // 2. Agregar a la bolsa, hacer clic en 'Agregar protección' y validar indicador en el header
        pdp.addToBag()
           .clickAddProtection();
        Assert.assertTrue(pdp.isProductAddedToBagBadgeVisible(),
                "El contador de la bolsa en el header no se actualizó tras agregar la lavadora.");

        // 3. Ir a Mi Bolsa y validar que el producto aparece listado
        CartPage cartPage = pdp.goToBag();
        Assert.assertTrue(cartPage.isProductPresentInBag(),
                "El enlace del producto agregado no se encontró dentro de Mi Bolsa.");

        String productName = cartPage.getProductTitleInBag();
        BigDecimal subtotal = cartPage.getSubtotal();
        BigDecimal discount = cartPage.getDiscount();
        BigDecimal actualTotal = cartPage.getTotalWithIVA();
        BigDecimal expectedTotal = subtotal.subtract(discount);

        // Registrar métricas de negocio en el reporte de Allure
        Allure.parameter("Sitio", "Liverpool Web");
        Allure.parameter("Producto en Bolsa", productName);
        Allure.parameter("Subtotal", "$" + subtotal);
        Allure.parameter("Descuento", "$" + discount);
        Allure.parameter("Total con IVA", "$" + actualTotal);

        // 4. Validar que Subtotal - Descuento == Total con IVA
        Assert.assertEquals(actualTotal, expectedTotal,
                "El Total con IVA en la bolsa no coincide con el cálculo (Subtotal - Descuento).");
    }
}