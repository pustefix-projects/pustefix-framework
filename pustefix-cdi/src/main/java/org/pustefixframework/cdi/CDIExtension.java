package org.pustefixframework.cdi;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;

public class CDIExtension implements Extension {

    private List<Bean<Object>> beans = new ArrayList<Bean<Object>>();
    
    public void addBean(@Observes ProcessBean<Object> procBean) {
        System.out.println("BEAN: "+ procBean.getBean());
        Bean<Object> bean = procBean.getBean();
        beans.add(bean);
    }
    
}
