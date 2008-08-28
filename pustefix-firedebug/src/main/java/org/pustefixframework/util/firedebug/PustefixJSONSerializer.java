package org.pustefixframework.util.firedebug;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.webservice.jsonqx.CalendarSerializer;
import de.schlund.pfixcore.webservice.jsonws.SerializationException;
import de.schlund.pfixcore.webservice.jsonws.Serializer;
import de.schlund.pfixcore.webservice.jsonws.SerializerRegistry;

/**
 * PustefixJSONSerializer
 * 
 * PustefixJSONSerializer uses the Pustefix JSONSerializer to
 * serialize JAVA objects to JSON.
 * 
 * @author Holger Rüprich
 */

public class PustefixJSONSerializer implements JSONSerializer {

    public String javaToJson(Object object) {
        BeanDescriptorFactory beanDescriptorFactory = new BeanDescriptorFactory();
        SerializerRegistry serializerRegistry = new SerializerRegistry(beanDescriptorFactory);
        Serializer dateSerializer=new CalendarSerializer();
        serializerRegistry.register(Date.class,dateSerializer);
        serializerRegistry.register(Calendar.class,dateSerializer);
        serializerRegistry.register(GregorianCalendar.class,dateSerializer);
        de.schlund.pfixcore.webservice.jsonws.JSONSerializer jsonSerializer = new de.schlund.pfixcore.webservice.jsonws.JSONSerializer(serializerRegistry);
        
        Writer writer = new StringWriter();
        try {
            jsonSerializer.serialize(object, writer);
        } catch (SerializationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

}
