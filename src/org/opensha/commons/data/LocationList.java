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

package org.opensha.commons.data;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;

import org.dom4j.Element;
import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.metadata.XMLSaveable;

/**
 *  <b>Title:</b> LocationList<p>
 *
 * <b>Description:</b> List container for location objects. This class is a
 * specialized version of an ArrayList. Can add special SortedIterator
 * that returns the Locations in geographical order<p>
 *
 * As with an ArrayList this class provides all the standard methods to
 * opereate on elements within a list, such as (paraphrasing)
 * add(), get(), delete(), rep/ace(), num(), Iterator(), clearAll(). <p>
 *
 * Note: It is good design that the the internal data structure (ArrayList)
 * is hidden to the "outside world". This is the idea of encapsulation in
 * object oriented programming. The internal complexity of an object
 * should not be exposed. There is no need for calling classes to know this
 * information so it keeps the API simpler.<p>
 * 
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public class LocationList implements java.io.Serializable, XMLSaveable, Iterable<Location> {

	private static final long serialVersionUID = 0xA9F494E;
	
    /** Class name used for debugging purposes */
    protected final static String C = "LocationList";
    
    public static final String XML_METADATA_NAME = "LocationList";
    /** if true print out debugging statements */
    protected final static boolean D = false;

    /**
     *  Internal data structure (ArrayList ) that contains the
     *  list of Locations.
     */
    protected ArrayList<Location> locations = new ArrayList<Location>();


    /**
     *  Returns parameter at the specified index if exist, else throws
     *  exception. Recall that these locations are stored in a ArrayList, which is
     *  like an array. Therefore you can access items by index.
     *
     * @param  index  Description of the Parameter
     * @return        The locationAt value
     */
    public Location getLocationAt( int index ) throws InvalidRangeException {
        checkIndex(index);
        return (Location)locations.get( index );
    }

    /** Validates that an index falls within the internal data structure range */
    private void checkIndex(int index) throws InvalidRangeException {

        if( size() < index + 1 ) throw new InvalidRangeException(
            C + ": getLocationAt(): " +
            "Specified index larger than array size."
        );

    }

    /**
   *  Replaces the location in the list at the specified index if it is a valid index.
   *  An exception is thrown if the specified index is invalid.
   *
   * @param  location  Location that is to be added
   * @param  index     location in the list at which this event needs to be added.
   */

    public void replaceLocationAt( Location location, int index ) throws InvalidRangeException  {
        checkIndex(index);
        locations.set(index, location);
    }


    /**
     * Sorts the list of locations based on Latitude. If Latitude is same then
     * sorts on Longitude.
     */
    @SuppressWarnings("unchecked")
	public void sort(){
      Collections.sort(locations, new LocationComparator());
    }

    /**
     *  Adds the Locatrion to the end of the list.
     *
     * @param  location  The feature to be added to the Location attribute
     */
    public void addLocation( Location location ) { locations.add(location); }
    
    public void addLocation(double lat, double lon, double depth) {
    	addLocation(new Location(lat, lon, depth));
    }

    
    public void addAllLocations(LocationList locList) {
    	for(int i=0;i<locList.size();i++)
    		addLocation(locList.getLocationAt(i));
    }

    /**
     * Inserts the location at the given index.
     *
     * @param  location  The feature to be added to the Location attribute
     * @param index int Index at which location is to be added
     *
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     */
    public void addLocationAt( Location location, int index ) {
      locations.add(index,location);
    }




    /**
     *  Returns a list iterator of all Locations in this list, in the order they
     *  were added to the list
     *
     * @return    Description of the Return Value
     */
    //public ListIterator<Location> listIterator() { return locations.listIterator(); }


    /**  Removes all Locations from this list */
    public void clear() { locations.clear(); }

    public void remove(int index) {
    	locations.remove(index);
    }
    
    /**
     * Reverses the order of Locations. Has the
     * effect of reversing the Iterator.
     */
    public void reverse(){
    	
        int size = locations.size();
        int reverseIndex = size - 1;

        ArrayList<Location> newList = new ArrayList<Location>();
        for( int i = reverseIndex; i >= 0; i--){
            newList.add( locations.get(i) );
        }

        this.locations = newList;
    }


    /**
     *  Returns the number of Locations in this list
     *
     * @return    Description of the Return Value
     */
    public int size() {
        return locations.size();
    }

    private final static String TAB = "  ";
    /** Helper debugging method that prints out all Locations in this list */
    @Override
	public String toString(){

        StringBuffer b = new StringBuffer();
        b.append('\n');
        b.append(TAB + "Size = " + size());

        Iterator<Location> it = iterator();
        while( it.hasNext() ){

            Location location = it.next();
            b.append(TAB + location.toString());
        }
        return b.toString();
    }

    /**
     * This computes the distance (in km) between the given loc and the closest point in the LocationList
     * @param loc
     * @return
     */
    public double getHorzDistToClosestLocation(Location loc) {
      double min = Double.MAX_VALUE, temp;
      Iterator<Location> it = iterator();
      while(it.hasNext()) {
        temp = RelativeLocation.getHorzDistance(loc,it.next());
        if (temp < min) min = temp;
      }
      return min;
    }


    /**
     * Compares if the 2 LocationList.
     * It checks if both locationlist object contains the same Locations objects.
     * @param obj LocationList Obj
     * @return 0 if both object are same else return -1
     */

	public int compareTo(Object obj) {

		boolean compareFlag = true;
		if (!(obj instanceof LocationList)) {
			throw new ClassCastException(C
					+ "Object not a LocationList, unable to compare");
		}

		LocationList locList = (LocationList) obj;

		Iterator<Location> it = locList.iterator();
		Iterator<Location> it1 = iterator();

		if (size() != locList.size()) {
			return -1;
		}
		
		Location loc = null;
		Location loc1 = null;
		while (it.hasNext()) {
			loc = it.next();
			loc1 = it1.next();
			compareFlag = loc.equals(loc1);
			if (compareFlag == false) {
				break;
			}
		}

		if (!compareFlag) {
			return -1;
		}

		return 0;
	}
    
//    // TODO should also override hashcode
//    @Override
//	public boolean equals(Object obj) {
//    	return compareTo(obj) == 0;
//    }


    /**
     * Returns the index of location with the given location list.
     * @param loc Location finds index of this location
     * @return int index in the List.
     */
    public int getLocationIndex(Location loc){
      return locations.indexOf(loc);
    }


    /**
     * This computes the shortest horizontal distance (in km) from the given loc
     * to any point on the line defined by connecting the points in this location list.
     * This is approximate in that it uses the RelativeLocation.getApproxHorzDistToLine(*) method
     * @param loc
     * @return
     */
    public double getMinHorzDistToLine(Location loc) {
      double min = Double.MAX_VALUE, temp;

      // TODO this should loop over the points and then only solve at the segment ??
      // loop over each line segment
      for(int i=1; i<size(); i++) {
        temp = RelativeLocation.getApproxHorzDistToLine(getLocationAt(i-1),getLocationAt(i),loc);
        if (temp < min) min = temp;
      }
      return min;
    }
    
    public Element toXMLMetadata(Element root) {
    	Element locs = root.addElement(LocationList.XML_METADATA_NAME);
  	  	for (int i=0; i<this.size(); i++) {
  	  		Location loc = this.getLocationAt(i);
  	  		locs = loc.toXMLMetadata(locs);
  	  	}
  	  	
  	  	return root;
    }
    
    public static LocationList fromXMLMetadata(Element locationElement) {
    	LocationList locs = new LocationList();
    	Iterator<Element> it = locationElement.elementIterator();
    	while (it.hasNext()) {
    		Element el = it.next();
    		if (el.getName().equals(Location.XML_METADATA_NAME)) {
    			locs.addLocation(Location.fromXMLMetadata(el));
    		}
    	}

    	return locs;
    }

	/* implementation */
	public Iterator<Location> iterator() {
		return locations.iterator();
	}
	
	public ArrayList<LocationList> split(int pieceSize) {
		ArrayList<LocationList> lists = new ArrayList<LocationList>();
		
		// quickly handle the trivial case
		if (pieceSize <= 0 || this.size() <= pieceSize) {
			lists.add(this);
			return lists;
		}
		
		LocationList cur = new LocationList();
		
		for (int i=0; i<this.size(); i++) {
			if (i % pieceSize == 0 && i > 0) {
				lists.add(cur);
				cur = new LocationList();
			}
			cur.addLocation(this.getLocationAt(i));
		}
		
		if (cur.size() > 0)
			lists.add(cur);
		
		return lists;
	}
	
	/**
	 * Returns an exact copy of this <code>LocationList</code>. This is a
	 * deep copy.
	 * 
	 * TODO should change to clone and implement cloneable?
	 * MAKE CUSTOM CLONE THAT RETURNS LOCATIONLIST
	 * Location should also implement clone that way when copying locations
	 * value range checking won't be required as it is now when each new
	 * Location is initialized
	 * 
	 * @return a copy
	 */
	public LocationList copy() {
		LocationList locList = new LocationList();
		for (Location loc : locations) {
			locList.addLocation(loc.copy());
		}
		return locList;
	}
	
	public LocationList copyImmutable() {
		LocationList locList = new LocationList();
		for (Location loc : locations) {
			locList.addLocation(Location.immutableLocation(loc));
		}
		return locList;
	}
}
