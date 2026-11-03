package com.example.demo.controller;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.easypost.exception.EasyPostException;
import com.example.demo.dto.ShipmentCreateFormDTO;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.service.ShippingService;
import com.example.demo.service.UserService;
import com.example.demo.service.VendorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/shipping")
public class ShippingViewController {

    private final ShippingService shippingService;
    private final UserService userService;
    private final VendorService vendorService;

    public ShippingViewController(ShippingService shippingService, UserService userService,
            VendorService vendorService) {
        this.shippingService = shippingService;
        this.userService = userService;
        this.vendorService = vendorService;
    }

    // DISPLAY SHIPMENT FORM
    @GetMapping("/shipments/new/{orderId}")
    public String showCreateShipmentForm(@PathVariable("orderId") UUID orderId, Model model) {
        ShipmentCreateFormDTO shipmentForm = new ShipmentCreateFormDTO();
        shipmentForm.setReference(orderId.toString());
        model.addAttribute("shipmentForm", shipmentForm);
        return "shipment-create-form";
    }

    @PostMapping("/shipments")
    public String createShipment(@Valid @ModelAttribute("shipmentForm") ShipmentCreateFormDTO shipmentForm,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "shipment-create-form";
        }
        try {
            model.addAttribute("shipmentRates", shippingService.createShipment(shipmentForm));
            return "shipment-rates";
        } catch (EasyPostException e) {
            bindingResult.reject("easypost", e.getMessage());
            return "shipment-create-form";
        }
    }

    @PostMapping("/shipments/{shipmentId}/buy")
    public String buyShipment(@PathVariable String shipmentId, @RequestParam String rateId,
            Authentication authentication, Model model) {
        try {
            shippingService.buyShipment(currentVendorId(authentication), shipmentId, rateId);
        } catch (EasyPostException e) {
            model.addAttribute("error", e.getMessage());
            return "shipment-label";
        } catch (ResponseStatusException e) {
            model.addAttribute("error", e.getReason());
            return "shipment-label";
        }
        // Redirect so a refresh re-reads the label instead of re-submitting the
        // purchase
        return "redirect:/shipping/shipments/" + shipmentId + "/label";
    }

    @GetMapping("/shipments/{shipmentId}/label")
    public String showShipmentDetails(@PathVariable("shipmentId") String shipmentId, Model model,
            Authentication authentication) {
        try {
            model.addAttribute("label",
                    shippingService.displayShipmentDetails(currentVendorId(authentication), shipmentId));
        } catch (EasyPostException e) {
            model.addAttribute("error", e.getMessage());
        } catch (ResponseStatusException e) {
            model.addAttribute("error", e.getReason());
        }
        return "shipment-label";
    }

    private Long currentVendorId(Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
        return currentVendor.getId();
    }
}
