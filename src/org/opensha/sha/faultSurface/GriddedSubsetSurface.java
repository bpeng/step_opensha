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
import org.opensha.commons.data.Container2DAPI;
import org.opensha.commons.data.ContainerSubset2D;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.Window2D;
import org.opensha.commons.exceptions.LocationException;

/**
 * <b>Title:</b> GriddedSubsetSurface<p>
 *
 * <b>Description:</b> Implements the same functionality as a GriddedSurface,
 * but only maintains a small read only window view into a GriddedSurface. The
 * Gridded Surface actually stores the data points.<p>
 *
 * <b>Note:</b> This class is purely a convinience class that translates indexes so the
 * user can deal with a smaller window than the full GriddedSurface. Think of
 * this as a "ZOOM" function into a GriddedSurface.<p>
 *
 * <b>Note:</b> SetLocation, setAveStrike, setAveDip have been disabled, this
 * class is read-only into the dataset. <p>
 *
 * @see Window2D
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */
public class GriddedSubsetSurface extends ContainerSubset2D implements EvenlyGriddedSurfaceAPI {

    /**
     *  Constructor for the GriddedSubsetSurface object
     *
     * @param  numRows                             Specifies the length of the window.
     * @param  numCols                             Specifies the height of the window
     * @param  startRow                            Start row into the main GriddedSurface.
     * @param  startCol                            Start column into the main GriddedSurface.
     * @exception  ArrayIndexOutOfBoundsException  Thrown if window indexes exceed the
     * main GriddedSurface indexes.
     */
    public GriddedSubsetSurface( int numRows, int numCols, int startRow, int startCol )
             throws ArrayIndexOutOfBoundsException {
        super( numRows, numCols, startRow, startCol );
    }


    /**
     *  Constructor for the GriddedSubsetSurface object
     *
     * @param  numRows                             Specifies the length of the window.
     * @param  numCols                             Specifies the height of the window
     * @param  startRow                            Start row into the main GriddedSurface.
     * @param  startCol                            Start column into the main GriddedSurface.
     * @param  data                                The main GriddedSurface this is a window into
     * @exception  ArrayIndexOutOfBoundsException  Thrown if window indexes exceed the
     * main GriddedSurface indexes.
     */
    public GriddedSubsetSurface( int numRows, int numCols, int startRow, int startCol, EvenlyGriddedSurfaceAPI data )
             throws ArrayIndexOutOfBoundsException {
        super( numRows, numCols, startRow, startCol, ( Container2DAPI ) data );
    }


    /** Add a Location to the grid. This method throws UnsupportedOperationException as it is disabled. */
    public void setLocation( int row, int col,
            org.opensha.commons.data.Location location ) {
        throw new java.lang.UnsupportedOperationException( "This function is not implemented in this subclass" );
    }


    /**
     * Resizes the window view into the main GriddedSurface data. <p>
     *
     * Note: This function uses some advanced features of a transactional nature.
     * A transaction is basically a series of steps that must follow each other.
     * If any of the steps fails, the previous steps must be rolled back. I
     * perform this by calling initTransaction(), rollback() if error, else commit().
     * It sounds more complicated than it it. This approach basically resets
     * the window size to the starting size if any of the new indexes fail. Each
     * is checked one at a time.
     *
     * @param  startRow                            The Start row into the main GriddedSurface.
     * @param  startCol                            Start column into the main GriddedSurface.
     * @param  numRows                             The new length of the window.
     * @param  numCols                             The new height of the window
     * @exception  ArrayIndexOutOfBoundsException  Thrown if window indexes exceed the
     * main GriddedSurface indexes.
     */
    public void setLimits(
            int startRow,
            int startCol,
            int numRows,
            int numCols )
             throws ArrayIndexOutOfBoundsException {

        String S = C + ": setLimits():";
        initTransaction();


        window.setStartRow(startRow);
        window.setStartCol(startCol);
        window.setNumRows(numRows);
        window.setNumCols(numCols);

        try {
            window.checkLowerBounds( S );
            window.calcUpperBounds();
            if ( data != null ) {
                window.checkUpperBounds( S );
            }
        } catch ( ArrayIndexOutOfBoundsException e ) {
            rollback();
            throw e;
        }
        commit();
    }


     /**
     * Replaces the real data of the GriddedSurface with
     * a new surface. <p>
     *
     * Note: This could be a dangerous thing to do if the
     * indexes are invalid for the new surface. I am not
     * sure if this is being checked. Please consult the
     * Container2D documentation for further information.
     *
     * @param  gs  The new newMainSurface value
     */
    public void setNewMainSurface( EvenlyGriddedSurface gs ) {
        super.setContainer2D( ( Container2D ) gs );
    }


    /**
     *  Get a Location to the grid, unless it doesn't exist. Note
     *  these points are translated to the real grid. FOr example
     *  0 row and column returns from the GriddedSurface the Location
     *  at (startRow, startColumn). <p>
     *
     * Recall that the grid point may be a valid point but there
     * is no Location object stored at that grid point.
     *
     * @param  row                    The row index from which to obtain the Location, in subset coordinates.
     * @param  col                    The column index from which to obtain the Location, in subset coordinates.
     * @return                        The location value if found.
     * @exception  LocationException  Thrown if a Location doesn't exist
     * at the specified grid point.
     */
    public Location getLocation( int row, int col )
             throws LocationException {
        String S = C + ": getLocation():";
        if ( exist( row, col ) ) {
            return ( Location ) get( row, col );
        } else {
            throw new LocationException( S + "Requested object doesn't exist in " + row + ", " + col );
        }
    }


    /**
     *  Gets the locationsIterator attribute of the GriddedSubsetSurface object.
     *  Will return a subset iterator, not iterating over the whole GriddedSurface,
     *  but just over the subset window points.
     *
     * @return    A listIterator of Location obejcts.
     */
    public ListIterator getLocationsIterator() {
        return this.listIterator();
    }

    /**
     * Put all the locations of this surface into a location list
     *
     * @return
     */
    public LocationList getLocationList() {
      LocationList locList = new LocationList();
      Iterator it = this.getLocationsIterator();
      while(it.hasNext()) locList.addLocation((Location)it.next());
      return locList;

    }

    /** Proxy method that returns the number of rows in the main GriddedSurface. */
    public int getMainNumRows() {
        return data.getNumRows();
    }


    /** Proxy method that returns the number of colums in the main GriddedSurface. */
    public int getMainNumCols() {
        return data.getNumCols();
    }


    /**
     *  Proxy method that returns the aveStrike of the main GriddedSurface. <P>
     *
     *  SWR: Note - should we be returning the main GriddedSurface ave strike,
     *  or the ave. strike for the subsurface, which may be different from the
     *  main surface.
     *
     * @return    The aveStrike value
     */
    public double getAveStrike() {
        return ( ( EvenlyGriddedSurfaceAPI) data ).getAveStrike();
    }


    /**
     *  Proxy method that returns the aveDip of the main GriddedSurface. <P>
     *
     *  SWR: Note - should we be returning the main GriddedSurface ave. dip, or
     *  the ave. dip for the subsurface, which may be different from the main
     *  surface.  This is especially important now that we have a
     *  SimpleListricGriddedFaultFactory (Ned's comment).
     *
     * @return    The aveDip value
     */
    public double getAveDip() {
        return ( ( EvenlyGriddedSurface ) data ).getAveDip();
    }

    /** Debug string to represent a tab. Used by toString().  */
    final static char TAB = '\t';

    /** Prints out each location and fault information for debugging */
    public String toString(){

        StringBuffer b = new StringBuffer();
        b.append( C + '\n');
        if ( data != null ) b.append( "Ave. Strike = " + ( ( EvenlyGriddedSurface ) data ).getAveStrike() + '\n' );
        if ( data != null ) b.append( "Ave. Dip = " + ( ( EvenlyGriddedSurface ) data ).getAveDip() + '\n' );

        b.append( "Row" + TAB + "Col" + TAB + "Latitude" + TAB + "Longitude" + TAB + "Depth");

        String superStr = super.toString();
        //int index = superStr.indexOf('\n');
        //if( index > 0 ) superStr = superStr.substring(index + 1);
        b.append( '\n' + superStr );

        return b.toString();
    }


    /**
     * This returns the total length of the surface
     * @return double
     */
    public double getSurfaceLength() {

        return getGridSpacing() * (getNumCols()-1);
    }

    /**
     * This returns the surface width (down dip)
     * @return double
     */
    public double getSurfaceWidth() {
      return getGridSpacing() * (getNumRows()-1);
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

       return ((EvenlyGriddedSurfaceAPI)data).getSurfaceMetadata();
    }


    /**
     * returns the grid spacing
     *
     * @return
     */
    public double getGridSpacing() {
      return ((EvenlyGriddedSurfaceAPI)data).getGridSpacing();
    }
    
    /**
     * This returns the total length of the surface in km
     * @return double
     */
    
    /**
     * This returns the surface area in km-sq
     * @return double
     */
    public double getSurfaceArea() {
      return getSurfaceWidth()*getSurfaceLength();
    }


}
