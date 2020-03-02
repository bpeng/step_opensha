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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Class representing an N to N mapping. Similar to the java Map interface, but returns Collection's of values
 * for each key instead of individual values.
 * 
 * @author Kevin Milner
 *
 * @param <Element1>
 * @param <Element2>
 */
public class NtoNMap<Element1, Element2> {
	
	public HashMap<Element1, Collection<Element2>> oneToTwoMap = new HashMap<Element1, Collection<Element2>>();
	
	public HashMap<Element2, Collection<Element1>> twoToOneMap = new HashMap<Element2, Collection<Element1>>();
	
	private int size = 0;
	
	public NtoNMap() {
		
	}
	
	public void clear() {
		oneToTwoMap.clear();
		twoToOneMap.clear();
		size = 0;
	}
	
	/**
	 * Add the mapping to the map
	 * 
	 * @param elem1
	 * @param elem2
	 */
	public void put(Element1 elem1, Element2 elem2) {
		Collection<Element2> twos = oneToTwoMap.get(elem1);
		Collection<Element1> ones = twoToOneMap.get(elem2);
		
		if (ones == null) {
			ones = new ArrayList<Element1>();
			twoToOneMap.put(elem2, ones);
		}
		if (twos == null) {
			twos = new ArrayList<Element2>();
			oneToTwoMap.put(elem1, twos);
		}
		
		if (!ones.contains(elem1) && !twos.contains(elem2)) {
			ones.add(elem1);
			twos.add(elem2);
			size++;
		}
	}
	
	/**
	 * Add all mappings from the given map
	 * 
	 * @param map
	 */
	public void putAll(NtoNMap<Element1, Element2> map) {
		for (Element1 one : map.getOnes()) {
			for (Element2 two : map.getTwos(one)) {
				this.put(one, two);
			}
		}
	}
	
	/**
	 * Get all of the Ones
	 * 
	 * @return Set<Element1>
	 */
	public Set<Element1> getOnes() {
		return oneToTwoMap.keySet();
	}
	
	/**
	 * Get all of the ones for the given two
	 * 
	 * @param two
	 * @return
	 */
	public Collection<Element1> getOnes(Element2 two) {
		return twoToOneMap.get(two);
	}
	
	/**
	 * Get all of the Twos
	 * 
	 * @return
	 */
	public Set<Element2> getTwos() {
		return twoToOneMap.keySet();
	}
	
	/**
	 * Get all of the twos for the given one
	 * 
	 * @param one
	 * @return
	 */
	public Collection<Element2> getTwos(Element1 one) {
		return oneToTwoMap.get(one);
	}
	
	/**
	 * Returns true if the specified mapping exists
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	public boolean containsMapping(Element1 one, Element2 two) {
		Collection<Element1> ones = this.getOnes(two);
		if (ones == null)
			return false;
		
		for (Element1 oneTest : ones) {
			if (oneTest.equals(one))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Get the number of unique mappings
	 * 
	 * @return
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Returns true if the size is 0
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return this.size() == 0;
	}
	
	/**
	 * Remove a mapping
	 * 
	 * @param one
	 * @param two
	 * @return success
	 */
	public boolean remove(Element1 one, Element2 two) {
		Collection<Element1> ones = getOnes(two);
		Collection<Element2> twos = getTwos(one);
		
		if (ones != null && twos != null) {
			if (ones.contains(one) && twos.contains(two)) {
				boolean success = ones.remove(one) && twos.remove(two);
				if (success) {
					size--;
					return true;
				}
			}
		}
		return false;
	}

}
