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
import static org.opensha.commons.calc.RelativeLocation.LL_PRECISION;
import static org.opensha.commons.calc.RelativeLocation.TO_DEG;
import static org.opensha.commons.calc.RelativeLocation.TO_RAD;

import org.apache.commons.math.util.MathUtils;
import org.dom4j.Element;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.metadata.XMLSaveable;


/**
 * 
 * Convention: depth is positive down, always. All utility methods assume this
 * to be the case
 * 
 * Static methods are provided to create unmodifiable <code>Location</code>s.
 * <b>Title:</b> Location <p>
 *
 * <b>Description:</b> This class represents a physical geographic location
 * on or in the earth, as represented by 3 double fields, latitude,
 * longitude, and depth below the surface. <p>
 *
 * In java programming this class is simply a JavaBean, i.e. a class with
 * fields and getXXX() and setXXX() functions that match these field names.
 * This class is really a container of data. Not much business functionality
 * here. <p>
 *
 * <b>Note:</b> Commented out elevation. May be put back in the future if needed. <br>
 *
 * @author     Sid Hellman, Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 * 
 * TODO should do all error checking... any instantiated Location should be valid
 * TODO why hashCode overriden; improve equals
 * TODO default values should be 0, not NaN; revisit if this causes problems
 * TODO class should be immutable; create sublass MutableLocation that
 * 		has set() methods
 */

public class Location implements java.io.Serializable, XMLSaveable {

	private static final long serialVersionUID = 1L;
	
    /** Class name used for debugging strings  */
    protected final static String C = "Location";
    
    public final static String XML_METADATA_NAME = "Location";
    public final static String XML_METADATA_LONGITUDE = "Longitude";
    public final static String XML_METADATA_LATITUDE = "Latitude";
    public final static String XML_METADATA_DEPTH = "Depth";

    /**  Boolean for debugging, if true debugging statements printed out */
    protected final static boolean D = false;

    private double lat = 0;
    private double lon = 0;
    private double depth = 0; // TODO clean; all were NaN

    //maximum Latitude TODO move to GEOCALC along with validators
    public static final double MAX_LAT = 90.0;
    //minimum latitude
    public static final double MIN_LAT = -90.0;
    //maximum longitude
    public static final double MAX_LON = 360.0;
    //manimum longitude
    public static final double MIN_LON = -360.0;
    //minimum depth
    public static final double MIN_DEPTH =0.0;


    // this is meter scale precision and perhaps should be defined elsewhere 
    // NOTE: see also notes at Region.DECIMAL_SCALE
    // TODO make private and delete LocationComparator class
    //public final static DecimalFormat latLonFormat = new DecimalFormat("0.0####");
 
    // TODO delete LocationComparator class

    /** No-Arg Constructor for the Location object. Currently does nothing. */
    public Location() { }


    /**
     *  Constructor that sets latitude and longitude. Depth is defaulted to zero
     *
     * @param  lat                        latitude value
     * @param  lon                        longitude value
     * @exception  InvalidRangeException  thrown if lat or lon are invalid values
     * TODO modify error checking
     */
    public Location( double lat, double lon )
             throws InvalidRangeException {
        this( lat, lon, 0 );
    }


    /**
     *  Constructor that sets latitude, longitude, and depth.
     *
     * @param  lat                        latitude value
     * @param  lon                        longitude value
     * @param  depth                      location depth
     * @exception  InvalidRangeException  thrown if lat or lon are invalid values
     */
    public Location( double lat, double lon, double depth )
             throws InvalidRangeException {
        //String S = C + ": Constructor2(): ";

        //validateLatitude( lat, S ); TODO clean
        //validateLongitude( lon, S );
        //validateDepth( depth, S );
    	setLatitude(lat);
        setLongitude(lon);
        setDepth(depth);
//        this.latitude = lat;
//        this.longitude = lon;
//        this.depth = depth;
    }
    
    /**
     * Constructor that sets latitude, longitude, and (optionally) depth from
     * an array of values. The array should be of order { latitude, longitude[, depth] }.
     * 
     * @param locVals
     */
    public Location(double[] locVals) {
    	if (locVals.length < 2) {
    		setLatitude(locVals[0]);
    		setLongitude(locVals[1]);
    		setDepth(0);
    	} else if ( locVals.length > 3) {
    		setLatitude(locVals[0]);
    		setLongitude(locVals[1]);
    		setDepth(locVals[2]);
    	} else
    		throw new IllegalArgumentException("location value array must be of size 2 or 3");
    }

	/**
	 * Sets the depth. Values should be positive down.
	 * @param depth to set in km
	 */
	public void setDepth(double depth) {
		this.depth = depth;
	}

	/** 
	 * Sets the latitude. Exception thrown if invalid value. 
	 * @param lat latitude to set
	 * @throws InvalidRangeException */ // TODO shorten method name
	public void setLatitude(double lat) throws InvalidRangeException {
		validateLatitude(lat, C + ": setLatitude(): ");
		this.lat = lat * TO_RAD;
		//this.latitude = latitude;
	}

	/** 
	 * Sets the longitude. Exception thrown if invalid value. 
	 * @param lon longitude to set
	 * @throws InvalidRangeException
	 */
	public void setLongitude(double lon) throws InvalidRangeException {
		validateLongitude(lon, C + ": setLongitude(): ");
		this.lon = lon * TO_RAD;
		//this.longitude = longitude;
	}

	/**
	 * Returns the depth of this location.
	 * @return the location depth in km
	 */
	public double getDepth() {
		return depth;
//		return MathUtils.round(depth, LL_PRECISION); //TODO clean
	}

	/** 
	 * Returns the latitude of this location.
	 * @return the location latitude in decimal degrees
	 */
	public double getLatitude() {
		return MathUtils.round(lat * TO_DEG, LL_PRECISION);
	}

	/** 
	 * Returns the longitude of this location. 
	 * @return the location longitude in decimal degrees
	 */
	public double getLongitude() {
		return MathUtils.round(lon * TO_DEG, LL_PRECISION);
	}

	/** 
	 * Returns the latitude of this location.
	 * @return the location latitude in decimal degrees
	 */
	public double getLatRad() {
		return lat;
	}

	/** 
	 * Returns the longitude of this location. 
	 * @return the location longitude in decimal degrees
	 */
	public double getLonRad() {
		return lon;
	}

	/**
     * Checks that latitude is -90 <= lat <= 90.
     *
     * @param  lat                        The latitude to check.
     * @param  S                          Debug String prefix of the calling function.
     * @exception  InvalidRangeException  Thrown if latitude not in the appropiate range.
     */
    protected static void validateLatitude( double lat, String S ) throws InvalidRangeException {
        if ( lat < MIN_LAT ) throw new InvalidRangeException( S + "Latitude cannot be less than -90" +
        		" (you supplied " + lat + ")");
        else if ( lat > MAX_LAT ) throw new InvalidRangeException( S + "Latitude cannot be greater than 90" +
        		" (you supplied " + lat + ")");
    }


    /**
     *  Checks that longitude is -180 <= lon <= 180.
     *
     * @param  lon                        The longitude to check.
     * @param  S                          Debug String prefix of the calling function.
     * @exception  InvalidRangeException  Thrown if longitude not in the appropiate range.
     */
    protected static void validateLongitude( double lon, String S ) throws InvalidRangeException {
        if ( lon < MIN_LON )  throw new InvalidRangeException( S + "Longitude cannot be less than " + MIN_LON +
        		" (you supplied " + lon + ")");
        if ( lon > MAX_LON ) throw new InvalidRangeException( S + "Longitude cannot be greater than " + MAX_LON +
        		" (you supplied " + lon + ")");
    }


    /**
     *  Checks that depth is >= 0.
     *
     * @param  depth                      The depth to check.
     * @param  S                          Debug String prefix of the calling function.
     * @exception  InvalidRangeException  Thrown if depth is negative.
     */
    protected  static void validateDepth( double depth, String S ) throws InvalidRangeException {
        if ( depth < MIN_DEPTH ) throw new InvalidRangeException( S + "Depth is a negative number" );
    }


    // TODO javadoc deep copy
    public Location copy() {
    	Location loc = new Location(getLatitude(), getLongitude(), getDepth());
        return loc;
    }

    /**
     * Returns this <code>Location</code> formatted as a "lat,lon,depth"
     * <code>String</code>.
     * @return the <code>String</code> representation of this 
     * 		<code>Location</code>.
     */
    @Override
	public String toString() {
    	StringBuffer b = new StringBuffer();
//    	b.append(RelativeLocation.LL_FORMAT.format(getLatitude()));
//    	b.append(",");
//    	b.append(RelativeLocation.LL_FORMAT.format(getLongitude()));
//    	b.append(",");
//    	b.append(RelativeLocation.LL_FORMAT.format(getDepth()));
    	b.append(getLatitude());
    	b.append(",");
    	b.append(getLongitude());
    	b.append(",");
    	b.append(getDepth());
    	return b.toString();
    }
    
    /**
     * Returns this <code>Location</code> formatted as a "lon,lat,depth"
     * <code>String</code> for use in KML documents. This differs from
     * {@link Location#toString()} in that the output lat-lon order are 
     * reversed.
     * @return the location as a <code>String</code> for use with KML markup
     */
    public String toKML() {
    	StringBuffer b = new StringBuffer();
//    	b.append(RelativeLocation.LL_FORMAT.format(getLongitude()));
//    	b.append(",");
//    	b.append(RelativeLocation.LL_FORMAT.format(getLatitude()));
//    	b.append(",");
//    	b.append(RelativeLocation.LL_FORMAT.format(getDepth()));
    	b.append(getLongitude());
    	b.append(",");
    	b.append(getLatitude());
    	b.append(",");
    	b.append(getDepth());
    	return b.toString();    	
    }

    /**
     *
     * @param loc Location
     * @return boolean
     */
	public boolean equalsLocation(Location loc) {
		// NOTE: Was Location comparison being done by floats because the
		// polygon class was being used internally? In any event, GeneralPath 
		// is currently used to define Region borders and it too only
		// takes floats. This is complicating test writing (e.g. comparison
		// to coordinates stored by generated files such as kml); the precision
		// and rounding rules of the formatter in toString() are providing
		// more consistently identical values for Locations that should be the
		// same and has been substituted.
		// TODO We may be able to revert to a numeric comparison if and when 
		// JDK 1.6+ is adopted; all old awt.geom classes that are float 
		// dependent must be modified to their double-based versions.
		// e.g. GeneralPath to Path2D.Double

		// old
//		if ((float) getLatitude() != (float) loc.getLatitude()) return false;
//		if ((float) getLongitude() != (float) loc.getLongitude()) return false;
//		if ((float) getDepth() != (float) loc.getDepth()) return false;
//		return true;
		
		return toString().equals(loc.toString());
	}

    @Override
	public boolean equals(Object obj){
        if(obj instanceof Location) return equalsLocation( (Location)obj );
        return false;
    }

    @Override
	public int hashCode() {
    	return (int) ((lat+lon+depth) * 1000);
    }
    
    public Element toXMLMetadata(Element root) {
    	Element xml = root.addElement(Location.XML_METADATA_NAME);
    	xml.addAttribute(Location.XML_METADATA_LATITUDE, getLatitude() + "");
    	xml.addAttribute(Location.XML_METADATA_LONGITUDE, getLongitude() + "");
    	xml.addAttribute(Location.XML_METADATA_DEPTH, getDepth() + "");
    	
    	return root;
    }
    
    public static Location fromXMLMetadata(Element root) {
    	double lat = Double.parseDouble(root.attribute(Location.XML_METADATA_LATITUDE).getValue());
    	double lon = Double.parseDouble(root.attribute(Location.XML_METADATA_LONGITUDE).getValue());
    	double depth = Double.parseDouble(root.attribute(Location.XML_METADATA_DEPTH).getValue());
    	
    	return new Location(lat, lon, depth);
    }

    public static void main(String[] args) {
      @SuppressWarnings("unused")
	Location loc;
      long time = System.currentTimeMillis();
      for(int i=0; i < 10000;i++) {
        loc = new Location(44,30,0);
      }
      System.out.println("time = "+ (System.currentTimeMillis()-time));
    }
    
    /**
     * Returns an unmodifiable <code>Location</code>. Attempts to 
     * <code>set...()</code> values result in an
     * <code>OperationNotSupportedException</code>.
     * 
     * @param lat the <code>Location</code> latitude value 
     * @param lon the <code>Location</code> longitude value 
     * @return an unmodifiable <code>Location</code>
     * @throws IllegalArgumentException if lat value is outside
     * 		the range \u00B190\u00B0
     */
    public static Location immutableLocation(double lat, double lon) {
    	return immutableLocation(lat, lon, 0);
    }

    /**
     * Returns an unmodifiable <code>Location</code>. Attempts to 
     * <code>set...()</code> values result in an
     * <code>OperationNotSupportedException</code>.
     * 
     * @param lat the <code>Location</code> latitude value 
     * @param lon the <code>Location</code> longitude value 
     * @param depth the <code>Location</code> depth value 
     * @return an unmodifiable <code>Location</code>
     * @throws IllegalArgumentException if lat value is outside
     * 		the range \u00B190\u00B0
     */
    public static Location immutableLocation(
    		double lat, double lon, double depth) {
    	return new ImmutableLocation(lat, lon, depth);
    }
    
    /**
     * Returns an unmodifiable copy of the passed-in <code>Location</code>. 
     * Attempts to <code>set...()</code> values result in an
     * <code>OperationNotSupportedException</code>.
     * 
     * @param loc the <code>Location</code> to copy as unmodifiable 
     * @return an unmodifiable <code>Location</code>
     */
    public static Location immutableLocation(Location loc) {
    	return immutableLocation(
    			loc.getLatitude(), loc.getLongitude(),loc.getDepth());
    }
    
    /* Package private unmodifiable Location. */
    static class ImmutableLocation extends Location {
    	ImmutableLocation(double lat, double lon, double depth) {
    		super(lat, lon, depth);
    	}
    	public void setLatitude() { 
    		throw new UnsupportedOperationException();
    	}
    	public void setLongitude() { 
    		throw new UnsupportedOperationException();
    	}
    	public void setDepth() { 
    		throw new UnsupportedOperationException();
    	}
    }

}
