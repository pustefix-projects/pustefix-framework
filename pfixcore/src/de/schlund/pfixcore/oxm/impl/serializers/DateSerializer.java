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
			pattern=dateAnno.value();
		}
	}
		
	public String serialize(Object obj, SerializationContext context) throws SerializationException {
		SimpleDateFormat dateFormat=new SimpleDateFormat(pattern);
		if(obj instanceof Date) {
			return dateFormat.format((Date)obj);
		} else if(obj instanceof Calendar) {
			return dateFormat.format(((Calendar)obj).getTime());
		} throw new SerializationException("Type not supported: "+obj.getClass().getName());
	}
	
	public static void main(String[] args) throws Exception {
		Calendar cal=Calendar.getInstance();
		DateSerializer ds=new DateSerializer("yyyy-MM-dd'T'HH:mm:ssZ");
		System.out.println(ds.serialize(cal,null));
	}
	
}
