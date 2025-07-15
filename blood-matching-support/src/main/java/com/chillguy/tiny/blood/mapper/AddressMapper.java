package com.chillguy.tiny.blood.mapper;

import com.chillguy.tiny.blood.dto.AddressDto;
import com.chillguy.tiny.blood.entity.Address;

public class AddressMapper {

    public static AddressDto addressMapper(Address address) {
        AddressDto addressDto = new AddressDto();
        addressDto.setCity(address.getCity());
        addressDto.setWard(address.getWard());
        addressDto.setDistrict(address.getDistrict());
        addressDto.setStreet(address.getStreet());
        addressDto.setLongitude(address.getLongitude());
        addressDto.setLatitude(address.getLatitude());
        return addressDto;
    }

    public static Address addressMapper(AddressDto addressDto) {
        Address address = new Address();
        address.setCity(addressDto.getCity());
        address.setWard(addressDto.getWard());
        address.setDistrict(addressDto.getDistrict());
        address.setStreet(addressDto.getStreet());
        address.setLongitude(addressDto.getLongitude());
        address.setLatitude(addressDto.getLatitude());
        return address;
    }
}
