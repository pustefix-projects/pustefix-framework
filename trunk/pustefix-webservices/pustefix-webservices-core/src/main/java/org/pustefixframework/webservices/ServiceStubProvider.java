package org.pustefixframework.webservices;

import java.io.OutputStream;

public class ServiceStubProvider {

    private ServiceRuntime serviceRuntime;

    ServiceStubProvider(ServiceRuntime serviceRuntime) {
        this.serviceRuntime = serviceRuntime;
    }

    public void generateStub(String serviceName, String serviceType, OutputStream out) {
        generateStub(new String[] { serviceName }, serviceType, out);
    }

    public void generateStub(String[] serviceNames, String serviceType, OutputStream out) {
        try {
            serviceRuntime.generateStub(serviceNames, serviceType, out);
        } catch(Exception x) {
            throw new RuntimeException("Error while generating webservice stubs", x);
        }
    }

}
