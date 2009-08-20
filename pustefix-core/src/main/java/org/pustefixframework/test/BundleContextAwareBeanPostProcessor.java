package org.pustefixframework.test;

import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.mock.MockBundleContext;

/**
 * BeanPostProcessor which sets the BundleContext before initialization.
 * It can be used to test Spring beans requiring a BundleContext when 
 * running outside of an OSGi runtime by providing a mock BundleContext.
 * 
 * @author mleidig@schlund.de
 *
 */
public class BundleContextAwareBeanPostProcessor implements BeanPostProcessor {

	private BundleContext bundleContext = new MockBundleContext();
	
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof BundleContextAware) {
			((BundleContextAware)bean).setBundleContext(bundleContext);
		}
		return bean;
	}
	
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	
}
