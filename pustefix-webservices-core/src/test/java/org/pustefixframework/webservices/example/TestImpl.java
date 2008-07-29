package org.pustefixframework.webservices.example;


public class TestImpl implements Test {
    
    private String text;
    
    public String echo(String str) {
        return str;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }

}
