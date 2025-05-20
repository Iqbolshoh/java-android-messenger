package com.example.mikeygresl.template;

public class Contact {

    private String UID;
    private String email;
    private String fname;
    private String lname;

    public Contact() {}

    public Contact(String UID, String email, String fname, String lname) {

        this.UID = UID;
        this.email = email;
        this.fname = fname;
        this.lname = lname;
    }

    public String getUID() {
        return UID;
    }

    public String getEmail() {
        return email;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }
}
