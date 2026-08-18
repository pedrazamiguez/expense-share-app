package es.pedrazamiguez.splittrip.features.expense.presentation.extensions

import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BrandAlipay
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BrandBebo
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BrandPaypal
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BrandPaypay
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BrandVivaldi
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BrandWechat
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BuildingBank
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.CashBanknote
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.CreditCard
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Qrcode
import es.pedrazamiguez.splittrip.domain.enums.PaymentMethod
import es.pedrazamiguez.splittrip.features.expense.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PaymentMethodExtensionsTest {

    @Nested
    inner class ToStringRes {

        @Test
        fun `CASH maps to cash string resource`() {
            assertEquals(R.string.payment_method_cash, PaymentMethod.CASH.toStringRes())
        }

        @Test
        fun `BIZUM maps to bizum string resource`() {
            assertEquals(R.string.payment_method_bizum, PaymentMethod.BIZUM.toStringRes())
        }

        @Test
        fun `PIX maps to pix string resource`() {
            assertEquals(R.string.payment_method_pix, PaymentMethod.PIX.toStringRes())
        }

        @Test
        fun `CREDIT_CARD maps to credit card string resource`() {
            assertEquals(R.string.payment_method_credit_card, PaymentMethod.CREDIT_CARD.toStringRes())
        }

        @Test
        fun `DEBIT_CARD maps to debit card string resource`() {
            assertEquals(R.string.payment_method_debit_card, PaymentMethod.DEBIT_CARD.toStringRes())
        }

        @Test
        fun `BANK_TRANSFER maps to bank transfer string resource`() {
            assertEquals(R.string.payment_method_bank_transfer, PaymentMethod.BANK_TRANSFER.toStringRes())
        }

        @Test
        fun `PAYPAL maps to paypal string resource`() {
            assertEquals(R.string.payment_method_paypal, PaymentMethod.PAYPAL.toStringRes())
        }

        @Test
        fun `VENMO maps to venmo string resource`() {
            assertEquals(R.string.payment_method_venmo, PaymentMethod.VENMO.toStringRes())
        }

        @Test
        fun `ALIPAY maps to alipay string resource`() {
            assertEquals(R.string.payment_method_alipay, PaymentMethod.ALIPAY.toStringRes())
        }

        @Test
        fun `WECHAT_PAY maps to wechat pay string resource`() {
            assertEquals(R.string.payment_method_wechat_pay, PaymentMethod.WECHAT_PAY.toStringRes())
        }

        @Test
        fun `OTHER maps to other string resource`() {
            assertEquals(R.string.payment_method_other, PaymentMethod.OTHER.toStringRes())
        }

        @Test
        fun `all payment methods map to distinct string resources`() {
            val resIds = PaymentMethod.entries.map { it.toStringRes() }
            assertEquals(resIds.size, resIds.toSet().size)
        }
    }

    @Nested
    inner class ToIconVector {

        @Test
        fun `CASH maps to CashBanknote icon`() {
            assertSame(TablerIcons.Outline.CashBanknote, PaymentMethod.CASH.toIconVector())
        }

        @Test
        fun `BIZUM maps to BrandBebo icon`() {
            assertSame(TablerIcons.Outline.BrandBebo, PaymentMethod.BIZUM.toIconVector())
        }

        @Test
        fun `PIX maps to BrandPaypay icon`() {
            assertSame(TablerIcons.Outline.BrandPaypay, PaymentMethod.PIX.toIconVector())
        }

        @Test
        fun `CREDIT_CARD maps to CreditCard icon`() {
            assertSame(TablerIcons.Outline.CreditCard, PaymentMethod.CREDIT_CARD.toIconVector())
        }

        @Test
        fun `DEBIT_CARD maps to CreditCard icon`() {
            assertSame(TablerIcons.Outline.CreditCard, PaymentMethod.DEBIT_CARD.toIconVector())
        }

        @Test
        fun `BANK_TRANSFER maps to BuildingBank icon`() {
            assertSame(TablerIcons.Outline.BuildingBank, PaymentMethod.BANK_TRANSFER.toIconVector())
        }

        @Test
        fun `PAYPAL maps to BrandPaypal icon`() {
            assertSame(TablerIcons.Outline.BrandPaypal, PaymentMethod.PAYPAL.toIconVector())
        }

        @Test
        fun `VENMO maps to BrandVivaldi icon`() {
            assertSame(TablerIcons.Outline.BrandVivaldi, PaymentMethod.VENMO.toIconVector())
        }

        @Test
        fun `ALIPAY maps to BrandAlipay icon`() {
            assertSame(TablerIcons.Outline.BrandAlipay, PaymentMethod.ALIPAY.toIconVector())
        }

        @Test
        fun `WECHAT_PAY maps to BrandWechat icon`() {
            assertSame(TablerIcons.Outline.BrandWechat, PaymentMethod.WECHAT_PAY.toIconVector())
        }

        @Test
        fun `OTHER maps to Qrcode icon`() {
            assertSame(TablerIcons.Outline.Qrcode, PaymentMethod.OTHER.toIconVector())
        }

        @Test
        fun `all payment methods return non-null icon vectors`() {
            PaymentMethod.entries.forEach { method ->
                assertNotNull(method.toIconVector(), "Icon for $method should not be null")
            }
        }

        @Test
        fun `icon vectors are cached and return same reference on repeated calls`() {
            assertSame(
                PaymentMethod.BIZUM.toIconVector(),
                PaymentMethod.BIZUM.toIconVector()
            )
        }
    }
}
