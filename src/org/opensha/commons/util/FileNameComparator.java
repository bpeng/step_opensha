/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.commons.util;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;

public class FileNameComparator implements Comparator<File> {
	// A Collator does string comparisons
	private Collator c = Collator.getInstance();
	
	/**
	 * This is called when you do Arrays.sort on an array or Collections.sort on a collection (IE ArrayList).
	 * 
	 * It simply compares their names using a Collator. It doesn't know how to compare
	 * a file with a directory, and returns -1 in this case.
	 */
	public int compare(File f1, File f2) {
		if(f1 == f2)
			return 0;

		// check if it is comparing a file with a directory, and if so return -1
		if(f1.isDirectory() && f2.isFile())
			return -1;
		if(f1.isFile() && f2.isDirectory())
			return 1;


		// let the Collator do the string comparison, and return the result
		return c.compare(f1.getName(), f2.getName());
	}
}
