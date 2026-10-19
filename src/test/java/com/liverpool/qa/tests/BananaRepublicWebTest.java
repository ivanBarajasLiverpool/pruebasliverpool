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
@Feature("Flujo 2: Banana Republic Web - Click & Collect")
public class BananaRepublicWebTest extends BaseTest {

    @Test(description = "Certificar flujo en Banana Republic con Click & Collect y validar Total con IVA en bolsa")
    @Story("Cambio de sitio a Banana Republic, búsqueda, Click & Collect y cuadre de precios en bolsa")
    @Severity(SeverityLevel.CRITICAL)
    public void testBananaRepublicClickAndCollectCartTotal() {
        HomePage homePage = new HomePage(getDriver());

        // 1. Ir a Otros sitios -> Banana Republic -> Buscar producto -> Seleccionar primero
        ProductDetailPage pdp = homePage
                .navigateToBananaRepublic()
                .searchProduct("Camisa")
                .selectFirstProduct();

        // 2. Seleccionar Click & Collect, buscar tienda por C.P. y agregar a la bolsa
        pdp.selectClickAndCollectStore("01000")
           .addToBag();

        Assert.assertTrue(pdp.isProductAddedToBagBadgeVisible(),
                "El badge de cantidad en Mi Bolsa no apareció tras agregar el producto en Banana Republic.");

        // 3. Navegar a Mi Bolsa y validar presencia del producto
        CartPage cartPage = pdp.goToBag();
        Assert.assertTrue(cartPage.isProductPresentInBag(),
                "El producto de Banana Republic no aparece listado en Mi Bolsa.");

        String productName = cartPage.getProductTitleInBag();
        BigDecimal subtotal = cartPage.getSubtotal();
        BigDecimal discount = cartPage.getDiscount();
        BigDecimal actualTotal = cartPage.getTotalWithIVA();
        BigDecimal expectedTotal = subtotal.subtract(discount);

        // Registrar métricas de negocio en el reporte de Allure
        Allure.parameter("Sitio", "Banana Republic");
        Allure.parameter("Producto en Bolsa", productName);
        Allure.parameter("Subtotal", "$" + subtotal);
        Allure.parameter("Descuento", "$" + discount);
        Allure.parameter("Total con IVA", "$" + actualTotal);

        // 4. Validar que Subtotal - Descuento == Total con IVA
        Assert.assertEquals(actualTotal, expectedTotal,
                "El Total con IVA en Banana Republic no coincide con el cálculo (Subtotal - Descuento).");
    }
}