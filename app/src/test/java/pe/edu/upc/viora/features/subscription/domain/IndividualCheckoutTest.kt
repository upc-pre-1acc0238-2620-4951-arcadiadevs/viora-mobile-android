package pe.edu.upc.viora.features.subscription.domain

import org.junit.Test
import org.junit.Assert.*
import pe.edu.upc.viora.features.subscription.domain.entity.isAllowedCheckoutUrl

class IndividualCheckoutTest {
    @Test fun `only exact https provider checkout locations are allowed`() {
        assertTrue(isAllowedCheckoutUrl("https://www.mercadopago.com.pe/checkout/v1/redirect?pref=1"))
        assertTrue(isAllowedCheckoutUrl("https://sandbox.mercadopago.com.pe/checkout/v1/redirect?pref=1"))
        listOf("http://www.mercadopago.com.pe/checkout/v1", "https://www.mercadopago.com.pe.evil.test/checkout/v1",
            "https://evil.test/checkout/v1", "https://evil@www.mercadopago.com.pe/checkout/v1",
            "https://www.mercadopago.com.pe:444/checkout/v1", "javascript:alert(1)",
            "https://www.mercadopago.com.pe/account").forEach { assertFalse(it, isAllowedCheckoutUrl(it)) }
    }
}
