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

/**
 * This class encapsulates information describing a vector between two
 * <code>Location</code>s. This vector is defined by the bearing from
 * a point p1 to a point p2, and also by the horizontal and vertical 
 * separation between the points.<br/>
 * <br/>
 * <b>Note:</b> Although a <code>LocationVector</code> will function in any
 * reference frame, the convention in seismology is for depth to be positive
 * down.
 *
 * @author Peter Powers
 * @author Sid Hellman
 * @author Steven W. Rock
 * @version $Id: Direction.java 6403 2010-03-08 04:19:31Z bpeng $
 */
// TODO refactor to LocationVector
public class Direction {

	/*
	 * Developer Notes: The previous incarnation of this class as 'Direction'
	 * included back azimuth. By committing to provide a back azimuth, each
	 * Direction was implicitely location dependent. Furthermore, the onus
	 * was on the user to provide the correct value for back azimuth. There
	 * were also instances where the back azimuth was incorrectly assumed to 
	 * be the 180deg complement of azimuth. This property of the class has been
	 * removed and users are directed in LocationUtils.azimuth() to simply 
	 * reverse the points of interest if a back azimuth value is required.
	 */
	
    private final static String C = "Direction";
    private final static boolean D = false;

    private double vDist;
    private double hDist;
    private double azimuth;
    private double backAzimuth;

    /**
     *  No-Arg Constructor for the Direction object sets default values:
     *
     * <ul><li>azimuth.setValue( new Double(180) );
     * <li>backAzimuth.setValue( new Double(0) );
     * <li>horzDistance = 0;
     * <li>vertDistance = 0;
     * </ul>

     */
    public Direction() {
    	// TODO fix THIS IS WRONG and NOT a good assumption to make
    	// back azimuth is dependent on location
//        this.azimuth.setValue( new Double(180) );
//        this.backAzimuth.setValue( new Double(0) );
//        this.horzDistance = 0;
//        this.vertDistance = 0;

    }


    /**
     *  Constructor that allows setting all the fields of this Direction object
     *
     * @param  vDist   vertical distance
     * @param  hDist   horizontal distance
     * @param  az      azimuth
     * @param  backAz  back azimuth
     */
    public Direction( double vDist, double hDist, double az, double backAz ) {

//        this.azimuth.setValue( new Double(az) );
//        this.backAzimuth.setValue( new Double(backAz) );
        this.hDist = hDist;
        this.vDist = vDist;
        this.azimuth = az;
        this.backAzimuth = backAz;
    }


    /**
     *  Sets the vertDistance attribute of the Direction object
     *
     * @param  vertDistance  The new vertDistance value
     */
    public void setVertDistance( double vertDistance ) {
        this.vDist = vertDistance;
    }


    /**
     *  Sets the horzDistance attribute of the Direction object
     *
     * @param  horzDistance  The new horzDistance value
     */
    public void setHorzDistance( double horzDistance ) {
        this.hDist = horzDistance;
    }


    /**
     *  Sets the azimuth attribute of the Direction object
     *
     * @param  azimuth        The new azimuth value
     * @exception  Exception  Description of the Exception
     */
    public void setAzimuth( double azimuth ) {
    	this.azimuth = azimuth;
//        this.azimuth.setValue( new Double( azimuth ) );
    }


    /**
     *  Sets the backAzimuth attribute of the Direction object
     *
     * @param  backAzimuth    The new backAzimuth value
     * @exception  Exception  Description of the Exception
     */
    public void setBackAzimuth( double backAzimuth ) {
    	this.backAzimuth = backAzimuth;
//        this.backAzimuth.setValue( new Double( backAzimuth ) );
    }


    /**
     *  Gets the vertDistance attribute of the Direction object
     *
     * @return    The vertDistance value
     */
    public double getVertDistance() {
        return vDist;
    }


    /**
     *  Gets the horzDistance attribute of the Direction object
     *
     * @return    The horzDistance value
     */
    public double getHorzDistance() {
        return hDist;
    }


    /**
     *  Gets the azimuth attribute of the Direction object
     *
     * @return    The azimuth value
     */
    public double getAzimuth() {
    	return azimuth;
        //return ( ( Double ) azimuth.getValue() ).doubleValue();
    }


    /**
     *  Gets the backAzimuth attribute of the Direction object
     *
     * @return    The backAzimuth value
     */
    public double getBackAzimuth() {
    	return backAzimuth;
        //return ( ( Double ) backAzimuth.getValue() ).doubleValue();
    }



    /** Debug printout of all the field values */
    public String toString(){

        StringBuffer b = new StringBuffer();
        b.append(C);
        //b.append('\n');
        b.append(" : ");

        b.append("horzDistance = ");
        b.append(hDist);
        //b.append('\n');
        b.append(" : ");


        b.append("vertDistance = ");
        b.append(vDist);
        //b.append('\n');
        b.append(" : ");


        b.append("azimuth = ");
        b.append(azimuth);
        //b.append('\n');
        b.append(" : ");

        b.append("backAzimuth = ");
        b.append(backAzimuth);
        //b.append('\n');
        b.append(" : ");

        return b.toString();

    }

    /**
     * Checks to see if another Direction object has the same field values.
     * If it does, they are considered equal.
     * @param dir
     * @return
     */
    public boolean equalsDirection(Direction dir){

        if(hDist != dir.hDist ) return false;
        if(vDist != dir.vDist ) return false;
        if(azimuth != dir.azimuth ) return false;
        if(this.backAzimuth != dir.backAzimuth ) return false;

        return true;
    }

    /**
     * Calls equalsDirection(Direction dir) if passed in object is a Direction,
     * else returns false. A different class could never be considered equals, like
     * comparing apples to oranges.
     * @param obj
     * @return
     */
    public boolean equals(Object obj){
        if(obj instanceof Direction) return equalsDirection( (Direction)obj );
        else return false;
    }
    
}
