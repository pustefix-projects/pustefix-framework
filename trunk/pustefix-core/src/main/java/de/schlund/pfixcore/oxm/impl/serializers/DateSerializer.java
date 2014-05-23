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
package de.schlund.pfixcore.oxm.impl.serializers;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.schlund.pfixcore.oxm.impl.AnnotationAware;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.SimpleTypeSerializer;

public class DateSerializer implements SimpleTypeSerializer, AnnotationAware {

	private final static String DEFAULT_PATTERN="yyyy-MM-dd'T'HH:mm:ssZ";
	private String pattern;
	
	public DateSerializer() {
		pattern=DEFAULT_PATTERN;
	}
	
	public DateSerializer(String pattern) {
		this.pattern=pattern;
	}
	
	public void setAnnotation(Annotation annotation) {
		if(annotation instanceof de.schlund.pfixcore.oxm.impl.annotation.DateSerializer) {
			de.schlund.pfixcore.oxm.impl.annotation.DateSerializer dateAnno=
				(de.schlund.pfixcore.oxm.impl.annotation.DateSerializer)annotation;
			pattern = dateAnno.value();
		}
	}
		
	public String serialize(Object obj, SerializationContext context) throws SerializationException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		if(obj instanceof Date) {
			return dateFormat.format((Date)obj);
		} else if(obj instanceof Calendar) {
			return dateFormat.format(((Calendar)obj).getTime());
		} throw new SerializationException("Type not supported: "+obj.getClass().getName());
	}
}