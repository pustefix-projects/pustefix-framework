package de.schlund.pfixcore.example.bean;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.schlund.pfixcore.beans.BeanDescriptor;
import de.schlund.pfixcore.beans.metadata.Bean;
import de.schlund.pfixcore.beans.metadata.Beans;
import de.schlund.pfixcore.example.webservices.DataBean;

public class BeanDescriptorMain {
    public static void main(String[] args) {
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
        Logger logger = Logger.getRootLogger();
        logger.setLevel((Level) Level.DEBUG);
        logger.removeAllAppenders();
        logger.addAppender(appender);

        Beans beans = new Beans();
        Bean bean = new Bean(DataBean.class.getName());
        bean.excludeByDefault();
        bean.includeProperty("boolVal");
        beans.setBean(bean);
        BeanDescriptor desc = new BeanDescriptor(DataBean.class, null);
        System.out.println(desc);
    }
}
