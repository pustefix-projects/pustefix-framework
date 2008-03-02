package de.schlund.pfixcore.oxm.impl.serializers;

import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

public class ForceElementSerializer  implements ComplexTypeSerializer {
    public void serialize(Object obj, SerializationContext context, XMLWriter writer) throws SerializationException {
        writer.writeCharacters(obj.toString());
    }
}