package org.opensha.step.calc;

import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.faultSurface.FaultTrace;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public final class DistDecayFromRupCalc {
	private static double decayParam = 2.0;
	public static double[] rupDistList;

	public DistDecayFromRupCalc() {
	}

	public static double[] getDensity(FaultTrace faultTrace,
			GriddedRegion aftershockZone) {
		double[] nodePerc = null;
		double sumInvDist = 0;

		int numLocs = aftershockZone.getNodeCount();
		double[] nodeDistFromFault = new double[numLocs];
		double[] invDist = new double[numLocs];
		nodePerc = new double[numLocs];

		//get the iterator of all the locations within that region
		Iterator<Location> zoneIT = aftershockZone.getNodeList().iterator();
		int ind = 0;
		double totDistFromFault = 0;

		// get the summed squared distance to all nodes from the fault trace
		while (zoneIT.hasNext()) {
			nodeDistFromFault[ind++] =
				faultTrace.getHorzDistToClosestLocation( zoneIT.next());
			totDistFromFault = totDistFromFault +
			Math.pow(nodeDistFromFault[ind - 1], decayParam);
		}
		for (int indLoop = 0; indLoop < numLocs; ++indLoop) {
			invDist[indLoop] = totDistFromFault /
			Math.pow(nodeDistFromFault[indLoop], decayParam);
			sumInvDist = sumInvDist + invDist[indLoop];
		}

		for (int indLoop = 0; indLoop < ind - 1; ++indLoop) {
			nodePerc[indLoop] = invDist[indLoop] / sumInvDist;
		}

		return nodePerc;
	}

	/**
	 * setNodePerc
	 * This will taper assign a percentage of the k value that should
	 * be assigned to each grid node.
	 */
	public static double[] getDensity(ObsEqkRupture mainshock,
			GriddedRegion aftershockZone) {
		Location pointRupture;
		Location gLoc;
		double[] nodePerc = null;

		//get the iterator of all the locations within that region
		Iterator<Location> zoneIT = aftershockZone.getNodeList().iterator();
		int ind = 0;
		double totDistFromFault = 0;
		double sumInvDist = 0;
		int numLocs = aftershockZone.getNodeCount();
		double[] nodeDistFromFault = new double[numLocs];
		double[] invDist = new double[numLocs];
		nodePerc = new double[numLocs];

		if (mainshock.getRuptureSurface() == null) {
			// this is a point source fault so get the sum squared distance
			// from all grid nodes to the point source.
			pointRupture = mainshock.getHypocenterLocation();
			while (zoneIT.hasNext()) {
				nodeDistFromFault[ind++] =
					RelativeLocation.getApproxHorzDistance(
							pointRupture, zoneIT.next());
				totDistFromFault = totDistFromFault +
				Math.pow(nodeDistFromFault[ind - 1], decayParam);
			}
		}
		else {
			// this is a rupture surface.  get  the sum squared distance from
			// all grid nodes to the rupture surface.
			EvenlyGriddedSurfaceAPI ruptureSurface = mainshock.getRuptureSurface();

			while (zoneIT.hasNext()) {
				gLoc = zoneIT.next();    	 
				if (gLoc!=null){
					nodeDistFromFault[ind++] = calcRupDist(ruptureSurface,gLoc, aftershockZone.getSpacing());
					//      (Location) zoneIT.next());
					totDistFromFault = totDistFromFault +
					Math.pow(nodeDistFromFault[ind - 1], decayParam);
				}  else{
					nodeDistFromFault[ind++] = -1.0;
				}
			}
			setRupDistList(nodeDistFromFault);
		}

		for (int indLoop = 0; indLoop < numLocs; ++indLoop) {
			if (nodeDistFromFault[indLoop] > 0){
				invDist[indLoop] = totDistFromFault /
				Math.pow(nodeDistFromFault[indLoop], decayParam);
				sumInvDist = sumInvDist + invDist[indLoop];
			}
		}

		for (int indLoop = 0; indLoop < ind  ; ++indLoop) {//TODO check ????  ind - 1 ????? checnged by Baishan
			if (nodeDistFromFault[indLoop] > 0)
				nodePerc[indLoop] = invDist[indLoop] / sumInvDist;
			else
				nodePerc[indLoop] = 0;
		}
		//System.out.println("###getDensity numLocs " + numLocs + " ind " + 	ind 	 );
		return nodePerc;
	}

	/**
	 * getRupDist
	 * @param gridSpacing 
	 */
	private static double calcRupDist(EvenlyGriddedSurfaceAPI ruptureSurface, Location gridLoc, double gridSpacing) { 
		//cutoff distance 
		double cutOffDist = RelativeLocation.getApproxHorzDistance( new Location(gridLoc.getLatitude() + RegionDefaults.CUTOFF_DISTANCE*gridSpacing, 
				gridLoc.getLongitude() ), gridLoc);
		//System.out.println("cutOffDist " + cutOffDist  + " getGridSpacing " + gridSpacing);
		int numRupGrids = ruptureSurface.getLocationList().size();
		double nodeDistFromRup, minDistFromRup = 0;
		if(numRupGrids > 1){//non point rupture, get dist to the rupture line connecting the closest 2 locations      	
			Location loc0 = ruptureSurface.getLocationList().getLocationAt(0);
			Location loc1 = ruptureSurface.getLocationList().getLocationAt(1);
			Location loc2 = ruptureSurface.getLocationList().getLocationAt(2);

			double dist0 = 	minDistFromRup = RelativeLocation.getApproxHorzDistToLine( loc0,loc1 ,gridLoc); 
			double dist1 = 	minDistFromRup = RelativeLocation.getApproxHorzDistToLine( loc2,loc1 ,gridLoc);
			minDistFromRup = dist1 < dist0?dist1:dist0;
			//minDistFromRup = RelativeLocation.getApproxHorzDistToLine( minDistGridLoc,loc1 ,gridLoc); 
			// System.out.println("ruptureSurface " + ruptureSurface .getName() + " mainshock " + mainshock.getHypocenterLocation());
			//System.out.println( "loc1 " +  loc1 + " minDistGridLoc " + minDistGridLoc
			//		 + " " + minDistFromRup );

		}else{//TODO check if correct !!!! point rupture surface	    
			int ind = 0;	   
			ListIterator rupIT = ruptureSurface.listIterator();
			while (rupIT.hasNext()) {
				nodeDistFromRup = RelativeLocation.getApproxHorzDistance(
						(Location) (rupIT.next()), gridLoc);
				if (ind == 0) {
					minDistFromRup = nodeDistFromRup;
					ind++;
				}  else {
					if (nodeDistFromRup < minDistFromRup) {
						minDistFromRup = nodeDistFromRup;
					}
				}
			}
		}

		if(minDistFromRup < cutOffDist){//		
			// System.out.println( minDistFromRup + ", " + minDistFromRup1 + " gridLoc=" + gridLoc + " mainloc " + ruptureSurface.getLocationList().getLocationAt(1));
			minDistFromRup = cutOffDist;/**TODO check correct 
			                             assign it to cutOffDist value to get the highest rates 
			 **/
		}
		//System.out.println(   "minDistFromRup " + minDistFromRup);
		return minDistFromRup;
	}

	/**
	 * setDecayParam
	 * set the exponent to be used for calculating the decay.  Default is 2.
	 */
	public void setDecayParam(double decayParam) {
		this.decayParam = decayParam;
	}

	/**
	 * getDecayParam
	 * return the exponent that is used for calculating the decay.
	 */
	public double getDecayParam() {
		return this.decayParam;
	}
	
	/**
	 * setRupDistList
	 * set the list of distances from each grid node to the fault
	 */
	private static void setRupDistList(double[] nodeDistFromFault ){
		rupDistList = nodeDistFromFault;
	}
	
	/**
	 * getRupDistList
	 */
	public static double[] getRupDistList(){
		return rupDistList;
	}
	 

}
