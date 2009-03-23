package de.schlund.pfixcore.example.webservices;

import javax.jws.WebService;

@WebService
public class BeanTestImpl implements BeanTest {

    public WeirdBean echoWeird(WeirdBean bean) {
        return bean;
    }
    
    public WeirdBeanSub echoWeirdSub(WeirdBeanSub bean) {
        return bean;
    }
    
}
