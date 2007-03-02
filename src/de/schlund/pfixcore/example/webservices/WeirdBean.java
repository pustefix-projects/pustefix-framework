package de.schlund.pfixcore.example.webservices;

import de.schlund.pfixcore.webservice.beans.Alias;
import de.schlund.pfixcore.webservice.beans.Exclude;

public class WeirdBean {

    int foo=1;
    int bar=2;
    int baz=3;
    
    @Alias("public")
    public int pub=4;
    
    int ro=5;
    int wo=6;
    
    public static int staticMember=7;
    public final int finalMember=8;
    
    @Exclude
    public int blah=9;
    
    public int getFoo() {return foo;}
    public void setFoo(int foo) {this.foo=foo;}

    @Exclude
    public int getBar() {return bar;}
    public void setBar(int bar) {this.bar=bar;}

    @Alias("test")
    public int getBaz() {return baz;}
    public void setBaz(int baz) {this.baz=baz;}
    
    public int getRo() {
        return ro;
    }
    
    public void setRo(int wo) {
        this.wo=wo;
    }
    
}
