package org.pustefixframework.cmis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class CMISMappingImpl implements CMISMapping {

    private String id;
    private String atomPubUrl;
    private String user;
    private String password;
    private String basePath;
    
    private Session session;
    
    public CMISMappingImpl(String id, String atomPubUrl, String user, String password, String basePath) {
        this.id = id;
        this.atomPubUrl = atomPubUrl;
        this.user = user;
        this.password = password;
        this.basePath = basePath;
    }
    
    public String getId() {
        return id;
    }
    
    private Session getSession() {
        
        if(basePath.endsWith("/")) basePath = basePath.substring(0, basePath.length()-1);
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put(SessionParameter.USER, user);
        parameter.put(SessionParameter.PASSWORD, password);
        parameter.put(SessionParameter.ATOMPUB_URL, atomPubUrl);
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        List<Repository> repos = factory.getRepositories(parameter);
        if(repos.size() > 0) {
            parameter.put(SessionParameter.REPOSITORY_ID, repos.get(0).getId());
            session = factory.createSession(parameter);
            return session;
        } else {
            throw new RuntimeException("Can't find CMIS repository under '" + atomPubUrl + "'.");
        }
        
    }
        
    public CmisObject getCMISObject(String path) {
        if(!path.startsWith("/")) path = "/" + path;
        return getSession().getObjectByPath(basePath + path );
    }
    
}
