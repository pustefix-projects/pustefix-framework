package de.schlund.pfixcore.example;

public class AdultInfoChangeEvent {
    
    private Boolean value;
    private ContextAdultInfo source;
    
    public AdultInfoChangeEvent(Boolean value, ContextAdultInfo source) {
        this.value = value;
        this.source = source;
    }
    
    public Boolean getValue() {
        return value;
    }
    
    public ContextAdultInfo getSource() {
        return source;
    }

}
