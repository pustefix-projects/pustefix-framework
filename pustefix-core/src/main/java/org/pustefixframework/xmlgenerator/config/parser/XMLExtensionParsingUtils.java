package org.pustefixframework.xmlgenerator.config.parser;

import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.xmlgenerator.config.model.AbstractModelElement;
import org.pustefixframework.xmlgenerator.config.model.XMLExtension;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

public class XMLExtensionParsingUtils {

	@SuppressWarnings("unchecked")
	public static <T extends AbstractModelElement> XMLExtension<T> getListExtension(HandlerContext context) throws ParserException {
		return ParsingUtils.getSingleTopObject(XMLExtension.class , context);
	}

}
