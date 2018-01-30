package org.pustefixframework.samples.mvctest;

import org.pustefixframework.container.spring.beans.PustefixConfigPostProcessor;
import org.pustefixframework.webservices.spring.WebServiceRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "org.pustefixframework.samples.mvctest")
public class SpringConfig implements WebMvcConfigurer {

    @Bean
    public PustefixConfigPostProcessor registerPustefix() {
        return new PustefixConfigPostProcessor();
    }

    @Bean
    public WebServiceRegistration registerNameCheckService(NameCheckService service) {
        WebServiceRegistration reg = new WebServiceRegistration();
        reg.setServiceName("NameCheck");
        reg.setInterface(NameCheckService.class.getName());
        reg.setTarget(service);
        return reg;
    }

}
