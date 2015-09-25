package org.pustefixframework.eventbus;

import org.springframework.beans.factory.config.AbstractFactoryBean;

public class IEventListenerFactoryBean extends AbstractFactoryBean<IEventListenerBean> {

    @Override
    protected IEventListenerBean createInstance() throws Exception {
        return new IEventListenerBean();
    }
    
    @Override
    public Class<?> getObjectType() {
        return IEventListenerBean.class;
    }
    
}
