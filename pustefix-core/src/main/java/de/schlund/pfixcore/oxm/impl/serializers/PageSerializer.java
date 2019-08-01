package de.schlund.pfixcore.oxm.impl.serializers;

import java.util.Iterator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

public class PageSerializer implements ComplexTypeSerializer {

    @Override
    public void serialize(Object obj, SerializationContext context, XMLWriter writer) throws SerializationException {

        if(obj instanceof Page) {
            Page<?> page = (Page<?>)obj;
            writer.writeAttribute("firstPage", String.valueOf(page.isFirst()));
            writer.writeAttribute("lastPage", String.valueOf(page.isLast()));
            writer.writeAttribute("number", String.valueOf(page.getNumber()));
            writer.writeAttribute("numberOfElements", String.valueOf(page.getNumberOfElements()));
            writer.writeAttribute("size", String.valueOf(page.getSize()));
            writer.writeAttribute("totalElements", String.valueOf(page.getTotalElements()));
            writer.writeAttribute("totalPages", String.valueOf(page.getTotalPages()));
            Sort sort = page.getSort();
            if(sort != null && sort.isSorted()) {
                writer.writeStartElement("sort");
                Iterator<Order> it = sort.iterator();
                while(it.hasNext()) {
                    Order order = it.next();
                    writer.writeStartElement("order");
                    writer.writeAttribute("ascending", String.valueOf(order.isAscending()));
                    writer.writeAttribute("direction", order.getDirection().name());
                    writer.writeAttribute("ignoreCase", String.valueOf(order.isIgnoreCase()));
                    writer.writeAttribute("property", order.getProperty());
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
            writer.writeStartElement("content");
            context.serialize(page.getContent(), writer);
            writer.writeEndElement();
        } else throw new SerializationException("Illegal type: "+obj.getClass().getName());

    }

}
