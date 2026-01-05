package com.example.ogani.model.request;

public class UpdateOrderRequest {
    private String firstname;
    private String lastname;
    private String country;
    private String address;
    private String town;
    private String state;
    private String postCode;
    private String email;
    private String phone;
    private String note;

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getTown() { return town; }
    public void setTown(String town) { this.town = town; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPostCode() { return postCode; }
    public void setPostCode(String postCode) { this.postCode = postCode; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
