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

package org.opensha.sha.faultSurface;

import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.data.Container2D;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.exceptions.LocationException;


/**
 * <p>Title: GriddedSurface</p>
 *
 * <p>Description: Creates a Arbitrary surface that takes in a list of locations.
 * </p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */

public class GriddedSurface extends Container2D
    implements GriddedSurfaceAPI {


    /** The average strike of this surface on the Earth.  */
    protected double aveStrike=Double.NaN;

    /** The average dip of this surface into the Earth.  */
    protected double aveDip=Double.NaN;


  /**
   *  Constructor for the GriddedSurface object
   *
   * @param  numRows  Number of grid points along width of fault
   * @param  numCols  Number of grid points along length of fault
   */
  public GriddedSurface(int numRows, int numCols) {
    super(numRows, numCols);
  }



  /**
   *  Add a Location to the grid - does the same thing as set except that it
   *  ensures the object is a Location object.
   *
   * @param  row                                 The row to set this Location at.
   * @param  column                              The column to set this Location at.
   * @param  location                            The new location value.
   * @exception  ArrayIndexOutOfBoundsException  Thrown if the row or column lies beyond the grid space indexes.
   */
  public void setLocation(int row, int column, Location location) {
    super.set(row, column, location);
  }

  /**
   * Returns the average dip of this surface into the Earth.
   *
   * @throws UnsupportedOperationException
   * @return double
   * @todo Implement this org.opensha.sha.surface.GriddedSurfaceAPI method
   */
  public double getAveDip() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "getAveDip() not supported by GriddedSurface class");
  }

  /**
   * Returns the average strike of this surface into the Earth.
   *
   * @throws UnsupportedOperationException
   * @return double
   * @todo Implement this org.opensha.sha.surface.GriddedSurfaceAPI method
   */
  public double getAveStrike() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "getAveStrike() not supported by GriddedSurface class");
  }



  /**
   * Returns the lenth of this surface.
   *
   * @throws UnsupportedOperationException
   * @return double
   * @todo Implement this org.opensha.sha.surface.GriddedSurfaceAPI method
   */
  public double getSurfaceLength() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "getSurfaceLength() not supported by GriddedSurface class");
  }

  /**
   * Returns the down-dip width of this surface.
   *
   * @throws UnsupportedOperationException
   * @return double
   * @todo Implement this org.opensha.sha.surface.GriddedSurfaceAPI method
   */
  public double getSurfaceWidth() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "getSurfaceWidth() not supported by GriddedSurface class");
  }



  /**
   *  Set an object in the 2D grid. Ensures the object passed in is a Location.
   *
   * @param  row                                 The row to set the Location.
   * @param  column                              The row to set the Location.
   * @param  obj                                 Must be a Location object
   */
  public void set(int row, int col, Object loc) {
    if (loc instanceof Location)
      setLocation(row, col, (Location)loc);
    else
      throw new ClassCastException(
          "Only Location object can be provided in this method");
  }



  /**
   *  Retrieves a Location in the 2D grid - does the same thing as get except
   *  that it casts the returned object to a Location.
   *
   * @param  row     The row to get this Location from.
   * @param  column  The column to get this Location from.
   * @return         The location stored at the row and column.
   * @exception  LocationException  Thown if the object being retrieved cannot be cast to a Location.
   */
  public Location getLocation(int row, int col) throws LocationException {
    String S = C + ": getLocation():";
    if (exist(row, col)) {
      return (Location) get(row, col);
    }
    else {
      throw new LocationException(S + "Requested object doesn't exist in " +
                                  row + ", " + col);
    }
  }


  /** Does same thing as listIterator() in super Interface */
  public ListIterator getLocationsIterator() {
    return super.listIterator();
  }

  /**
   * Put all the locations of this surface into a location list
   *
   * @return
   */
  public LocationList getLocationList() {
    LocationList locList = new LocationList();
    Iterator it = this.getLocationsIterator();
    while (it.hasNext()) locList.addLocation( (Location) it.next());
    return locList;
  }


  final static char TAB = '\t';
  /** Prints out each location and fault information for debugging */
  public String toString() {

    StringBuffer b = new StringBuffer();
    b.append(C + '\n');
    if (aveStrike != Double.NaN) b.append("Ave. Strike = " + aveStrike + '\n');
    if (aveDip != Double.NaN) b.append("Ave. Dip = " + aveDip + '\n');

    b.append("Row" + TAB + "Col" + TAB + "Latitude" + TAB + "Longitude" + TAB +
             "Depth");

    String superStr = super.toString();
    //int index = superStr.indexOf('\n');
    //if( index > 0 ) superStr = superStr.substring(index + 1);
    b.append('\n' + superStr);

    return b.toString();
  }


  /** get a list of locations that constitutes the perimeter (forst row, last col, last row, and first col) */
  public LocationList getSurfacePerimeterLocsList() {
	  LocationList locList = new LocationList();
	  for(int c=0;c<getNumCols();c++) locList.addLocation(getLocation(0, c));
	  for(int r=0;r<getNumRows();r++) locList.addLocation(getLocation(r, getNumCols()-1));
	  for(int c=getNumCols()-1;c>=0;c--) locList.addLocation(getLocation(getNumRows()-1, c));
	  for(int r=getNumRows()-1;r>=0;r--) locList.addLocation(getLocation(r, 0));
	  return locList;
  }



  /**
   * Returns the Surface Metadata with the following info:
   * <ul>
   * <li>AveDip
   * <li>Surface length
   * <li>Surface DownDipWidth
   * <li>GridSpacing
   * <li>NumRows
   * <li>NumCols
   * <li>Number of locations on surface
   * <p>Each of these elements are represented in Single line with tab("\t") delimitation.
   * <br>Then follows the location of each point on the surface with the comment String
   * defining how locations are represented.</p>
   * <li>#Surface locations (Lat Lon Depth)
   * <p>Then until surface locations are done each line is the point location on the surface.
   *
   * </ul>
   * @return String
   */
  public String getSurfaceMetadata() {
    String surfaceMetadata;
    surfaceMetadata = (float) aveDip + "\t";
    surfaceMetadata += (float) Double.NaN + "\t";
    int numRows = getNumRows();
    int numCols = getNumCols();
    surfaceMetadata += numRows + "\t";
    surfaceMetadata += numCols + "\t";
    surfaceMetadata += (numRows * numCols) + "\n";
    surfaceMetadata += "#Surface locations (Lat Lon Depth) \n";
    ListIterator it = getLocationsIterator();
    while (it.hasNext()) {
      Location loc = (Location) it.next();
      surfaceMetadata += (float) loc.getLatitude() + "\t";
      surfaceMetadata += (float) loc.getLongitude() + "\t";
      surfaceMetadata += (float) loc.getDepth() + "\n";
    }
    return surfaceMetadata;
  }
}

