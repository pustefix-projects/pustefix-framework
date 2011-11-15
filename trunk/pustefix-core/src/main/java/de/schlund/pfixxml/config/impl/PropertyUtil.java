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

package de.schlund.pfixxml.config.impl;

/**
 * Provides utility methods for rules reading property style values.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
class PropertyUtil {
    /**
     * Parses a property style values unescaping special sequences.
     * 
     * @param value string to parse
     * @return value with unescaped special sequences
     */
    static String unescapePropertyValue(String value) {
        StringBuffer newValue = new StringBuffer(value.length());
        char aChar;
        int off = 0;
        int end = value.length();

        while (off < end) {
            aChar = value.charAt(off++);
            if (aChar == '\\') {
                aChar = value.charAt(off++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int val = 0;
                    for (int i = 0; i < 4; i++) {
                        try  {
                            aChar = value.charAt(off++);
                        } catch (StringIndexOutOfBoundsException e) {
                            throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                val = (val << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                val = (val << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                val = (val << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                    }
                    newValue.append((char) val);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    newValue.append(aChar);
                }
            } else {
                newValue.append(aChar);
            }
        }
        
        return newValue.toString();
    }

}
