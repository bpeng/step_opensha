/**
 * 
 */
package org.opensha.step.calc;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.BorderType;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.commons.data.region.Region;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;


/**
 * @author matthew
 *
 */
public class CountObsInGrid {
	
	private int numGridNodes;
	private GriddedRegion region;
	private int[] numObsInGrid;
	private ObsEqkRupList obsEvents;
//	private EvenlyGriddedCircularGeographicRegion castCircularRegion;
//	private EvenlyGriddedSausageGeographicRegion castSausageRegion;
//	private boolean useCircle = false, useSausage = false;

	public CountObsInGrid(ObsEqkRupList obsEvents, GriddedRegion region){
		
		this.obsEvents = obsEvents;
		this.region = region;
		// not necessary -- all gridded regions behave the same regardless of shape
//		if(region instanceof EvenlyGriddedCircularGeographicRegion){
//	    	this.castCircularRegion = (EvenlyGriddedCircularGeographicRegion)this.region;
//	    	this.useCircle = true;
//	    	this.numGridNodes = castCircularRegion.getNumGridLocs();
//		}
//		else {
//	    	this.castSausageRegion = (EvenlyGriddedSausageGeographicRegion)this.region;
//		    this.useSausage = true;
//		    this.numGridNodes = castSausageRegion.getNumGridLocs();
//		}
		this.numGridNodes = this.region.getNodeCount();
    	this.numObsInGrid = new int[numGridNodes];
	    
    	countEventsInCell();
	}
	
//	public int getNumGridnodes(){
//		if (useCircle)
//			return this.castCircularRegion.getNumGridLocs();
//		else //if is sausage
//			return this.castSausageRegion.getNumGridLocs();
//	}
	
	/**
	 * getNumObsInGridList
	 * @return int[]
	 * 
	 */
	public int[] getNumObsInGridList(){
		return this.numObsInGrid;
	}
	
	/**
	 * countEventsInCell
	 * count the number of observed events w/in each grid cell
	 */
	public void countEventsInCell(){
		Location gridCenter;
		LocationList cellLoc;
		double gLong,gLat;
		Location gridCorner1;
		Location gridCorner2;
		Location gridCorner3;
		Location gridCorner4;		
		ObsEqkRupList cellEvents;
		cellLoc = new LocationList();
		int gLoop = 0;
		while ( gLoop < numGridNodes ){
			cellLoc.clear();//clear the locations
			gridCenter = this.region.locationForIndex(gLoop);
//			if (useCircle)
//				gridCenter = this.castCircularRegion.getGridLocation(gLoop);
//			else
//				gridCenter = this.castSausageRegion.getGridLocation(gLoop);
			gLat = gridCenter.getLatitude();
			gLong = gridCenter.getLongitude();
			//create a Location for each corner of the cell
			// add the corner to a LocationList and create a Region
			// so that we can see what events are inside the region.
			gridCorner1 = new Location(gLat + RegionDefaults.gridSpacing/2,gLong + RegionDefaults.gridSpacing/2);
			cellLoc.addLocationAt(gridCorner1,0);
			gridCorner2 = new Location(gLat - RegionDefaults.gridSpacing/2,gLong + RegionDefaults.gridSpacing/2);
			
			cellLoc.addLocationAt(gridCorner2,1);
			gridCorner3 = new Location(gLat - RegionDefaults.gridSpacing/2,gLong - RegionDefaults.gridSpacing/2);
			cellLoc.addLocationAt(gridCorner3,2);
			gridCorner4 = new Location(gLat + RegionDefaults.gridSpacing/2,gLong - RegionDefaults.gridSpacing/2);
			cellLoc.addLocationAt(gridCorner4,3);
			//this creates a Region that is the cell for Loc gLoop
			Region gridRegion = new Region(cellLoc, BorderType.MERCATOR_LINEAR);
			
		    // find ObsEqkRupList of events w/in this grid cell
			cellEvents = obsEvents.getObsEqkRupsInside(gridRegion);
			this.numObsInGrid[gLoop++] = cellEvents.size();				
		}
	}

}
