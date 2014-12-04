package de.schlund.pfixcore.oxm.impl.serializers;

import org.pustefixframework.web.mvc.filter.Filter;
import org.pustefixframework.web.mvc.filter.Junction;
import org.pustefixframework.web.mvc.filter.Not;
import org.pustefixframework.web.mvc.filter.Property;

import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

public class FilterSerializer implements ComplexTypeSerializer {

    @Override
    public void serialize(Object obj, SerializationContext context, XMLWriter writer) throws SerializationException {
        
        if(obj instanceof Filter) {
            Filter filterSpec = (Filter)obj;
            if(filterSpec instanceof Junction) {
                Filter[] filters = ((Junction)filterSpec).getFilters();
                writer.writeStartElement(filterSpec.getClass().getSimpleName().toLowerCase());
                for(Filter filter: filters) {
                    context.serialize(filter, writer);
                }
                writer.writeEndElement();
            } else if(filterSpec instanceof Not) {
                writer.writeStartElement("not");
                context.serialize(((Not)obj).getFilter(), writer);
                writer.writeEndElement();
            } else if(filterSpec instanceof Property) {
                writer.writeStartElement("property");
                writer.writeAttribute("name", context.serialize(((Property)filterSpec).getName()));
                writer.writeAttribute("value", context.serialize(((Property)filterSpec).getValue()));
                writer.writeEndElement();
            }
        } else throw new SerializationException("Illegal type: "+obj.getClass().getName());
        
    }

}
