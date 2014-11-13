package de.schlund.pfixcore.example;

public class Email {

    private String address;
    private String type;
    private boolean active;
    private boolean virusProtection;
    private boolean spamFilter;
    private String forwardTarget;
    
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public boolean isVirusProtection() {
        return virusProtection;
    }
    public void setVirusProtection(boolean virusProtection) {
        this.virusProtection = virusProtection;
    }
    public boolean isSpamFilter() {
        return spamFilter;
    }
    public void setSpamFilter(boolean spamFilter) {
        this.spamFilter = spamFilter;
    }
    public String getForwardTarget() {
        return forwardTarget;
    }
    public void setForwardTarget(String forwardTarget) {
        this.forwardTarget = forwardTarget;
    }
    
}
