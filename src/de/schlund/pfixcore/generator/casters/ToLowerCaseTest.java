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

package de.schlund.pfixcore.generator.casters;

import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.SimpleRequestParam;
import junit.framework.TestCase;

/**
 * @author tom
 *
 */
public class ToLowerCaseTest extends TestCase {
	ToLowerCase toLower = new ToLowerCase();
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	private void assertLower(String s1, String s2) {
		toLower.castValue(new RequestParam[] {new SimpleRequestParam(s1)});
		assertEquals(s2, (String)toLower.getValue()[0]);
	}

	public void testGetValue() {
		assertLower("HaLlo", "hallo");
		assertLower("Route66", "route66");
		assertLower("Who am I", "who am i");
		assertLower("Groß", "groß");
		assertLower("BüböMÄÖÜ$%&!", "bübömäöü$%&!");
		assertLower("©=(){}[]Æ§", "©=(){}[]æ§");
		assertLower("#+*~-_", "#+*~-_");
	}

}
