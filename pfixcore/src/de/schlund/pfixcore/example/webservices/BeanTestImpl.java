package de.schlund.pfixcore.example.webservices;

public class BeanTestImpl implements BeanTest {

    public WeirdBean echoWeird(WeirdBean bean) {
        return bean;
    }
    
    public WeirdBeanSub echoWeirdSub(WeirdBeanSub bean) {
        return bean;
    }
    
}
