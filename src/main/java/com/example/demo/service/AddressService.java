package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.ShipmentAddressDTO;
import com.example.demo.model.Address;
import com.example.demo.repository.AddressRepository;

import jakarta.validation.Validator;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final Validator validator;

    public AddressService(AddressRepository addressRepository, Validator validator) {
        this.addressRepository = addressRepository;
        this.validator = validator;
    }

    public List<Address> getAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIdAsc(userId);
    }

    public boolean addAddress(Long userId, ShipmentAddressDTO form) {
        if (form == null || isBlank(form)) {
            return false;
        }
        var violations = validator.validate(form);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid address: " + message);
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setName(form.getName().trim());
        address.setCompany(trimToNull(form.getCompany()));
        address.setStreet1(form.getStreet1().trim());
        address.setStreet2(trimToNull(form.getStreet2()));
        address.setCity(form.getCity().trim());
        address.setState(form.getState().trim().toUpperCase());
        address.setZip(form.getZip().trim());
        address.setCountry(form.getCountry().trim().toUpperCase());
        address.setPhone(trimToNull(form.getPhone()));
        address.setEmail(trimToNull(form.getEmail()));
        addressRepository.save(address);
        return true;
    }

    private static boolean isBlank(ShipmentAddressDTO form) {
        return trimToNull(form.getName()) == null
                && trimToNull(form.getCompany()) == null
                && trimToNull(form.getStreet1()) == null
                && trimToNull(form.getStreet2()) == null
                && trimToNull(form.getCity()) == null
                && trimToNull(form.getState()) == null
                && trimToNull(form.getZip()) == null
                && trimToNull(form.getPhone()) == null
                && trimToNull(form.getEmail()) == null;
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
