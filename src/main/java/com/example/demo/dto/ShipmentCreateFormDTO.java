package com.example.demo.dto;

import jakarta.validation.Valid;

public class ShipmentCreateFormDTO {
    @Valid
    private ShipmentAddressDTO fromAddress = new ShipmentAddressDTO();
    @Valid
    private ShipmentAddressDTO toAddress = new ShipmentAddressDTO();
    @Valid
    private ShipmentParcelDTO parcel = new ShipmentParcelDTO();

    private String reference;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ShipmentAddressDTO getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(ShipmentAddressDTO fromAddress) {
        this.fromAddress = fromAddress;
    }

    public ShipmentAddressDTO getToAddress() {
        return toAddress;
    }

    public void setToAddress(ShipmentAddressDTO toAddress) {
        this.toAddress = toAddress;
    }

    public ShipmentParcelDTO getParcel() {
        return parcel;
    }

    public void setParcel(ShipmentParcelDTO parcel) {
        this.parcel = parcel;
    }
}
