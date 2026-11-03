package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.CheckoutFulfillmentService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final CheckoutFulfillmentService checkoutFulfillmentService;

    public PaymentController(CheckoutFulfillmentService checkoutFulfillmentService) {
        this.checkoutFulfillmentService = checkoutFulfillmentService;
    }

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    /**
     * Endpoint to receive secure event webhooks from Stripe
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        if (sigHeader == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Stripe-Signature header");
        }

        Event event;

        try {
            // Verify the webhook signature to ensure it came from Stripe
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed; check STRIPE_WEBHOOK_SECRET");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);
        if (stripeObject == null) {
            // Empty when the event's API version differs from the one stripe-java is pinned
            // to.
            try {
                stripeObject = dataObjectDeserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException e) {
                log.error("Could not deserialize Stripe event {} (API version {})", event.getId(),
                        event.getApiVersion(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Deserialization failed");
            }
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                checkoutFulfillmentService.fulfill((Session) stripeObject);
                break;

            case "payment_intent.payment_failed":
                log.info("Payment failed for event {}", event.getId());
                break;

            default:
                log.debug("Unhandled Stripe event type: {}", event.getType());
                break;
        }

        return ResponseEntity.ok("Success");
    }
}