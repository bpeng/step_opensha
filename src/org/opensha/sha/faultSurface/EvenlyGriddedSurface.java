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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Container2D;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.exceptions.LocationException;

/**
 * <b>Title:</b> GriddedSurface<p>
 *
 * <b>Description:</b> Base implementation of the EvenlyGriddedSurfaceAPI.
 *
 * @author
 * @created
 * @version    1.0
 */
public abstract class EvenlyGriddedSurface
         extends Container2D
         implements EvenlyGriddedSurfaceAPI {


    /** Class name for debugging. */
    protected final static String C = "EvenlyGriddedSurface";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    /** The average strike of this surface on the Earth.  */
    protected double aveStrike=Double.NaN;

    /** The average dip of this surface into the Earth.  */
    protected double aveDip=Double.NaN;

    /**
     * @todo Variables
     */
    protected double gridSpacing;

    /**
     * No Argument constructor, called from classes extending it.
     *
     */
    protected EvenlyGriddedSurface(){}

    /**
     *  Constructor for the GriddedSurface object
     *
     * @param  numRows  Number of grid points along width of fault
     * @param  numCols  Number of grid points along length of fault
     */
    public EvenlyGriddedSurface( int numRows, int numCols,double gridSpacing ) {
        super( numRows, numCols );
        this.gridSpacing = gridSpacing;
    }

    /**
     *  Set an object in the 2D grid. Ensures the object passed in is a Location.
     *
     * @param  row                                 The row to set the Location.
     * @param  column                              The row to set the Location.
     * @param  obj                                 Must be a Location object
     * @exception  throws UnsupportedOperationException      Throws this exception if
     * once created user tries to change any location on the surface.
     */
    public void set( int row, int column, Object obj ) throws UnsupportedOperationException{

      throw new UnsupportedOperationException("EvenlyGriddedSurface does not allow the user to set locations.");
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
    protected void setLocation( int row, int column, Location location ) {
        super.set( row, column, location );
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
    public Location getLocation( int row, int col )
             throws LocationException {
        String S = C + ": getLocation():";
        if ( exist( row, col ) ) {
            return ( Location ) get( row, col );
        } else {
            throw new LocationException( S + "Requested object doesn't exist in " + row + ", " + col );
        }
    }


    /** Returns the average strike of this surface on the Earth.  */
    public double getAveStrike() { return aveStrike; }

    /** Returns the average dip of this surface into the Earth.  */
    public double getAveDip() { return aveDip; }


    /** Does same thing as listIterator() in super Interface */
    public ListIterator getLocationsIterator() { return super.listIterator(); }

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



    final static char TAB = '\t';
    /** Prints out each location and fault information for debugging */
    public String toString(){

        StringBuffer b = new StringBuffer();
        b.append( C + '\n');
        if ( aveStrike != Double.NaN ) b.append( "Ave. Strike = " + aveStrike + '\n' );
        if ( aveDip != Double.NaN ) b.append( "Ave. Dip = " + aveDip + '\n' );

        b.append( "Row" + TAB + "Col" + TAB + "Latitude" + TAB + "Longitude" + TAB + "Depth");

        String superStr = super.toString();
        //int index = superStr.indexOf('\n');
        //if( index > 0 ) superStr = superStr.substring(index + 1);
        b.append( '\n' + superStr );

        return b.toString();
    }



    /**
     * returns the grid spacing
     *
     * @return
     */
    public double getGridSpacing() {
      return this.gridSpacing;
    }



    /**
     * Gets the Nth subSurface on the surface
     *
     * @param numSubSurfaceCols  Number of grid points along length
     * @param numSubSurfaceRows  Number of grid points along width
     * @param numSubSurfaceOffset Number of grid poits for offset
     * @param n The index of the desired surface (from 0 to (getNumSubsetSurfaces - 1))
     *
     */
    public GriddedSubsetSurface getNthSubsetSurface(int numSubSurfaceCols,
                                                    int numSubSurfaceRows,
                                                    int numSubSurfaceOffset,
                                                    int n) {
      // number of subSurfaces along the length of fault
      int nSubSurfaceAlong = (int)Math.floor((numCols-numSubSurfaceCols)/numSubSurfaceOffset +1);

      // there is only one subSurface
      if(nSubSurfaceAlong <=1) {
        nSubSurfaceAlong=1;
      }
      if(numSubSurfaceCols > numCols) numSubSurfaceCols = numCols;

      // number of subSurfaces down fault width
      int nSubSurfaceDown =  (int)Math.floor((numRows-numSubSurfaceRows)/numSubSurfaceOffset +1);

      // one subSurface along width
      if(nSubSurfaceDown <=1) {
        nSubSurfaceDown=1;
      }
      if(numSubSurfaceRows > numRows) numSubSurfaceRows = numRows;

      return getNthSubsetSurface(numSubSurfaceCols, numSubSurfaceRows, numSubSurfaceOffset, nSubSurfaceAlong, n);
   //     throw new RuntimeException("EvenlyGriddeddsurface:getNthSubsetSurface::Inavlid n value for subSurface");
    }


    /**
     * Gets the Nth subSurface on the surface
     *
     * @param numSubSurfaceCols  Number of grid points along length
     * @param numSubSurfaceRows  Number of grid points along width
     * @param numSubSurfaceOffset Number of grid poits for offset
     * @param n The index of the desired surface (from 0 to (getNumSubsetSurfaces - 1))
     *
     */
    private GriddedSubsetSurface getNthSubsetSurface(int numSubSurfaceCols,int numSubSurfaceRows,
                                                     int numSubSurfaceOffset,int nSubSurfaceAlong,
                                                     int n){
      //getting the row number in which that subsetSurface is present
      int row = n/nSubSurfaceAlong * numSubSurfaceOffset;

      //getting the column from which that subsetSurface starts
      int col = n%nSubSurfaceAlong * numSubSurfaceOffset;

      return (new GriddedSubsetSurface((int)numSubSurfaceRows,(int)numSubSurfaceCols,row,col,this));
    }


    /**
     * Gets the Nth subSurface on the surface
     *
     * @param subSurfaceLength  subsurface length in km
     * @param subSurfaceWidth  subsurface width in km
     * @param subSurfaceOffset offset in km
     * @param n The index of the desired surface (from 0 to (getNumSubsetSurfaces - 1))
     *
     */
    public GriddedSubsetSurface getNthSubsetSurface(double subSurfaceLength,
                                                   double subSurfaceWidth,
                                                   double subSurfaceOffset,
                                                   int n) {
       return getNthSubsetSurface((int)Math.rint(subSurfaceLength/gridSpacing+1),
                                  (int)Math.rint(subSurfaceWidth/gridSpacing+1),
                                  (int)Math.rint(subSurfaceOffset/gridSpacing), 
                                  n);
    }
    
    
    /**
     * Gets the Nth subSurface centered down dip on the surface. If surface is not perfectly centered,
     * (numRows-numRowsInRup != even number), rupture is one grid increment closer to top then to bottom.
     *
     * @param subSurfaceLength  subsurface length in km
     * @param subSurfaceWidth  subsurface width in km
     * @param subSurfaceOffset offset in km
     * @param n The index of the desired surface (from 0 to (getNumSubsetSurfaces - 1))
     *
     */
    public GriddedSubsetSurface getNthSubsetSurfaceCenteredDownDip(double subSurfaceLength,
                                                   double subSurfaceWidth,
                                                   double subSurfaceOffset,
                                                   int n) {
    	
        int numSubSurfaceCols =  (int)Math.rint(subSurfaceLength/gridSpacing+1);
        int startCol = -1;
        
        // make sure it doesn't extend beyond the end
        if(numSubSurfaceCols>numCols){
        	numSubSurfaceCols=numCols;
        	startCol=0;
        }
        else {
            startCol = n * (int)Math.rint(subSurfaceOffset/gridSpacing);
        }

    	int numSubSurfaceRows = (int)Math.rint(subSurfaceWidth/gridSpacing+1);
    	int startRow=-1;
    	
    	// make sure it doesn't extend beyone the end
    	if(numSubSurfaceRows >= numRows){
    		numSubSurfaceRows=numRows;
    		startRow=0;
    	}
    	else {
        	startRow = (int)Math.floor((numRows-numSubSurfaceRows)/2);  		
    	}
    	System.out.println("subSurfaceLength="+subSurfaceLength+", subSurfaceWidth="+subSurfaceWidth+", subSurfaceOffset="+
    			subSurfaceOffset+", numRows="+numRows+", numCols="+numCols+", numSubSurfaceRows="+
    			numSubSurfaceRows+", numSubSurfaceCols="+numSubSurfaceCols+", startRow="+startRow+", startCol="+startCol);
       return (new GriddedSubsetSurface(numSubSurfaceRows,numSubSurfaceCols,startRow,startCol,this));
    }
   
    
    
    

    /**
     * Get the subSurfaces on this fault
     *
     * @param numSubSurfaceCols  Number of grid points according to length
     * @param numSubSurfaceRows  Number of grid points according to width
     * @param numSubSurfaceOffset Number of grid poits for offset
     *
     */
    public Iterator getSubsetSurfacesIterator(int numSubSurfaceCols, int numSubSurfaceRows, int numSubSurfaceOffset) {

        //vector to store the GriddedSurface
        ArrayList<GriddedSubsetSurface> v = new ArrayList<GriddedSubsetSurface>();

        // number of subSurfaces along the length of fault
        int nSubSurfaceAlong = (int)Math.floor((numCols-numSubSurfaceCols)/numSubSurfaceOffset +1);

        // there is only one subSurface
        if(nSubSurfaceAlong <=1) {
          nSubSurfaceAlong=1;
          numSubSurfaceCols = numCols;
        }

        // number of subSurfaces along fault width
        int nSubSurfaceDown =  (int)Math.floor((numRows-numSubSurfaceRows)/numSubSurfaceOffset +1);

        // one subSurface along width
        if(nSubSurfaceDown <=1) {
          nSubSurfaceDown=1;
          numSubSurfaceRows = numRows;
        }

        //getting the total number of subsetSurfaces
        int totalSubSetSurface = nSubSurfaceAlong * nSubSurfaceDown;
        //emptying the vector
        v.clear();

        //adding each subset surface to the ArrayList
        for(int i=0;i<totalSubSetSurface;++i)
          v.add(getNthSubsetSurface(numSubSurfaceCols,numSubSurfaceRows,numSubSurfaceOffset,nSubSurfaceAlong,i));

       return v.iterator();
   }



   /**
    * Get the subSurfaces on this fault
    *
    * @param subSurfaceLength  Sub Surface length in km
    * @param subSurfaceWidth   Sub Surface width in km
    * @param subSurfaceOffset  Sub Surface offset
    * @return           Iterator over all subSurfaces
    */
    public Iterator getSubsetSurfacesIterator(double subSurfaceLength,
                                              double subSurfaceWidth,
                                              double subSurfaceOffset) {

       return getSubsetSurfacesIterator((int)Math.rint(subSurfaceLength/gridSpacing+1),
                                        (int)Math.rint(subSurfaceWidth/gridSpacing+1),
                                        (int)Math.rint(subSurfaceOffset/gridSpacing));

    }

    /**
     *
     * @param subSurfaceLength subSurface length in km
     * @param subSurfaceWidth  subSurface Width in km
     * @param subSurfaceOffset subSurface offset
     * @return total number of subSurface along the fault
     */
    public int getNumSubsetSurfaces(double subSurfaceLength,double subSurfaceWidth,double subSurfaceOffset){
      int lengthCols =  (int)Math.rint(subSurfaceLength/gridSpacing+1);
      int widthCols =    (int)Math.rint(subSurfaceWidth/gridSpacing+1);
      int offsetCols =   (int)Math.rint(subSurfaceOffset/gridSpacing);
      int totalSubSurfaces =1;
      // number of subSurfaces along the length of fault
       int nSubSurfaceAlong = (int)Math.floor((numCols-lengthCols)/offsetCols +1);

       // there is only one subSurface
       if(nSubSurfaceAlong <=1) {
         nSubSurfaceAlong=1;
       }

       // nnmber of subSurfaces along fault width
       int nSubSurfaceDown =  (int)Math.floor((numRows-widthCols)/offsetCols +1);

       // one subSurface along width
       if(nSubSurfaceDown <=1) {
         nSubSurfaceDown=1;
       }
     totalSubSurfaces =   nSubSurfaceAlong * nSubSurfaceDown;
     return totalSubSurfaces;
    }


    
    /**
    * This computes the number of subset surfaces along the length only (not down dip)
    * @param subSurfaceLength subSurface length in km
    * @param subSurfaceOffset subSurface offset
    * @return total number of subSurface along the fault
    */
   public int getNumSubsetSurfacesAlongLength(double subSurfaceLength,double subSurfaceOffset){
     int lengthCols =  (int)Math.rint(subSurfaceLength/gridSpacing+1);
     int offsetCols =   (int)Math.rint(subSurfaceOffset/gridSpacing);

     // number of subSurfaces along the length of fault
      int nSubSurfaceAlong = (int)Math.floor((numCols-lengthCols)/offsetCols +1);

      // there is only one subSurface
      if(nSubSurfaceAlong <=1) {
        nSubSurfaceAlong=1;
      }

    return nSubSurfaceAlong;
   }

    

    /**
     * This returns the total length of the surface in km
     * @return double
     */
    public double getSurfaceLength() {

        return getGridSpacing() * (getNumCols()-1);
    }

    /**
     * This returns the surface width (down dip) in km
     * @return double
     */
    public double getSurfaceWidth() {
      return getGridSpacing() * (getNumRows()-1);
    }
    
    /**
     * This returns the surface area in km-sq
     * @return double
     */
    public double getSurfaceArea() {
      return getSurfaceWidth()*getSurfaceLength();
    }
    
   
    
    /**
     * Calculate the minimum distance of this surface from user provided surface
     * @param surface EvenlyGriddedSurface 
     * @return distance in km
     */
    public double getMinDistance(EvenlyGriddedSurfaceAPI surface) {
    	Iterator it = getLocationsIterator();
    	double min3dDist = Double.POSITIVE_INFINITY;
    	double dist;
    	// find distance between all location pairs in the two surfaces
    	while(it.hasNext()) { // iterate over all locations in this surface
    		Location loc1 = (Location)it.next();
    		Iterator it2 = surface.getLocationsIterator();
    		while(it2.hasNext()) { // iterate over all locations on the user provided surface
    			Location loc2 = (Location)it2.next();
    			dist = RelativeLocation.getApproxHorzDistance(loc1, loc2);
    			if(dist<min3dDist) min3dDist = dist;
    		}
    	}
    	return min3dDist;
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
      surfaceMetadata = (float)aveDip + "\t";
      surfaceMetadata += (float)getSurfaceLength() + "\t";
      surfaceMetadata += (float)getSurfaceWidth() + "\t";
      surfaceMetadata += (float)Double.NaN + "\t";
      int numRows = getNumRows();
      int numCols = getNumCols();
      surfaceMetadata += numRows + "\t";
      surfaceMetadata += numCols + "\t";
      surfaceMetadata += (numRows * numCols) + "\n";
      surfaceMetadata += "#Surface locations (Lat Lon Depth) \n";
      ListIterator it = getLocationsIterator();
      while (it.hasNext()) {
        Location loc = (Location) it.next();
        surfaceMetadata += (float)loc.getLatitude()+"\t";
        surfaceMetadata += (float)loc.getLongitude()+"\t";
        surfaceMetadata += (float)loc.getDepth()+"\n";
      }
      return surfaceMetadata;
    }
}
