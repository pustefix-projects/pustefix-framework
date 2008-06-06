package de.schlund.pfixcore.oxm.impl.serializers;

import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

/**
 * ForceElementSerializer
 * 
 * Makes sure, that a simple type is serialized to a new
 * element instead of an attribute
 *  
 * @author Stephan Schmidt <schst@stubbles.net>
 */
public class ForceElementSerializer  implements ComplexTypeSerializer {
    public void serialize(Object obj, SerializationContext context, XMLWriter writer) throws SerializationException {
        writer.writeCharacters(obj.toString());
    }
}