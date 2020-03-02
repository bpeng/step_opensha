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

package org.opensha.commons.calc;

import static java.lang.Math.PI;

import java.text.DecimalFormat;

import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Location;

/**
 * This class contains static utility methods to operate on geographic
 * <code>Location</code> data.
 * <br>
 * <br>
 * See: <a href="http://williams.best.vwh.net/avform.htm#Dist" target="_blank">
 * Aviation Formulary</a> for formulae implemented in this class as well as
 * <a href="http://www.movable-type.co.uk/scripts/latlong.html"
 * target="_blank">Moveable Type Scripts</a> for other implementations.
 * 
 * TODO rename to GeographicUtils or GeoTools or similar after region merge
 * TODO should Location store lat lon internally in radians
 * TODO should exceptions be thrown for separations large enough to produce
 * 		bad values in any of the 'fast' methods
 *
 * @author Steven W. Rock
 * @author Peter Powers
 * @version $Id: RelativeLocation.java 6403 2010-03-08 04:19:31Z bpeng $
 * @see Location
 */
public final class RelativeLocation {
	
	/*
	 * Developer Notes: All experimental, exploratory and test methods were moved
	 * to the RelativeLocationTest.java. On the basis of various experiments,
	 * older methods to calculate distance were replaced with updated versions,
	 * many of which leverage spherical geometry to produce more accurate
	 * results. Some 'fast' versions were updated as well. All legacy
	 * methods, however, are preserved in RelativeLocationTest.java where
	 * comparison tests can be rerun.
	 * 
	 * Most methods take Locations exclusively as arguments. This alleviates
	 * any error checking that must otherwise be performed on user supplied
	 * lat-lon values. It also alleviates the need for expensive degree-radian
	 * conversions by using radians, the native format for Locations,
	 * exclusively.
	 */

	/* No instantiation allowed */
	private RelativeLocation() {}
	
	/** Conversion multiplier for degrees to radians */
	public static final double TO_RAD = Math.toRadians(1.0);

	/** Conversion multiplier for radians to degrees */
	public static final double TO_DEG = Math.toDegrees(1.0);
	
	/** Convenience constant for 2 * PI */
	public static final double TWOPI = 2*PI;
	
	/** Convenience constant for PI / 2 */
	public static final double PI_BY_2 = PI/2;

	/**
	 * The Authalic mean radius (A<subscript>r</subscript>) of the earth 
	 * [6371.0072 km] (see <a 
	 * href="http://en.wikipedia.org/wiki/Earth_radius#Authalic_radius" 
	 * target="_blank">Wikipedia</a>).
	 */
	public static final double EARTH_RADIUS_MEAN = 6371.0072;

	/**
	 * The equatorial radius of the earth [6378.1370 km] (see 
	 * <a href="http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius" 
	 * target="_blank">Wikipedia</a>) as derived from the WGS-84 ellipsoid.
	 */
	public static final double EARTH_RADIUS_EQUATORIAL = 6378.1370; 
	
	/**
	 * The polar radius of the earth [6356.7523 km] (see <a 
	 * href="http://en.wikipedia.org/wiki/Earth_radius#Polar_radius" 
	 * target="_blank">Wikipedia</a>) as derived from the WGS-84 ellipsoid.
	 */
	public static final double EARTH_RADIUS_POLAR = 6356.7523;
	
	
	// NOTE: This vlaue is used to 'clean' decimal values that have been 
	// subject to narrowing conversions when creating border Areas. For
	// lat-lon values as high as 180, floats allow for 5 decimal places
	// which equates to m-scale precision. In the case of a buffered region, 
	// this precision loss carries over to the 'border' because it is the one
	// type of region whose border is intialized from the area, not the
	// other way around.
	//
	// If/when JRE 6 becomes standard requirement, new methods and classes 
	// will alleviate the ned for thisnote and allow greater precision
	// (sub-meter scale). Specifically:
	//
	// Region.createArea(LocationList) should be updated to use Path2D.Double()
	//
	// the offset in Region(Location, Location) should be decreased to some
	// value that matches the precision of the Location toString() output
	// format precision
	//
	// the Region test case data arrays will also have to be regenerated
	//
	// note also that with JRE6 Point2D.Double is serializable and DataPoint2D
	// could be retired.
	
	/** The decimal precision imposed on latitude and longitude values. */
	public static final int LL_PRECISION = 5;

	/** 
	 * The formatting imposed on latitude and longitude values to maintain
	 * consistency with <code>LL_PRECISION</code>.
	 */
	public static final DecimalFormat LL_FORMAT = new DecimalFormat("0.0####");

	/**
	 * <code>Enum</code> used indicate sidedness of points with
	 * respect to a line.
	 */
	public enum Side {
		/** Indicates a point is on the right side of a line. */
		RIGHT,
		/** Indicates a point is on the left side of a line. */
		LEFT,
		/** Indicates a point is on the a line. */
		ON;
	}

	/**
	 * Calculates the angle between two <code>Location</code>s using the 
	 * <a href="http://en.wikipedia.org/wiki/Haversine_formula" target="_blank">
	 * Haversine</a> formula. This method properly handles values spanning
	 * &#177;180&#176;. See 
	 * <a href="http://williams.best.vwh.net/avform.htm#Dist">
	 * Aviation Formulary</a> for source. Result is returned in radians.
	 * 
	 * @param p1 the first <code>Location</code> point
	 * @param p2 the second <code>Location</code> point
	 * @return the angle between the points (in radians)
	 */
	public static double angle(Location p1, Location p2) {
		double lat1 = p1.getLatRad();
		double lat2 = p2.getLatRad();
		double sinDlatBy2 = Math.sin((lat2 - lat1) / 2.0);
		double sinDlonBy2 = Math.sin((p2.getLonRad() - p1.getLonRad()) / 2.0);
		// half length of chord connecting points
		double c = (sinDlatBy2 * sinDlatBy2) +
				   (Math.cos(lat1) * Math.cos(lat2) * sinDlonBy2 * sinDlonBy2);
		return 2.0 * Math.atan2(Math.sqrt(c), Math.sqrt(1-c));
	}
	
	/**
	 * Calculates the great circle surface distance between two
	 * <code>Location</code>s using the Haversine formula for 
	 * computing the angle between two points. For a faster, but less
	 * accurate implementation at large separations, see 
	 * {@link #surfaceDistanceFast(Location, Location)}.
	 * 
	 * @param p1 the first <code>Location</code> point
	 * @param p2 the second <code>Location</code> point
	 * @return the distance between the points in km
	 * @see #angle(Location, Location)
	 * @see #surfaceDistanceFast(Location, Location)
	 */
	public static double surfaceDistance(Location p1, Location p2) {
		return EARTH_RADIUS_MEAN * angle(p1,p2);
	}
	
	/**
	 * Calculates approximate distance between two <code>Location</code>s.
	 * This method is about 2 orders of magnitude faster than 
	 * <code>surfaceDistance()</code>, but is imprecise at large distances. 
	 * Method uses the latitudinal and longitudinal differences between the 
	 * points as the sides of a right triangle. The longitudinal distance is 
	 * scaled by the cosine of the mean latitude.<br/>
	 * <br/>
	 * <b>Note:</b> This method does <i>NOT</i> support values spanning 
	 * #177;180&#176; and fails where the numeric angle exceeds 180&#176;. Use
	 * {@link #surfaceDistance(Location, Location)} in such
	 * instances.
	 * 
	 * @param p1 the first <code>Location</code> point
	 * @param p2 the second <code>Location</code> point
	 * @return the distance between the points in km
	 * @see #surfaceDistance(Location, Location)
	 */
	public static double surfaceDistanceFast(Location p1, Location p2) {
		// modified from J. Zechar:
		// calculates distance between two points, using formula
		// as specifed by P. Shebalin via email 5.8.2004
		double lat1 = p1.getLatRad();
		double lat2 = p2.getLatRad();
		double dLat = lat1 - lat2;
		double dLon = (p1.getLonRad() - p2.getLonRad()) * 
				Math.cos((lat1 + lat2) * 0.5);
		return  EARTH_RADIUS_MEAN * Math.sqrt((dLat*dLat) + (dLon*dLon));
	}

	/**
	 * Calculates the distance in three dimensions between two
	 * <code>Location</code>s using spherical geometry. Method returns the 
	 * straight line distance taking into account the depths of the points.
	 * For a faster, but less accurate implementation at large separations,
	 * see {@link #linearDistanceFast(Location, Location)}.
	 * 
	 * @param p1 the first <code>Location</code> point
	 * @param p2 the second <code>Location</code> point
	 * @return the distance in km between the points
	 * @see #linearDistanceFast(Location, Location)
	 */
	public static double linearDistance(Location p1, Location p2) {
		double alpha = angle(p1,p2);
		double R1 = EARTH_RADIUS_MEAN - p1.getDepth();
		double R2 = EARTH_RADIUS_MEAN - p2.getDepth();
		double B = R1 * Math.sin(alpha);
		double C = R2 - R1 * Math.cos(alpha);
		return Math.sqrt(B*B + C*C);
	}
	
	/**
	 * Calculates the approximate linear distance in three dimensions
	 * between two <code>Location</code>s. This simple and speedy
	 * implementation uses the Pythagorean theorem, treating horizontal 
	 * and vertical separations as orthogonal.<br/>
	 * <br/>
	 * <b>Note:</b> This method is very imprecise at large separations and
	 * should not be used for points &gt;200km apart. If an estimate of
	 * separation distance is not known in advance use
	 * {@link #linearDistance(Location, Location)} for
	 * more reliable results.
	 * 
	 * @param p1 the first <code>Location</code> point
	 * @param p2 the second <code>Location</code> point
	 * @return the distance in km between the points
	 * @see #linearDistance(Location, Location)
	 */
	public static double linearDistanceFast(Location p1, Location p2) {
		double h = surfaceDistanceFast(p1, p2);
		double v = getVertDistance(p1, p2);
		return Math.sqrt(h*h + v*v);
	}

	/**
	 * Computes the initial azimuth (bearing) when moving from one
	 * <code>Location</code> to another. See 
	 * <a href="http://williams.best.vwh.net/avform.htm#Crs">
	 * Aviation Formulary</a> for source. For back azimuth, 
	 * reverse the <code>Location</code> arguments. Result is returned in
	 * radians over the interval 0 to 2&pi;.<br/>
	 * <br/>
	 * <b>Note:</b> It is more efficient to use this method for computation 
	 * because <code>Location</code>s store lat and lon in radians internally.
	 * Use <code>azimuth(Location, Location)</code> for presentation.
	 * 
	 * @param p1 the first <code>Location</code> point
	 * @param p2 the second <code>Location</code> point
	 * @return the azimuth (bearing) from p1 to p2 in radians
	 * @see #azimuth(Location, Location)
	 */
	public static double azimuthRad(Location p1, Location p2) {
		
		double lat1 = p1.getLatRad();
		//double lon1 = p1.getLonRad(); TODO clean
		double lat2 = p2.getLatRad();
		//double lon2 = p2.getLonRad();
		
		// check the poles using a small number ~ machine precision
		if (isPole(p1)) {
			return ((lat1 > 0) ? PI : 0); // N : S pole
		}
		
		// for starting points other than the poles:
		double dLon = p2.getLonRad() - p1.getLonRad();
		double cosLat2 = Math.cos(lat2);
		double azRad = Math.atan2(
				Math.sin(dLon) * cosLat2,
				Math.cos(lat1) * Math.sin(lat2) - 
				Math.sin(lat1) * cosLat2 * Math.cos(dLon));

		return (azRad + TWOPI) % TWOPI;
	}

	/**
	 * Computes the initial azimuth (bearing) when moving from one
	 * {@link Location} to another in degrees. See 
	 * <a href="http://williams.best.vwh.net/avform.htm#Crs">
	 * Aviation Formulary</a> for source. For back azimuth, 
	 * reverse the <code>Location</code> arguments. Result is returned in
	 * decimal degrees over the interval 0&#176; to 360&#176;.
	 * 
	 * @param p1 the first <code>Location</code> point
	 * @param p2 the second <code>Location</code> point
	 * @return the azimuth (bearing) from p1 to p2 in decimal degrees
	 * @see #azimuthRad(Location, Location)
	 */
	public static double azimuth(Location p1, Location p2) {
		return azimuthRad(p1, p2) * TO_DEG;
	}
	
	/**
	 * Computes a <code>Location</code> given an origin point, bearing, and 
	 * distance. See <a href="http://williams.best.vwh.net/avform.htm#LL">
	 * Aviation Formulary</a> for source.
	 * 
	 * @param origin or starting location point
	 * @param bearing in radians away from origin
	 * @param distance along bearing
	 * @return the end location 
	 */
	public static Location location(
			Location origin, double bearing, double distance) {
		double lat1 = origin.getLatRad();
		double lon1 = origin.getLonRad();
		double sinLat1 = Math.sin(lat1);
		double cosLat1 = Math.cos(lat1);
		double d = distance / EARTH_RADIUS_MEAN; // angular distance
		double sinD = Math.sin(d);
		double cosD = Math.cos(d);
		
		double lat2 = Math.asin(
				sinLat1 * cosD +
				cosLat1 * sinD * Math.cos(bearing));
		
		double lon2 = lon1 + Math.atan2(
				Math.sin(bearing) * sinD * cosLat1,
				cosD - sinLat1 * Math.sin(lat2));
		
		//double lon2 = ((lon1 + dlon + PI) % TWOPI ) - PI;
		
		return new Location(lat2 * TO_DEG, lon2 * TO_DEG);
	}
	
	////////////////////////////////////
	
	 /*
	  * OLD COMMENTS FROM CLASS
	  * 
	 *  These calculations are an adoption from Frankel's FORTRAN code. Perhaps
	 *  a better code base that is more accurate is the geod.exe program from the
	 *  USGS. This code incorporates the idea of ellipsiod models into the program.
	 *  This code is written in c, and can certainly be adopted to java. The geod
	 *  program is part of the Proj codebase. Here are some reference URLS:
	 *
	 *  <UL>
	 *  <LI>http://kai.er.usgs.gov/intro/MAPGENdetails.html
	 *  <LI>ftp://kai.er.usgs.gov/pub/Proj.4/
	 *  <LI>http://www.geog.fu-berlin.de:/cgi-bin/man2html/usr/local/man/man1/geod.1#index
	 *  </UL>
	 *  <p>
*/
	
	/** Earth radius constant */
	public final static int R = 6367;

	/** Radians to degrees conversion constant */
	public final static double RADIANS_CONVERSION = Math.PI / 180;

	/** Degree to Km conversion at equator */
	public final static double D_COEFF = 111.11;


	/**
	 * OLD METHOD
	 * 
	 *  By passing in two Locations this calculator will determine the
	 *  Distance object between them. The four fields calculated are:
	 *
	 * <uL>
	 * <li>horzDistance
	 * <li>azimuth
	 * <li>backAzimuth
	 * <li>vertDistance
	 * </ul>
	 *
	 * @param  location1							First geographic location
	 * @param  location2							Second geographic location
	 * @return									  The direction, decomposition of the vector between two locations
	 * @exception  UnsupportedOperationException	Thrown if the Locations contain bad data such as invalid latitudes
	 * @see	 Distance							to see the field definitions
	 */
	public static Direction getDirection( Location location1, Location location2 ) throws UnsupportedOperationException {

		Direction dir = new Direction();

		double lat1 = location1.getLatitude();
		double lon1 = location1.getLongitude();
		double lat2 = location2.getLatitude();
		double lon2 = location2.getLongitude();

		double horzDistance = getHorzDistance(lat1, lon1, lat2, lon2);
		double azimuth = getAzimuth(lat1, lon1, lat2, lon2);
		double backAzimuth = getBackAzimuth(lat1, lon1, lat2, lon2);
		double vertDistance = location2.getDepth() - location1.getDepth();

		dir.setHorzDistance(horzDistance);
		dir.setAzimuth(azimuth);
		dir.setBackAzimuth(backAzimuth);
		dir.setVertDistance(vertDistance);

		return dir;
	}

	/**
	 * OLD METHOD
	 * 
	 * This computes the total distance in km.
	 */
	public static double getTotalDistance(Location loc1, Location loc2) {
		double hDist = getHorzDistance(loc1, loc2);
		double vDist = getVertDistance(loc1, loc2);
		return  Math.sqrt(hDist*hDist+vDist*vDist);
	}

	/**
	 * Returns the vertical separation between two <code>Location</code>s. The
	 * returned value is not absolute and preserves the sign of the difference
	 * between the points.
	 * @param p1 the first <code>Location</code> point
	 * @param p2 the first <code>Location</code> point
	 * @return the vertical separation between the points
	 */
	public static double getVertDistance(Location p1, Location p2) {
		return  p2.getDepth() - p1.getDepth();
	}

	/**
	 *  Given a Location and a Direction object, this function calculates a
	 *  second Location the Direction points to (only the azimuth is used;
	 * backAzimuth is ignored). The fields calculated for the
	 *  second Location are:
	 *
	 * <uL>
	 * <li>Lat
	 * <li>Lon
	 * <li>Depth
	 * </ul>
	 *
	 * @param  location1							First geographic location
	 * @param  direction							Direction object pointing to second Location
	 * @return location2							The second location
	 * @exception  UnsupportedOperationException	Thrown if the Location or Direction contain bad data such as invalid latitudes
	 * @see	 Location							to see the field definitions
	 */
	public static Location getLocation( Location location, Direction direction ) throws UnsupportedOperationException {

		double lat1 = location.getLatitude();
		double lon1 = location.getLongitude();
		double depth = location.getDepth();

		double azimuth = direction.getAzimuth();
		double horzDistance = direction.getHorzDistance();
		double vertDistance = direction.getVertDistance();

		//
		double newLat = getLatitude( horzDistance, azimuth, lat1, lon1 );
		double newLon= getLongitude( horzDistance, azimuth, lat1, lon1 );
//		double newDepth = depth + -1*vertDistance;
		double newDepth = depth + vertDistance;

		//
		Location newLoc = new Location(newLat, newLon, newDepth);
		return newLoc;

	}

	/**
	 * OLD METHOD
	 * 
	 * Internal helper method that calculates the latitude of a second location
	 * given the input location and direction components
	 *
	 * @param delta			 Horizontal distance
	 * @param azimuth		   angle towards new point
	 * @param lat			   latitude of original point
	 * @param lon			   longitude of original point
	 * @return				  latitude of new point
	 */
	private static double getLatitude( 
			double delta, double azimuth, double lat, double lon){

		delta = ( delta / D_COEFF ) * RADIANS_CONVERSION;

		double sdelt= Math.sin( delta );
		double cdelt= Math.cos( delta );

		double xlat = lat * RADIANS_CONVERSION;
		//double xlon = lon * RADIANS_CONVERSION;

		double az2 = azimuth * RADIANS_CONVERSION;

		double st0 = Math.cos( xlat );
		double ct0 = Math.sin( xlat );

		//double phi0 = xlon;

		double cz0 = Math.cos( az2 );
		double ct1 = ( st0 * sdelt * cz0 ) + ( ct0 * cdelt );

		double x = (st0 * cdelt ) - ( ct0 * sdelt * cz0 );
		double y = sdelt * Math.sin( az2 );

		double st1 =  Math.pow( ( ( x * x ) + ( y * y ) ), .5 );
		//double dlon = Math.atan2( y, x );

		double newLat = Math.atan2( ct1, st1 ) / RADIANS_CONVERSION;
		return newLat;
	}


	/**
	 * 
	 * OLD METHOD
	 * 
	 * Internal helper method that calculates the longitude of a second location
	 * given the input location and direction components
	 *
	 * @param delta			 Horizontal distance
	 * @param azimuth		   angle towards new point
	 * @param lat			   latitude of original point
	 * @param lon			   longitude of original point
	 * @return				  longitude of new point
	 */
	private static double getLongitude(
			double delta, double azimuth, double lat, double lon){

		delta = ( delta / D_COEFF ) * RADIANS_CONVERSION;

		double sdelt= Math.sin( delta );
		double cdelt= Math.cos( delta );

		double xlat = lat * RADIANS_CONVERSION;
		double xlon = lon * RADIANS_CONVERSION;

		double az2 = azimuth * RADIANS_CONVERSION;

		double st0 = Math.cos( xlat );
		double ct0 = Math.sin( xlat );

		double phi0 = xlon;

		double cz0 = Math.cos( az2 );
		// double ct1 = ( st0 * sdelt * cz0 ) + ( ct0 * cdelt );

		double x = (st0 * cdelt ) - ( ct0 * sdelt * cz0 );
		double y = sdelt * Math.sin( az2 );

		//  double st1 =  Math.pow( ( ( x * x ) + ( y * y ) ), .5 );
		double dlon = Math.atan2( y, x );

		double newLon = ( phi0 + dlon ) / RADIANS_CONVERSION;
		return newLon;
	}


	/**
	 * OLD METHOD
	 * 
	 * This computes the approximate horizontal distance (in km) using
	 * the standard cartesian coordinate transformation.  Not implemented 
	 * correctly is lons straddle 360 or 0 degrees!
	 */
	private static double getApproxHorzDistance(
			double lat1, double lon1, double lat2, double lon2 ){
	  double d1 = (lat1-lat2)*111.111;
	  double d2 = (lon1-lon2)*111.111*Math.cos(((lat1+lat2)/(2))*Math.PI/180);
	  return Math.sqrt(d1*d1+d2*d2);
	}

	/**
	 * OLD METHOD
	 * 
	 * Second way to calculate the distance between two points. Obtained 
	 * off the internet, but forgot where now. When used in comparision with
	 * the latLonDistance function you see they give practically the same 
	 * values at the equator, and only start to diverge near the
	 * poles, but still reasonable close to each other. Good for point 
	 * of comparision.
	 */
	private static double getHorzDistance(
			double lat1, double lon1, double lat2, double lon2 ){

		double xlat = lat1 * RADIANS_CONVERSION;
		double xlon = lon1 * RADIANS_CONVERSION;

		double st0 = Math.cos( xlat );
		double ct0 = Math.sin( xlat );

		double phi0 = xlon;

		xlat = lat2 * RADIANS_CONVERSION;
		xlon = lon2 * RADIANS_CONVERSION;

		double st1 = Math.cos(xlat);
		double ct1 = Math.sin(xlat);


		double sdlon = Math.sin( xlon - phi0);
		double cdlon = Math.cos( xlon - phi0);

		double cdelt = ( st0 * st1 * cdlon ) + ( ct0 * ct1 );

		double x = ( st0 * ct1 ) - ( st1 * ct0 * cdlon );
		double y = st1 * sdlon;

		double sdelt=  Math.pow( ( ( x * x ) + ( y * y ) ), .5 );
		double delta = Math.atan2( sdelt, cdelt ) / RADIANS_CONVERSION;

		delta = delta * D_COEFF;

		return delta;
	}

	/**
	 * OLD METHOD
	 */
	public static double getApproxHorzDistance(Location loc1, Location loc2) {
		return getApproxHorzDistance(
				loc1.getLatitude(), loc1.getLongitude(), 
				loc2.getLatitude(), loc2.getLongitude());
	}


	/**
	 * OLD METHOD
	 */
	public static double getHorzDistance(Location loc1, Location loc2) {
		return getHorzDistance(
				loc1.getLatitude(), loc1.getLongitude(),
				loc2.getLatitude(), loc2.getLongitude());
	}

	/**
	 * OLD METHOD
	 * 
	 * Helper method that calculates the angle between two locations 
	 * (value returned is between -180 and 180 degrees)
	 * on the earth.<p>
	 *
	 * @param lat1			   latitude of first point
	 * @param lon1			   longitude of first point
	 * @param lat2			   latitude of second point
	 * @param lon2			   longitude of second point
	 * @return				  angle between the two lat/lon locations
	 */
	private static double getAzimuth(
			double lat1, double lon1, double lat2, double lon2 ){

		double xlat = lat1 * RADIANS_CONVERSION;
		double xlon = lon1 * RADIANS_CONVERSION;

		double st0 = Math.cos( xlat );
		double ct0 = Math.sin( xlat );

		double phi0 = xlon;

		xlat = lat2 * RADIANS_CONVERSION;
		xlon = lon2 * RADIANS_CONVERSION;

		double st1 = Math.cos(xlat);
		double ct1 = Math.sin(xlat);

		double sdlon = Math.sin( xlon - phi0);
		double cdlon = Math.cos( xlon - phi0);

		double x = ( st0 * ct1 ) - ( st1 * ct0 * cdlon );
		double y = st1 * sdlon;

		double az = Math.atan2( y, x ) / RADIANS_CONVERSION;

		return az;
	}

	/**
	 * OLD METHOD
	 * 
	 * Helper method that calculates the angle between two locations
	 * on the earth.<p>
	 *
	 * @param loc1			   location of first point
	 * @param loc2			   location of second point
	 * @return				  angle between the two locations
	 */
	public static double getAzimuth( Location loc1, Location loc2 ){
	  return getAzimuth( loc1.getLatitude(), loc1.getLongitude(),
						 loc2.getLatitude(), loc2.getLongitude() );
	}



	/**
	 * OLD METHOD
	 * 
	 * Helper method that calculates the angle between two locations
	 * on the earth.<p>
	 *
	 * Note: SWR: I'm not quite sure of the difference between azimuth and
	 * back azimuth. Ned, you will have to fill in the details.
	 *
	 * @param lat1			   latitude of first point
	 * @param lon1			   longitude of first point
	 * @param lat2			   latitude of second point
	 * @param lon2			   longitude of second point
	 * @return				  angle between the two lat/lon locations
	 */
	private static double getBackAzimuth(
			double lat1, double lon1, double lat2, double lon2 ){

		double xlat = lat1 * RADIANS_CONVERSION;
		double xlon = lon1 * RADIANS_CONVERSION;

		double st0 = Math.cos( xlat );
		double ct0 = Math.sin( xlat );

		double phi0 = xlon;

		xlat = lat2 * RADIANS_CONVERSION;
		xlon = lon2 * RADIANS_CONVERSION;

		double st1 = Math.cos(xlat);
		double ct1 = Math.sin(xlat);

		double sdlon = Math.sin( xlon - phi0);
		double cdlon = Math.cos( xlon - phi0);

		double x = ( st1 * ct0 ) - ( st0 * ct1 * cdlon );
		double y = -sdlon * st0;

		double baz = Math.atan2( y, x ) / RADIANS_CONVERSION;

		return baz;
	}

	/**
	 * Computes the shortest distance between a point and a line (great-circle).
	 * Both the line and point are assumed to be at the earth's surface; the 
	 * depth component of each <code>Location</code> is ignored. This
	 * is the true spherical geometric function for 'off-track distance'; 
	 * See <a href="http://williams.best.vwh.net/avform.htm#XTE">
	 * Aviation Formulary</a> for source.<br/>
	 * <br/>
	 * <b>Note:</b> This method, though more accurate over longer distances  
	 * and line lengths, is up to 20x slower than
	 * {@link RelativeLocation#getApproxHorzDistToLine(
	 * Location, Location, Location)}. However, this method does return
	 * accurate results for values spanning #177;180&#176;. Moreover, the
	 * sign of the result indicates which side of the supplied line
	 * <code>p3</code> is on (right:[+] left:[-]).
	 * 
	 * @param p1 the first <code>Location</code> point on the line
	 * @param p2 the second <code>Location</code> point on the line
	 * @param p3 the <code>Location</code> point for which distance will 
	 * 		be calculated
	 * @return the shortest distance in km between the supplied point and line
	 * @see #getApproxHorzDistToLine(Location, Location, Location)
	 */
	public static double distanceToLine(Location p1, Location p2, Location p3) {
		// angular distance
		double ad13 = angle(p1, p3);
		// delta azimuth p1 to p3 and azimuth p1 to p2
		double Daz13az12 = azimuthRad(p1, p3) - azimuthRad(p1, p2);		
		
		// cross-track distance (in radians)
		double xtdRad = Math.asin( Math.sin(ad13) * Math.sin(Daz13az12));
		// along-track distance (in km)
		double atd = Math.acos( Math.cos(ad13) / Math.cos(xtdRad)) * 
				EARTH_RADIUS_MEAN;
		
		// check if beyond p3
		if (atd > surfaceDistance(p1, p2)) return surfaceDistance(p2, p3);
		// check if before p1
		if (Math.cos(Daz13az12) < 0) return surfaceDistance(p1, p3);
		return xtdRad * EARTH_RADIUS_MEAN;
	}
	
	/**
	 * Computes the shortest distance between a point and a line. Both the 
	 * line and point are assumed to be at the earth's surface; the depth
	 * component of each <code>Location</code> is ignored. This is a fast,
	 * geometric, cartesion (flat-earth approximation) solution in which
	 * longitude is scaled by the cosine of latitude; it is only appropriate 
	 * for use over short distances (e.g. &lt;200 km).<br/>
	 * <br/>
	 * <b>Note:</b> This method does <i>NOT</i> support values spanning 
	 * #177;180&#176; and results for such input values are not guaranteed.
	 * 
	 * @param p1 the first <code>Location</code> point on the line
	 * @param p2 the second <code>Location</code> point on the line
	 * @param p3 the <code>Location</code> point for which distance will 
	 * 		be calculated
	 * @return the shortest distance in km between the supplied point and line
	 * @see RelativeLocation#distanceToLine(Location, Location, Location)
	 */
	// TODO rename to distanceToLineFast
	public static double getApproxHorzDistToLine(
			Location p1,
			Location p2,
			Location p3) {

		double lat1 = p1.getLatRad();
		double lat2 = p2.getLatRad();
		double lat3 = p3.getLatRad();
		double lon1 = p1.getLonRad();
		double lon2 = p2.getLonRad();
		double lon3 = p3.getLonRad();

		// use average latitude to scale longitude
		double lonScale = Math.cos(0.5 * lat3 + 0.25 * lat1 + 0.25 * lat2);

		// line-point corrdinates w/ loc transformed to the origin
		double x1 = (lon1 - lon3) * lonScale;
		double x2 = (lon2 - lon3) * lonScale;
		double y1 = lat1 - lat3;
		double y2 = lat2 - lat3;

		double dist;

		// check for values very close to zero
		if (Math.abs(x1 - x2) > 1e-6) {
			double m = (y2 - y1) / (x2 - x1); // slope
			double b = y2 - m * x2; 		  // intercept
			double xT = -m * b / (1 + m * m); // x target
			double yT = m * xT + b; 		  // y target

			// make sure the target point is in between the two endpoints
			boolean betweenPts = false;
			if (x2 > x1) {
				if (xT <= x2 && xT >= x1) betweenPts = true;
			} else {
				if (xT <= x1 && xT >= x2) betweenPts = true;
			}

			if (betweenPts)
				dist = Math.sqrt(xT * xT + yT * yT);
			// return Math.sqrt(xT*xT + yT*yT) * EARTH_RADIUS_MEAN;
			else {
				double d1 = Math.sqrt(x1 * x1 + y1 * y1);
				double d2 = Math.sqrt(x2 * x2 + y2 * y2);
				dist = Math.min(d1, d2);
			}
		} else {
			// the x1 = x2 case
			if (y2 > y1) {
				if (y2 <= 0.0) {
					dist = Math.sqrt(x2 * x2 + y2 * y2);
				} else if (y1 >= 0) {
					dist = Math.sqrt(x1 * x1 + y1 * y1);
				} else {
					dist = Math.abs(x1);
				}
			} else {
				// (y1 > y2)
				if (y1 <= 0.0) {
					dist = Math.sqrt(x1 * x1 + y1 * y1);
				} else if (y2 >= 0) {
					dist = Math.sqrt(x2 * x2 + y2 * y2);
				} else {
					dist = Math.abs(x1);
				}
			}
		}
		return dist * EARTH_RADIUS_MEAN;
	}
	
	/**
	 * Returns the radius of the earth at the latitude of the supplied
	 * <code>Location</code> (see <a 
	 * href="http://en.wikipedia.org/wiki/Earth_radius#Authalic_radius" 
	 * target="_blank">Wikipedia</a> for source).
	 * 
	 * @param p the <code>Location</code> at which to compute the earth's radius
	 * @return the earth's radius at the supplied <code>Location</code>
	 */
	public static double radiusAtLocation(Location p) {
		double cosL = Math.cos(p.getLatRad());
		double sinL = Math.sin(p.getLatRad());
		double C1 = cosL * EARTH_RADIUS_EQUATORIAL;
		double C2 = C1 * EARTH_RADIUS_EQUATORIAL;
		double C3 = sinL * EARTH_RADIUS_POLAR;
		double C4 = C3 * EARTH_RADIUS_POLAR;
		return Math.sqrt((C2*C2 + C4*C4) / (C1*C1 + C3*C3));
	}
	
	/**
	 * TODO move to GeoTools
	 * 
	 * Returns the number of degrees of latitude per km at a given 
	 * <code>Location</code>. This can be used to convert between km-based 
	 * and degree-based grid spacing. The calculation takes into account
	 * the shape of the earth (oblate spheroid) and scales the conversion
	 * accordingly.
	 * 
	 * @param p the <code>Location</code> at which to conversion value
	 * @return the number of decimal degrees latitude per km at a given
	 * 		<code>Location</code>
	 * @see RelativeLocation#radiusAtLocation(Location)
	 */
	public static double degreesLatPerKm(Location p) {
		return TO_DEG / radiusAtLocation(p);
	}

	/**
	 * TODO move to GeoTools
	 * 
	 * Returns the number of degrees of longitude per km at a given 
	 * <code>Location</code>. This can be used to convert between km-based
	 * and degree-based grid spacing. The calculation scales the degrees
	 * longitude per km at the equator by the cosine of the supplied
	 * latitude.
	 * 
	 * @param p the <code>Location</code> at which to conversion value
	 * @return the number of decimal degrees longitude per km at a given
	 * 		<code>Location</code>
	 */
	public static double degreesLonPerKm(Location p) {
		return TO_DEG / (EARTH_RADIUS_EQUATORIAL * Math.cos(p.getLatRad()));
	}

	public static double getDeltaLatFromKm(double km) {

	  //1 degree of Latitude is equal to 111.14kms.
	  return km/111.14;
	}

	/**
	 * OLD METHOD
	 * 
	 * As the earth is sperical, and does not have a constant radius for each longitude,
	 * so we calculate the longitude spacing (in Kms) for ever latitude
	 * @param lat= value of long for every lat according to gridSpacing
	 * @return
	 */
	public static double getDeltaLonFromKm(double lat,double km){
	  double radius = R * Math.cos(Math.toRadians(lat));
	  double longDistVal = 2*Math.PI *radius /360;
	  return km/longDistVal;
	}

	/**
	 * Returns whether the supplied <code>Location</code> coincides with
	 * one of the poles. Any supplied <code>Location</code>s that are very 
	 * close (less than a mm) will return <code>true</code>.
	 * 
	 * @param loc <code>Location</code> to check
	 * @return <code>true</code> if <code>loc</code> coincides with one of the
	 *         earth's poles, <code>false</code> otherwise.
	 */
	public static boolean isPole(Location loc) {
		return Math.cos(loc.getLatRad()) < 0.000000000001;
	}

	public static void main(String[] args) {
		
//		Location L1 = new Location(0,0);
//		Location L2 = new Location(30,0);
//		Location L3 = new Location(45,0);
//		Location L4 = new Location(-60,0);
//		Location L5 = new Location(-90,0);
//		
//		Location L = L5;
//		
//		System.out.println("degLatPerKm " + degreesLatPerKm(L));
//		System.out.println("degLonPerKm " + degreesLonPerKm(L));
//		System.out.println("  gDeltaLat " + getDeltaLatFromKm(1));
//		System.out.println("  gDeltaLon " + getDeltaLonFromKm(L.getLatitude(), 1));
//		System.out.println("---");
//		System.out.println("degLatPerKm " + degreesLatPerKm(L) * D_COEFF);
//		System.out.println("degLonPerKm " + degreesLonPerKm(L) * D_COEFF);
//		System.out.println("  gDeltaLat " + getDeltaLatFromKm(1) * D_COEFF);
//		System.out.println("  gDeltaLon " + getDeltaLonFromKm(L.getLatitude(), 1) * D_COEFF);

	}
	
	

}
