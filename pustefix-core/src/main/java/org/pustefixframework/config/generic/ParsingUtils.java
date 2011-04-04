/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.config.generic;

import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.marsching.flexiparse.objecttree.ObjectTreeElement;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig
 *
 */
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
    
    public static <T> T getSingleObject(Class<T> clazz, HandlerContext context) throws ParserException {
        Collection<T> configs = context.getObjectTreeElement().getObjectsOfType(clazz);
        if(configs.size()==0) {
            throw new ParserException("Object tree element contains no instance of type '"+clazz.getName()+"'.");
        } else if(configs.size()>1) {
            throw new ParserException("Object tree element contains multiple instances of type '"+clazz.getName()+"'.");
        }
        return configs.iterator().next();
    }
    
    public static <T> T getSingleObject(Class<T> clazz, HandlerContext context, boolean mandatory) throws ParserException {
        Collection<T> configs = context.getObjectTreeElement().getObjectsOfType(clazz);
        if(configs.size()==0) {
            if(mandatory) throw new ParserException("Object tree element contains no instance of type '"+clazz.getName()+"'.");
            else return null;
        } else if(configs.size()>1) {
            throw new ParserException("Object tree element contains multiple instances of type '"+clazz.getName()+"'.");
        }
        return configs.iterator().next();
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
    
    public static <T> T getFirstTopObject(Class<T> clazz, HandlerContext context, boolean mandatory) throws ParserException {
        Collection<T> configs = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(clazz);
        if(configs.size()==0) {
            if(mandatory) throw new ParserException("Object tree contains no instance of type '"+clazz.getName()+"'.");
            else return null;
        }
        return configs.iterator().next();
    }
    
    public static <T> T getSingleSubObjectFromRoot(Class<T> clazz, HandlerContext context) throws ParserException {
        ObjectTreeElement treeElem = context.getObjectTreeElement().getRoot();
        Collection<T> configs = treeElem.getObjectsOfTypeFromSubTree(clazz);
        if(configs.size()==0) {
            throw new ParserException("Object tree contains no instance of type '"+clazz.getName()+"'.");
        } else if(configs.size()>1) {
            throw new ParserException("Object tree contains multiple instances of type '"+clazz.getName()+"'.");
        }
        return configs.iterator().next();
    }
    
    public static <T> T getSingleSubObjectFromRoot(Class<T> clazz, HandlerContext context, boolean mandatory) throws ParserException {
        ObjectTreeElement treeElem = context.getObjectTreeElement().getRoot();
        Collection<T> configs = treeElem.getObjectsOfTypeFromSubTree(clazz);
        if(configs.size()==0) {
            if(mandatory) throw new ParserException("Object tree contains no instance of type '"+clazz.getName()+"'.");
            else return null;
        } else if(configs.size()>1) {
            throw new ParserException("Object tree contains multiple instances of type '"+clazz.getName()+"'.");
        }
        return configs.iterator().next();
    }
    
}
