package com.example.demo.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.dto.OrderDetailsDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.dto.PaymentDetails;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentRetrieveParams;
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

        public PaymentDetails getPaymentDetails(Session session) throws StripeException {
                String paymentIntentId = session.getPaymentIntent();
                if (paymentIntentId == null) {
                        return null;
                }
                PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId,
                                PaymentIntentRetrieveParams.builder().addExpand("latest_charge").build(), null);
                Charge charge = paymentIntent.getLatestChargeObject();
                if (charge == null || charge.getPaymentMethodDetails() == null) {
                        return new PaymentDetails(paymentIntentId, null, null, null, null, null);
                }
                Charge.PaymentMethodDetails methodDetails = charge.getPaymentMethodDetails();
                Charge.PaymentMethodDetails.Card card = methodDetails.getCard();
                if (card == null) {
                        return new PaymentDetails(paymentIntentId, methodDetails.getType(), null, null, null, null);
                }
                return new PaymentDetails(paymentIntentId, methodDetails.getType(), card.getBrand(), card.getLast4(),
                                toInteger(card.getExpMonth()), toInteger(card.getExpYear()));
        }

        private Integer toInteger(Long value) {
                return value == null ? null : Math.toIntExact(value);
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
