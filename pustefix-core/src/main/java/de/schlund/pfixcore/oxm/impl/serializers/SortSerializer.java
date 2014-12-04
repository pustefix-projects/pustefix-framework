package de.schlund.pfixcore.oxm.impl.serializers;

import java.util.Iterator;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

public class SortSerializer implements ComplexTypeSerializer {

    @Override
    public void serialize(Object obj, SerializationContext context, XMLWriter writer) throws SerializationException {
        
        if(obj instanceof Sort) {
            Sort sort = (Sort)obj;
            Iterator<Order> it = sort.iterator();
            while(it.hasNext()) {
                Order order = it.next();
                writer.writeStartElement("order");
                context.serialize(order, writer);
                writer.writeEndElement();
            }
        } else throw new SerializationException("Illegal type: "+obj.getClass().getName());
        
    }
    
}
