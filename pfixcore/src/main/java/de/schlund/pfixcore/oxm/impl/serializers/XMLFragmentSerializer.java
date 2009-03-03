package de.schlund.pfixcore.oxm.impl.serializers;

import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

/**
 * XMLFragmentSerializer
 * 
 * Treats a string as an XML fragement and inserts it
 * into the document.
 *  
 * @author  Stephan Schmidt <schst@stubbles.net>
 */
public class XMLFragmentSerializer implements ComplexTypeSerializer {

	public XMLFragmentSerializer() {
	}

	public void serialize(Object obj, SerializationContext context, XMLWriter writer) throws SerializationException {
        if (!(obj instanceof String)) {
            throw new SerializationException("Type not supported: "+obj.getClass().getName());
        }
        String fragment = (String)obj;
        try {
            writer.writeFragment(fragment);
        } catch (RuntimeException e) {
            throw new SecurityException("Fragment " + fragment + " could not be written to the document.", e.getCause());
        }            
    }
}