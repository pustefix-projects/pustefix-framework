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

package org.pustefixframework.util.i18n;

/**
 * Representation of a PO file entry.
 */
public class POMessage {

    private final String messageContext;
    private final String messageId;
    private final String messageIdPlural;
    private final String[] messageStrings;

    /**
     * Creates a message object.
     * 
     * @param messageId  untranslated string
     * @param messageString  translated string
     */
    public POMessage(String messageId, String messageString) {
        this(null, messageId, null, new String[] {messageString});
    }

    /**
     * Creates a message object.
     * 
     * @param messageContext  context
     * @param messageId  untranslated string
     * @param messageIdPlural  untranslated plural string
     * @param messageStrings  translated strings
     */
    public POMessage(String messageContext, String messageId, String messageIdPlural, String[] messageStrings) {
        this.messageContext = messageContext;
        this.messageId = messageId;
        this.messageIdPlural = messageIdPlural;
        this.messageStrings = messageStrings;
    }

    /**
     * Returns the message context.
     * 
     * @return context
     */
    public String getMessageContext() {
        return messageContext;
    }

    /**
     * Returns the untranslated message string.
     * 
     * @return untranslated string
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Returns the untranslated plural message string.
     * 
     * @return untranslated plural string
     */
    public String getMessageIdPlural() {
        return messageIdPlural;
    }

    /**
     * Returns the translated message string(s).
     *
     * @return translated strings
     */
    public String[] getMessageStrings() {
        return messageStrings;
    }

}
