package de.schlund.pfixcore.util;

import org.w3c.dom.Element;

import de.schlund.pfixxml.targets.AuxDependencyInclude;

public interface IDumpText {
    void generateList(String depend) throws Exception;
    void addRootNodeAtributes(Element root);
    boolean includePartOK(AuxDependencyInclude aux);
    String retrieveTheme(AuxDependencyInclude aux);
}
