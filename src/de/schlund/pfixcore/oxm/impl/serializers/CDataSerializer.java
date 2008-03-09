package de.schlund.pfixcore.oxm.impl.serializers;

import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

/**
 * CDataSerializer
 * 
 * Writes a string as character data section
 * to the document.
 *  
 * @author  Stephan Schmidt <schst@stubbles.net>
 */
public class CDataSerializer implements ComplexTypeSerializer {

	public CDataSerializer() {
	}

	public void serialize(Object obj, SerializationContext context, XMLWriter writer) throws SerializationException {
        if (!(obj instanceof String)) {
            throw new SerializationException("Type not supported: "+obj.getClass().getName());
        }
        String cdata = (String)obj;
        writer.writeCDataSection(cdata);
    }
}