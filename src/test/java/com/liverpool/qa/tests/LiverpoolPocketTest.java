package com.liverpool.qa.tests;

import com.liverpool.qa.base.BaseTest;
import com.liverpool.qa.pages.mobile.PocketCartPage;
import com.liverpool.qa.pages.mobile.PocketHomePage;
import com.liverpool.qa.pages.mobile.PocketProductDetailPage;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.math.BigDecimal;

@Epic("Comercio Unificado - El Puerto de Liverpool")
@Feature("Flujo 3: Liverpool Pocket (Android) - Electrónica / Celulares")
public class LiverpoolPocketTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    @Parameters({"platform", "browser"})
    @Override
    public void setUp(@Optional String platform, @Optional String browser) {
        // Garantiza que al ejecutar LiverpoolPocketTest de forma individual o por suite use plataforma 'android'
        String mobilePlatform = (platform != null && !platform.isBlank()) ? platform : "android";
        super.setUp(mobilePlatform, browser);
    }

    @Test(description = "Certificar compra de Celular con garantía extra en Liverpool Pocket Android y validar Total con IVA")
    @Story("Navegar por categorías -> Electrónica -> Celulares -> Seleccionar celular -> Agregar garantía extra -> Validar Total con IVA en bolsa")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddCellphoneWithWarrantyAndValidateCartTotal() {
        PocketHomePage homePage = new PocketHomePage(getDriver());

        // 1. Navegar por Categorías -> Electrónica -> Celulares y seleccionar celular de preferencia
        PocketProductDetailPage pdp = homePage
                .navigateToCelulares()
                .selectPreferredCellphone();

        // 2. Agregar garantía extra (Liverpool Care), agregar a la bolsa y validar que se agregó correctamente
        pdp.addExtraWarranty()
           .addToBag();
        Assert.assertTrue(pdp.isProductAddedToBagBadgeVisible(),
                "El contador de la bolsa en el header de Liverpool Pocket no se actualizó tras agregar el celular con garantía.");

        // 3. Navegar a la bolsa y validar presencia del celular y de la garantía extra
        PocketCartPage cartPage = pdp.goToBag();
        Assert.assertTrue(cartPage.isProductPresentInBag(),
                "El celular seleccionado no se encontró dentro de Mi Bolsa en Liverpool Pocket.");
        Assert.assertTrue(cartPage.isWarrantyPresentInBag(),
                "La garantía extra seleccionada (Liverpool Care) no se encontró dentro de Mi Bolsa.");

        // 4. Obtener precios individuales del producto y la garantía en la bolsa
        String productName = cartPage.getProductTitleInBag();
        BigDecimal productPrice = cartPage.getProductPriceInBag();
        String warrantyName = cartPage.getWarrantyTitleInBag();
        BigDecimal warrantyPrice = cartPage.getWarrantyPriceInBag();

        // 5. Desplazar al resumen financiero y obtener Subtotal, Descuento y Total con IVA
        BigDecimal subtotal = cartPage.getSubtotal();
        BigDecimal discount = cartPage.getDiscount();
        BigDecimal actualTotalWithIVA = cartPage.getTotalWithIVA();

        BigDecimal expectedSumProductAndWarranty = productPrice.add(warrantyPrice);
        BigDecimal expectedSubtotalMinusDiscount = subtotal.subtract(discount);

        // Registrar parámetros detallados en el reporte de Allure
        Allure.parameter("Plataforma", "Liverpool Pocket - Android (Appium UiAutomator2)");
        Allure.parameter("Celular en Bolsa", productName);
        Allure.parameter("Precio Final Celular", "$" + productPrice);
        Allure.parameter("Garantía Extra", warrantyName);
        Allure.parameter("Precio Garantía Extra", "$" + warrantyPrice);
        Allure.parameter("Subtotal Resumen", "$" + subtotal);
        Allure.parameter("Descuento Resumen", "$" + discount);
        Allure.parameter("Total (IVA incluido)", "$" + actualTotalWithIVA);

        // 6. Validar que Precio del Producto + Precio de la Garantía == Total con IVA
        Assert.assertEquals(actualTotalWithIVA, expectedSumProductAndWarranty,
                "El Total con IVA en la bolsa no coincide con la suma del precio del celular más la garantía extra.");

        // 7. Validar cuadre contable del resumen (Subtotal - Descuento == Total con IVA)
        Assert.assertEquals(actualTotalWithIVA, expectedSubtotalMinusDiscount,
                "El Total con IVA en la bolsa no coincide con el cálculo del resumen (Subtotal - Descuento).");
    }
}
