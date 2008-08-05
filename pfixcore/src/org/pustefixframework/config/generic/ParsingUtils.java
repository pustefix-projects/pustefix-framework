package org.pustefixframework.config.generic;

import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

public class ParsingUtils {

    public static void checkAttributes(Element element, String[] mandatoryAttrs, String[] optionalAttrs) throws ParserException {
        NamedNodeMap attrs = element.getAttributes();
        for(int i=0; i<attrs.getLength(); i++) {
            String attrName = attrs.item(i).getNodeName();
            if(contains(attrName, mandatoryAttrs)) {
               String attrVal = attrs.item(i).getNodeValue();
               if(attrVal==null || attrVal.trim().equals("")) {
                   throw new ParserException("Missing mandatory attribute: "+attrName);
               }
            } else if(!contains(attrName, optionalAttrs)) {
                throw new ParserException("Unsupported attribute: "+attrName);
            }
        }
    }
    
    private static boolean contains(String attrName, String[] attrNames) {
        if(attrNames != null) {
            for(String name : attrNames) {
                if(name.equals(attrName)) return true;
            }
        }
        return false;
    }
    
    public static <T> T getSingleTopObject(Class<T> clazz, HandlerContext context) throws ParserException {
        Collection<T> configs = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(clazz);
        if(configs.size()==0) {
            throw new ParserException("Object tree contains no instance of type '"+clazz.getName()+"'.");
        } else if(configs.size()>1) {
            throw new ParserException("Object tree contains multiple instances of type '"+clazz.getName()+"'.");
        }
        return configs.iterator().next();
    }
    
}
