package com.example.demo.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.dto.PaymentDetails;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

@Service
public class CheckoutFulfillmentService {
    private static final Logger log = LoggerFactory.getLogger(CheckoutFulfillmentService.class);

    private final CartService cartService;
    private final OrderService orderService;
    private final StripeCheckoutService stripeCheckoutService;

    public CheckoutFulfillmentService(CartService cartService, OrderService orderService,
            StripeCheckoutService stripeCheckoutService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.stripeCheckoutService = stripeCheckoutService;
    }

    // Called from the success redirect; the session is re-fetched from Stripe so
    // the query param can't be forged.
    public void fulfillSession(UUID orderId, String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            if (!orderId.toString().equals(session.getClientReferenceId())) {
                log.warn("Checkout session {} does not belong to order {}", sessionId, orderId);
                return;
            }
            fulfill(session);
        } catch (StripeException e) {
            log.error("Could not retrieve checkout session {}", sessionId, e);
        }
    }

    // Synchronized so the webhook and success redirect can't both decrement stock
    // for the same order.
    public synchronized void fulfill(Session session) {
        // Delayed payment methods complete the session before funds arrive.
        if (!"paid".equals(session.getPaymentStatus())) {
            log.info("Checkout session {} completed but not yet paid", session.getId());
            return;
        }
        String reference = session.getClientReferenceId();
        if (reference == null) {
            log.warn("Checkout session {} has no client_reference_id", session.getId());
            return;
        }
        UUID orderId;
        try {
            orderId = UUID.fromString(reference);
        } catch (IllegalArgumentException e) {
            log.warn("Checkout session {} has invalid client_reference_id {}", session.getId(), reference);
            return;
        }
        try {
            PaymentDetails paymentDetails = stripeCheckoutService.getPaymentDetails(session);
            if (paymentDetails != null) {
                orderService.recordPaymentDetails(orderId, paymentDetails);
            }
        } catch (StripeException e) {
            log.error("Could not retrieve payment details for checkout session {}", session.getId(), e);
        }
        int updated = cartService.completePaidOrder(orderId);
        log.info("Checkout session {} paid; marked {} order row(s) for order {} as Processing",
                session.getId(), updated, orderId);
    }
}
