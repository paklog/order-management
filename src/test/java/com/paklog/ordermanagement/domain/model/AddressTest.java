package com.paklog.ordermanagement.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void testDefaultConstructor() {
        Address address = new Address();

        assertNotNull(address);
        assertNull(address.getName());
        assertNull(address.getAddressLine1());
        assertNull(address.getAddressLine2());
        assertNull(address.getCity());
        assertNull(address.getStateOrRegion());
        assertNull(address.getPostalCode());
        assertNull(address.getCountryCode());
    }

    @Test
    void testParameterizedConstructor() {
        String name = "John Doe";
        String addressLine1 = "123 Main St";
        String addressLine2 = "Apt 4B";
        String city = "New York";
        String stateOrRegion = "NY";
        String postalCode = "10001";
        String countryCode = "US";

        Address address = new Address(name, addressLine1, addressLine2, city, stateOrRegion, postalCode, countryCode);

        assertEquals(name, address.getName());
        assertEquals(addressLine1, address.getAddressLine1());
        assertEquals(addressLine2, address.getAddressLine2());
        assertEquals(city, address.getCity());
        assertEquals(stateOrRegion, address.getStateOrRegion());
        assertEquals(postalCode, address.getPostalCode());
        assertEquals(countryCode, address.getCountryCode());
    }

    @Test
    void testSettersAndGetters() {
        Address address = new Address();

        address.setName("Jane Smith");
        address.setAddressLine1("456 Oak Ave");
        address.setAddressLine2("Suite 200");
        address.setCity("Los Angeles");
        address.setStateOrRegion("CA");
        address.setPostalCode("90210");
        address.setCountryCode("US");

        assertEquals("Jane Smith", address.getName());
        assertEquals("456 Oak Ave", address.getAddressLine1());
        assertEquals("Suite 200", address.getAddressLine2());
        assertEquals("Los Angeles", address.getCity());
        assertEquals("CA", address.getStateOrRegion());
        assertEquals("90210", address.getPostalCode());
        assertEquals("US", address.getCountryCode());
    }

    @Test
    void testEqualsAndHashCode() {
        Address address1 = new Address("John Doe", "123 Main St", "Apt 4B", "New York", "NY", "10001", "US");
        Address address2 = new Address("John Doe", "123 Main St", "Apt 4B", "New York", "NY", "10001", "US");
        Address address3 = new Address("Jane Smith", "456 Oak Ave", "Suite 200", "Los Angeles", "CA", "90210", "US");

        assertEquals(address1, address2);
        assertEquals(address1.hashCode(), address2.hashCode());
        assertNotEquals(address1, address3);
        assertNotEquals(address1.hashCode(), address3.hashCode());
    }

    @Test
    void testEqualsWithNull() {
        Address address = new Address("John Doe", "123 Main St", "Apt 4B", "New York", "NY", "10001", "US");

        assertNotEquals(null, address);
        assertEquals(address, address);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Address address = new Address("John Doe", "123 Main St", "Apt 4B", "New York", "NY", "10001", "US");
        String notAnAddress = "not an address";

        assertNotEquals(address, notAnAddress);
    }

    @Test
    void testEqualsWithNullFields() {
        Address address1 = new Address(null, null, null, null, null, null, null);
        Address address2 = new Address(null, null, null, null, null, null, null);
        Address address3 = new Address("John", null, null, null, null, null, null);

        assertEquals(address1, address2);
        assertNotEquals(address1, address3);
    }

    @Test
    void testHashCodeConsistency() {
        Address address = new Address("John Doe", "123 Main St", "Apt 4B", "New York", "NY", "10001", "US");

        int hashCode1 = address.hashCode();
        int hashCode2 = address.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testConstructorWithNullValues() {
        Address address = new Address(null, null, null, null, null, null, null);

        assertNotNull(address);
        assertNull(address.getName());
        assertNull(address.getAddressLine1());
        assertNull(address.getAddressLine2());
        assertNull(address.getCity());
        assertNull(address.getStateOrRegion());
        assertNull(address.getPostalCode());
        assertNull(address.getCountryCode());
    }
}