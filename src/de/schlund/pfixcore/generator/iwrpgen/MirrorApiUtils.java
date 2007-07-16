/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.generator.iwrpgen;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;

/**
 * @author mleidig@schlund.de
 */
public class MirrorApiUtils {

    public static MethodDeclaration getMethodDeclaration(ClassDeclaration classDecl,String methodName) {
    	Collection<MethodDeclaration> meths=classDecl.getMethods();
		for(MethodDeclaration meth:meths) {
			if(meth.getSimpleName().equals(methodName)) return meth;
		}
		return null;
    }
    
    public static FieldDeclaration getFieldDeclaration(ClassDeclaration classDecl,String fieldName) {
    	Collection<FieldDeclaration> fields=classDecl.getFields();
    	for(FieldDeclaration field:fields) {
    		if(field.getSimpleName().equals(fieldName)) return field;
    	}
    	return null;
    }
    
    public static AnnotationMirror getAnnotationMirror(Declaration decl,AnnotationTypeDeclaration annoDecl) {
    	Collection<AnnotationMirror> mirrors=decl.getAnnotationMirrors();
    	for(AnnotationMirror mirror:mirrors) {
    		if(mirror.getAnnotationType().getDeclaration().equals(annoDecl)) return mirror;
    	}
    	return null;
    }
  
    
    public static AnnotationValue getAnnotationValue(AnnotationMirror mirror,String elemName) {
    	Map<AnnotationTypeElementDeclaration,AnnotationValue> map=mirror.getElementValues();
		Iterator<AnnotationTypeElementDeclaration> it=map.keySet().iterator();
		while(it.hasNext()) {
			AnnotationTypeElementDeclaration at=it.next();
			if(at.getSimpleName().equals(elemName)) return map.get(at);
		}
		return null;
    }
	
    public static boolean isPublicNonStatic(Declaration decl) {
    	Collection<Modifier> mods=decl.getModifiers();
    	return mods.contains(Modifier.PUBLIC)&&!mods.contains(Modifier.STATIC);
    }
    
    public static boolean isPublicNonStaticNonFinal(Declaration decl) {
    	Collection<Modifier> mods=decl.getModifiers();
		return mods.contains(Modifier.PUBLIC)&&!(mods.contains(Modifier.STATIC)||mods.contains(Modifier.FINAL));
    }
    
}
