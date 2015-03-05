package de.schlund.pfixxml.util.xsltimpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.pustefixframework.util.LocaleUtils;

import com.icl.saxon.Context;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.om.Axis;
import com.icl.saxon.om.Builder;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.tree.AttributeCollection;

import de.schlund.pfixxml.util.ExtensionFunctionUtils;

/**
 * Some useful extensions function not available until XSLT 2 "backported" to XSLT 1
 *
 */
public class Xslt2BackPortFunctions {

    public static boolean endsWith(String str, String end) {
        return str.endsWith(end);
    }

    public static String upperCase(String str) {
        return str.toUpperCase();
    }

    public static String lowerCase(String str) {
        return str.toLowerCase();
    }
    
    public static boolean matches(String input, String pattern) {
        return Pattern.compile(pattern).matcher(input).find();
    }
    
    public static boolean matches(String input, String pattern, String flags) {
        int flagMask = getPatternFlags(flags);
        return Pattern.compile(pattern, flagMask).matcher(input).find();
    }
    
    public static String replace(String input, String pattern, String replacement) {
        return Pattern.compile(pattern).matcher(input).replaceAll(replacement);
    }
    
    public static String replace(String input, String pattern, String replacement, String flags) {
        int flagMask = getPatternFlags(flags);
        return Pattern.compile(pattern, flagMask).matcher(input).replaceAll(replacement);
    }
        
    private static int getPatternFlags(String flags) {
        int flagMask = 0;
        for(int i=0; i<flags.length(); i++) {
            char ch = flags.charAt(i);
            switch(ch) {
                case 's': flagMask |= Pattern.DOTALL;
                case 'm': flagMask |= Pattern.MULTILINE;
                case 'i': flagMask |= Pattern.CASE_INSENSITIVE;
                case 'x': flagMask |= Pattern.COMMENTS;
            }
        }
        return flagMask;
    }
    
    public static NodeEnumeration tokenize(Context context, String str, String pattern) throws XPathException {
        try {
            Builder builder = context.getController().makeBuilder();
            NamePool pool = context.getController().getNamePool();
            builder.setNamePool(pool);
            builder.startDocument();
            int name = pool.allocate("", "", "token");
            AttributeCollection emptyAtts = new AttributeCollection(pool);
            String[] tokens = str.split(pattern);
            for(String token: tokens) {
                builder.startElement(name, emptyAtts, new int[0], 0);
                builder.characters(token.toCharArray(), 0, token.length());
                builder.endElement(name);
            }
            builder.endDocument();
            DocumentInfo doc = builder.getCurrentDocument();
            return doc.getEnumeration(Axis.CHILD, AnyNodeTest.getInstance());
        } catch (Exception err) {
            ExtensionFunctionUtils.setExtensionFunctionError(err);
            throw new XPathException(err);
        }
    }
    
    public static String formatDate(String dateTime, String datePattern, String timeZone, String dateLocale) throws XPathException {
        
        try {
            SimpleDateFormat format;
            if(dateLocale != null && !dateLocale.isEmpty()) {
                Locale locale = LocaleUtils.getLocale(dateLocale);
                format = new SimpleDateFormat(datePattern, locale);
            } else {
                format = new SimpleDateFormat(datePattern);
            }
            if(timeZone != null && !timeZone.isEmpty()) {
                format.setTimeZone(TimeZone.getTimeZone(timeZone));
            }
            return format.format(new Date(Long.parseLong(dateTime)));
        } catch (Exception err) {
            ExtensionFunctionUtils.setExtensionFunctionError(err);
            throw new XPathException(err);
        }
    }

}
