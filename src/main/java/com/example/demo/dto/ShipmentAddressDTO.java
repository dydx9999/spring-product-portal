package com.example.demo.dto;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ShipmentAddressDTO {
    @NotBlank
    private String name;
    private String company;
    @NotBlank
    private String street1;
    private String street2;
    @NotBlank
    private String city;
    @NotBlank
    @Size(min = 2, max = 2)
    private String state;
    @NotBlank
    private String zip;
    @NotBlank
    @Size(min = 2, max = 2)
    private String country = "US";
    private String phone;
    @Email
    private String email;

    public Map<String, Object> toParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("street1", street1);
        params.put("city", city);
        params.put("state", state);
        params.put("zip", zip);
        params.put("country", country);
        putIfPresent(params, "company", company);
        putIfPresent(params, "street2", street2);
        putIfPresent(params, "phone", phone);
        putIfPresent(params, "email", email);
        return params;
    }

    private static void putIfPresent(Map<String, Object> params, String key, String value) {
        if (value != null && !value.isBlank()) {
            params.put(key, value);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
