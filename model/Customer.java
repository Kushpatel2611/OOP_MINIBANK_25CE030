package model;

import java.io.Serializable;

public class Customer implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String email;
    private final String mobile;
    private final String customerId;
    private Address address;

    private static long customerCounter;

    public Customer(String name, String email, String mobile) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.customerId = generateCustomerId();
    }

    public static class Address {
        private final String line;
        private final String city;
        private final String pincode;

        public Address(String line, String city, String pincode) {
            this.line = line;
            this.city = city;
            this.pincode = pincode;
        }

        public String getLine() {
            return line;
        }

        public String getCity() {
            return city;
        }

        public String getPincode() {
            return pincode;
        }
    }

    private static synchronized String generateCustomerId() {
        customerCounter++;
        return "CUST" + (100 + customerCounter);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public Customer clone() {
        try {
            Customer cloned = (Customer) super.clone();

            if (this.address != null) {
                cloned.address = new Address(
                        this.address.getLine(),
                        this.address.getCity(),
                        this.address.getPincode());
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Customer clone failed", e);
        }
    }
}
