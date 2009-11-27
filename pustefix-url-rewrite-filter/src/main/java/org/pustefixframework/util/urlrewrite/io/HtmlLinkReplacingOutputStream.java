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

package org.pustefixframework.util.urlrewrite.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

/**
 * Output stream decorator, that replaces strings in the 
 * <code>href</code> attribute of links and the <code>action</code> attribute 
 * of forms in HTML.
 * This class is not thread-safe.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class HtmlLinkReplacingOutputStream extends OutputStream {

    private State state = State.OUTSIDE_TAG;

    private ByteArray tagName = new ByteArray();

    private ByteArray attributeName = new ByteArray();

    private ByteSequenceReplacingOutputStream replacingOutputStream;

    private boolean replacing = false;

    /**
     * Creates a wrapper around <code>out</code>.
     * 
     * @param out output stream this stream writes to
     * @param replacementMap map of strings to search for and their replacements.
     */
    public HtmlLinkReplacingOutputStream(OutputStream out, Map<? extends CharSequence, ? extends CharSequence> replacementMap) {
        replacingOutputStream = new ByteSequenceReplacingOutputStream(out, ByteNodeUtil.generateByteNodeTree(replacementMap));
        replacingOutputStream.setReplacing(replacing);
    }

    /**
     * Creates a wrapper around <code>out</code>. This constructor 
     * can be used instead of 
     * {@link #HtmlLinkReplacingOutputStream(OutputStream, Map)}, if the same 
     * map is used for several streams. This improves the performance as 
     * the map has to be converted to the tree only once.
     * 
     * @param out output stream this stream writes to
     * @param replacementTree tree of byte sequences and their replacements
     */
    public HtmlLinkReplacingOutputStream(OutputStream out, ByteNode<byte[]> replacementTree) {
        replacingOutputStream = new ByteSequenceReplacingOutputStream(out, replacementTree);
        replacingOutputStream.setReplacing(replacing);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int ioff = off;
        for (int i = ioff; i < len; i++) {
            boolean newReplacing = handleByte(b[i]);
            if (!newReplacing && replacing) {
                replacingOutputStream.write(b, ioff, i - ioff);
                ioff = i;
                replacing = newReplacing;
                replacingOutputStream.setReplacing(replacing);
            } else if (newReplacing && !replacing) {
                replacingOutputStream.write(b, ioff, i - ioff + 1);
                ioff = i + 1;
                replacing = newReplacing;
                replacingOutputStream.setReplacing(replacing);
            }
        }
        if (ioff < off + len) {
            replacingOutputStream.write(b, ioff, off + len - ioff);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException {
        boolean newReplacing = handleByte((byte) b);
        if (!newReplacing && replacing) {
            replacing = newReplacing;
            replacingOutputStream.setReplacing(replacing);
            replacingOutputStream.write(b);
        } else if (newReplacing && !replacing) {
            replacingOutputStream.write(b);
            replacing = newReplacing;
            replacingOutputStream.setReplacing(replacing);
        } else {
            replacingOutputStream.write(b);
        }
    }

    @Override
    public void close() throws IOException {
        replacingOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
        replacingOutputStream.flush();
    }

    private boolean handleByte(byte b) {
        // Use special handling for opening and 
        // closing bracket to parse even 
        // malformed documents.
        switch (state) {
        case OUTSIDE_TAG:
            if (b == '<') {
                state = State.BETWEEN_OPENING_BRACKET_AND_SPACE;
                tagName.clear();
            }
            break;
        case BETWEEN_OPENING_BRACKET_AND_SPACE:
            if (b == '-') {
                state = State.STARTING_COMMENT_BETWEEN_MINUS;
                tagName.append(b);
            } else if (isWhitespace(b)) {
                state = State.BETWEEN_OPENING_BRACKET_AND_TAG_NAME;
            } else {
                state = State.IN_TAG_NAME;
                tagName.append(b);
            }
            break;
        case IN_TAG_NAME:
            if (b == '>') {
                state = State.OUTSIDE_TAG;
            } else if (isWhitespace(b)) {
                state = State.BETWEEN_ATTRIBUTES;
            } else {
                tagName.append(b);
            }
            break;
        case BETWEEN_ATTRIBUTES:
            if (isWhitespace(b)) {
                // Do nothing
            } else if (b == '>') {
                state = State.OUTSIDE_TAG;
            } else {
                state = State.IN_ATTRIBUTE_NAME;
                attributeName.clear();
                attributeName.append(b);
            }
            break;
        case IN_ATTRIBUTE_NAME:
            if (isWhitespace(b)) {
                state = State.BETWEEN_ATTRIBUTE_NAME_AND_EQUALS;
            } else if (b == '=') {
                state = State.BETWEEN_EQUALS_AND_ATTRIBUTE_VALUE;
            } else {
                attributeName.append(b);
            }
            break;
        case BETWEEN_ATTRIBUTE_NAME_AND_EQUALS:
            if (isWhitespace(b)) {
                // Do nothing
            } else if (b == '=') {
                state = State.BETWEEN_EQUALS_AND_ATTRIBUTE_VALUE;
            } else {
                state = State.IN_ATTRIBUTE_NAME;
                attributeName.clear();
                attributeName.append(b);
            }
            break;
        case BETWEEN_EQUALS_AND_ATTRIBUTE_VALUE:
            if (b == '"') {
                state = State.IN_ATTRIBUTE_VALUE;
                return handleStartOfAttributeValue();
            } else {
                // Do nothing
            }
            break;
        case IN_ATTRIBUTE_VALUE:
            if (b == '"') {
                state = State.BETWEEN_ATTRIBUTES;
                return handleEndOfAttributeValue();
            } else {
                // Do nothing
            }
            break;
        case STARTING_COMMENT_BETWEEN_MINUS:
            if (b == '-') {
                state = State.IN_COMMENT;
            } else if (isWhitespace(b)) {
                state = State.BETWEEN_ATTRIBUTES;
            } else {
                state = State.IN_TAG_NAME;
                tagName.append(b);
            }
            break;
        case IN_COMMENT:
            if (b == '-') {
                state = State.ENDING_COMMENT_BETWEEN_MINUS;
            } else {
                // Do nothing
            }
            break;
        case ENDING_COMMENT_BETWEEN_MINUS:
            if (b == '-') {
                state = State.ENDING_COMMENT_BETWEEN_MINUS_AND_CLOSING_BRACKET;
            } else {
                state = State.IN_COMMENT;
            }
            break;
        case ENDING_COMMENT_BETWEEN_MINUS_AND_CLOSING_BRACKET:
            if (b == '>') {
                state = State.OUTSIDE_TAG;
            } else {
                state = State.IN_COMMENT;
            }
            break;
        default:
            throw new RuntimeException("Reached code that should never be reached!");
        }
        return replacing;
    }

    private boolean isWhitespace(byte b) {
        if (b == ' ' || b == '\t' || b == '\r' || b == '\n') {
            return true;
        } else {
            return false;
        }
    }

    private boolean handleStartOfAttributeValue() {
        String tagName;
        String attributeName;
        try {
            tagName = new String(this.tagName.getBytes(), "UTF-8").toLowerCase();
            attributeName = new String(this.attributeName.getBytes(), "UTF-8").toLowerCase();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not find charset UTF-8!", e);
        }
        if (tagName.equals("a") && attributeName.equals("href")) {
            return true;
        } else if (tagName.equals("form") && attributeName.equals("action")) {
            return true;
        } else if (tagName.equals("link") && attributeName.equals("href")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean handleEndOfAttributeValue() {
        return false;
    }

    private enum State {
        OUTSIDE_TAG, BETWEEN_OPENING_BRACKET_AND_SPACE, BETWEEN_OPENING_BRACKET_AND_TAG_NAME, IN_TAG_NAME, BETWEEN_ATTRIBUTES, IN_ATTRIBUTE_NAME, BETWEEN_ATTRIBUTE_NAME_AND_EQUALS, BETWEEN_EQUALS_AND_ATTRIBUTE_VALUE, IN_ATTRIBUTE_VALUE, IN_COMMENT, STARTING_COMMENT_BETWEEN_MINUS, ENDING_COMMENT_BETWEEN_MINUS, ENDING_COMMENT_BETWEEN_MINUS_AND_CLOSING_BRACKET

    }

    private class ByteArray {

        byte[] bytes = new byte[128];

        int length = 0;

        public void append(byte b) {
            if (length == bytes.length) {
                bytes = Arrays.copyOf(bytes, bytes.length * 2);
            }
            bytes[length] = b;
            length++;
        }

        public void clear() {
            length = 0;
        }

        public int size() {
            return length;
        }

        public byte[] getBytes() {
            return Arrays.copyOf(bytes, length);
        }
    }
}
