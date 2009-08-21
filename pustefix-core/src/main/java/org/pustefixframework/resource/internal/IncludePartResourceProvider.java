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

package org.pustefixframework.resource.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.transform.TransformerException;

import org.pustefixframework.resource.IncludePartResource;
import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.resource.ResourceProvider;
import org.pustefixframework.resource.support.AbstractResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * Provides resources from the file system using the file scheme.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IncludePartResourceProvider implements ResourceProvider {

    private final static String[] SUPPORTED_SCHEMES = new String[] { "includepart" };

    public Resource[] getResources(URI uri, URI originallyRequestedURI, ResourceLoader resourceLoader) {
        if (uri.getScheme() == null || !uri.getScheme().equals("includepart")) {
            throw new IllegalArgumentException("Cannot handle URI \"" + uri.toASCIIString() + "\": Scheme is not supported");
        }
        String schemeSpecificPart = uri.getSchemeSpecificPart();
        if (schemeSpecificPart == null) {
            throw new IllegalArgumentException("Error: URI \"" + uri.toASCIIString() + "\" does not specify a scheme specific part");
        }
        int lastColonIndex = schemeSpecificPart.lastIndexOf(':');
        if (lastColonIndex < 0) {
            throw new IllegalArgumentException("Error: URI \"" + uri.toASCIIString() + "\" does not contain an include part name");
        }
        String resourceURIString = schemeSpecificPart.substring(0, lastColonIndex);
        String includePartName = schemeSpecificPart.substring(lastColonIndex + 1);
        String themeName = null;
        int slashIndex = includePartName.indexOf('/');
        if (slashIndex >= 0) {
            themeName = includePartName.substring(slashIndex + 1);
            themeName = decodeURL(themeName);
            includePartName = includePartName.substring(0, slashIndex);
        }
        includePartName = decodeURL(includePartName);
        if (resourceURIString.length() == 0) {
            throw new IllegalArgumentException("Error: URI \"" + uri.toASCIIString() + "\" does not contain the URI of a file resource");
        }
        if (includePartName.length() == 0) {
            throw new IllegalArgumentException("Error: URI \"" + uri.toASCIIString() + "\" does not contain an include part name");
        }
        if (themeName != null && themeName.length() == 0) {
            throw new IllegalArgumentException("Error: URI \"" + uri.toASCIIString() + "\" does not contain a valid theme name");
        }
        URI resourceURI = URI.create(resourceURIString);

        InputStreamResource[] resources = resourceLoader.getResources(resourceURI, InputStreamResource.class);
        if (resources == null) {
            return null;
        }

        LinkedList<IncludePartResource> includePartResources = new LinkedList<IncludePartResource>();
        for (InputStreamResource resource : resources) {
            Collection<IncludePartResource> includePartResourcesTemp = loadIncludePartsFromFile(resource, includePartName, themeName, originallyRequestedURI != null ? originallyRequestedURI : uri);
            if (includePartResourcesTemp != null) {
                includePartResources.addAll(includePartResourcesTemp);
            }
        }

        if (includePartResources.size() == 0) {
            return null;
        } else {
            return includePartResources.toArray(new IncludePartResource[includePartResources.size()]);
        }
    }

    private Collection<IncludePartResource> loadIncludePartsFromFile(InputStreamResource resource, String includePartName, String requestedThemeName, URI originallyRequestedURI) {
        Document document;
        //TODO: find way to control which type of DOM we use here
        try {
//            document = Xml.parseMutable(resource);
            document = Xml.parse(XsltVersion.XSLT1, resource);
        } catch (TransformerException e) {
        	return null;
        }
//        } catch (IOException e) {
//            return null;
//        } catch (SAXException e) {
//            return null;
//        }
        Element rootElement = document.getDocumentElement();
        if (rootElement.getNamespaceURI() != null) {
            return null;
        }
        if (!rootElement.getLocalName().equals("include_parts")) {
            return null;
        }
        LinkedList<IncludePartResource> includePartResources = new LinkedList<IncludePartResource>();
        NodeList partElements = rootElement.getElementsByTagName("part");
        for (int i = 0; i < partElements.getLength(); i++) {
            Element partElement = (Element) partElements.item(i);
            if (partElement.getAttribute("name").equals(includePartName)) {
                NodeList themeElements = partElement.getElementsByTagName("theme");
                for (int j = 0; j < themeElements.getLength(); j++) {
                    Element themeElement = (Element) themeElements.item(j);
                    String themeName = themeElement.getAttribute("name").trim();
                    if (themeName.length() > 0 && (requestedThemeName == null || requestedThemeName.equals(themeName))) {
                        IncludePartResource includePartResource = createIncludePartResource(themeElement, themeName, includePartName, resource, originallyRequestedURI);
                        includePartResources.add(includePartResource);
                    }
                }
            }
        }
        return includePartResources;
    }

    private IncludePartResource createIncludePartResource(Element themeElement, String themeName, String partName, InputStreamResource fileResource, URI originallyRequestedURI) {
        IncludePartResourceImpl resource = new IncludePartResourceImpl();
        resource.element = themeElement;
        resource.originalURI = URI.create("includepart:" + fileResource.getOriginalURI().toASCIIString() + ":" + encodeURL(partName) + "/" + encodeURL(themeName));
        URI[] supplementaryURIs = fileResource.getSupplementaryURIs();
        if (supplementaryURIs != null) {
            LinkedList<URI> uris = new LinkedList<URI>();
            for (URI uri : supplementaryURIs) {
                URI newURI = URI.create("includepart:" + uri.toASCIIString() + ":" + encodeURL(partName) + "/" + encodeURL(themeName));
                uris.add(newURI);
            }
            resource.supplementaryURIs = uris.toArray(new URI[uris.size()]);
        }
        resource.uri = originallyRequestedURI;
        resource.theme = themeName;
        resource.includePartName = partName;
        resource.originResource = fileResource;
        return resource;
    }

    public String[] getSchemes() {
        return SUPPORTED_SCHEMES;
    }

    private class IncludePartResourceImpl extends AbstractResource implements IncludePartResource {

        private Element element;

        private URI originalURI;

        private URI[] supplementaryURIs;

        private URI uri;

        private String theme;

        private String includePartName;

        private InputStreamResource originResource;

        public Element getElement() throws IOException, SAXException {
            return element;
        }

        public URI getOriginalURI() {
            return originalURI;
        }

        public URI[] getSupplementaryURIs() {
            return supplementaryURIs;
        }

        public URI getURI() {
            return uri;
        }

        public String getTheme() {
            return theme;
        }

        public String getIncludePartName() {
            return includePartName;
        }

        public InputStreamResource getOriginResource() {
            return originResource;
        }

    }

    private static String encodeURL(String url) {
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    private static String decodeURL(String url) {
        try {
            return URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
}
