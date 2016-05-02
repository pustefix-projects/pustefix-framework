package org.pustefixframework.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;


public class AnnotationUtils {
    
    public static List<VariableElement> getPublicNonStaticNonFinalFields(TypeElement typeElem) {
        List<VariableElement> resultElems = new ArrayList<VariableElement>();
        List<? extends Element> elems = typeElem.getEnclosedElements();
        for(Element elem: elems) {
            if(elem.getKind() == ElementKind.FIELD) {
                if(isPublicNonStaticNonFinal(elem)) resultElems.add((VariableElement)elem);
            }
        }
        return resultElems;
    }

    public static List<ExecutableElement> getPublicNonStaticMethods(TypeElement typeElem) {
        List<ExecutableElement> resultElems = new ArrayList<ExecutableElement>();
        List<? extends Element> elems = typeElem.getEnclosedElements();
        for(Element elem: elems) {
            if(elem.getKind() == ElementKind.METHOD) {
                if(isPublicNonStatic(elem)) {
                    resultElems.add((ExecutableElement)elem);
                }
            }
        }
        return resultElems;
    }
    
    public static ExecutableElement getMethod(TypeElement typeElem, String methodName) {
        List<? extends Element> elems = typeElem.getEnclosedElements();
        for(Element elem: elems) {
            if(elem.getKind() == ElementKind.METHOD) {
                ExecutableElement exElem = (ExecutableElement)elem;
                if(exElem.getSimpleName().toString().equals(methodName)) {
                    return exElem;
                }
            }
        }
        return null;
    }
    
    public static VariableElement getField(TypeElement typeElem, String fieldName) {
        List<? extends Element> elems = typeElem.getEnclosedElements();
        for(Element elem: elems) {
            if(elem.getKind() == ElementKind.FIELD) {
                VariableElement varElem = (VariableElement)elem;
                if(varElem.getSimpleName().toString().equals(fieldName)) {
                    return varElem;
                }
            }
        }
        return null;
    }

    public static boolean isPublicNonStatic(Element element) {
        Set<Modifier> mods = element.getModifiers();
        return mods.contains(Modifier.PUBLIC) && !mods.contains(Modifier.STATIC);
    }

    public static boolean isPublicNonStaticNonFinal(Element element) {
        Set<Modifier> mods = element.getModifiers();
        return mods.contains(Modifier.PUBLIC) && !(mods.contains(Modifier.STATIC) || mods.contains(Modifier.FINAL));
    }

}