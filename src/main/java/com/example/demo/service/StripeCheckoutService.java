package com.example.demo.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.dto.OrderDetailsDTO;
import com.example.demo.dto.OrderItemDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;

@Service
public class StripeCheckoutService {

        @Value("${stripe.secret.key}")
        private String stripeSecretKey;

        @PostConstruct
        public void init() {
                Stripe.apiKey = stripeSecretKey;
        }

        public String createCheckoutSessionUrl(OrderDetailsDTO order, BigDecimal salesTax, String baseUrl)
                        throws StripeException {
                String successUrl = baseUrl + "/cart/checkout/confirmation/" + order.getOrderId()
                                + "?session_id={CHECKOUT_SESSION_ID}";
                String cancelUrl = baseUrl + "/cart/checkout";

                SessionCreateParams.Builder params = SessionCreateParams.builder()
                                .putExtraParam("ui_mode", "hosted_page")
                                .setMode(SessionCreateParams.Mode.PAYMENT)
                                .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.AUTO)
                                .setPhoneNumberCollection(
                                                SessionCreateParams.PhoneNumberCollection.builder().setEnabled(false)
                                                                .build())
                                .setAutomaticTax(SessionCreateParams.AutomaticTax.builder().setEnabled(false).build())
                                .setAllowPromotionCodes(false)
                                .setSubmitType(SessionCreateParams.SubmitType.AUTO)
                                .putExtraParam("integration_identifier", "hosted_mobile_app_0001")
                                .putExtraParam("origin_context", "mobile_app")
                                .setClientReferenceId(order.getOrderId().toString())
                                .setSuccessUrl(successUrl)
                                .setCancelUrl(cancelUrl);

                for (OrderItemDTO item : order.getItems()) {
                        params.addLineItem(lineItem(item.getProductName(), item.getUnitPrice(), item.getQuantity()));
                }
                if (salesTax.signum() > 0) {
                        params.addLineItem(lineItem("Sales tax", salesTax, 1));
                }

                return Session.create(params.build()).getUrl();
        }

        private SessionCreateParams.LineItem lineItem(String name, BigDecimal unitPrice, long quantity) {
                return SessionCreateParams.LineItem.builder()
                                .setQuantity(quantity)
                                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(unitPrice.movePointRight(2).longValueExact())
                                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData
                                                                .builder()
                                                                .setName(name)
                                                                .build())
                                                .build())
                                .build();
        }
}
