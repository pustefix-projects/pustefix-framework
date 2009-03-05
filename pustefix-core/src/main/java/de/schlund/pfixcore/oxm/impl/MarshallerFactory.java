package de.schlund.pfixcore.oxm.impl;

import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.oxm.Marshaller;

public class MarshallerFactory {

    private static Marshaller marshaller;

    static {
        BeanDescriptorFactory factory = new BeanDescriptorFactory();
        SerializerRegistry registry = new SerializerRegistry(factory);
        marshaller = new MarshallerImpl(registry);
    }

    public static Marshaller getSharedMarshaller() {
        return marshaller;
    }

}
