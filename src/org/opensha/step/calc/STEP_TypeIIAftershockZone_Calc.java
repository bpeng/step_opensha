package org.opensha.step.calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.sha.earthquake.griddedForecast.STEP_CombineForecastModels;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;

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
public class STEP_TypeIIAftershockZone_Calc {
	//private ObsEqkRupList newObsEventsList;
	private GriddedRegion typeIIAftershockZone;
	private LocationList faultSegments;
	private double zoneRadius, gridSpacing;
	//  private Comparator <Location> locationComparatorByLong = new Comparator <Location> (){
	//		@Override
	//		public int compare(Location loc1, Location loc2) {
	//			 System.out.println("## compare loc1 " + loc1 + " loc2 " + loc2);
	//			int longdiff = (int)(100*loc1.getLongitude() - 100*loc2.getLongitude());
	//			if(longdiff == 0){
	//				return (int)(100*loc1.getLatitude() - 100*loc2.getLatitude());
	//			}
	//			return longdiff;
	//		}    	
	//  }; 
	//  
	//  private Comparator <Location> locationComparatorByLat = new Comparator <Location> (){
	//		@Override
	//		public int compare(Location loc1, Location loc2) {
	//			int latdiff = (int)(100*loc1.getLatitude() - 100*loc2.getLatitude());
	//			if(latdiff == 0){
	//				return (int)(100*loc1.getLongitude() - 100*loc2.getLongitude());
	//			}
	//			return latdiff;
	//		}    	
	//  };

	private Comparator <Location> locationComparatorByLong = new Comparator <Location> (){
		@Override
		public int compare(Location loc1, Location loc2) {
			double longdiff = loc1.getLongitude() - loc2.getLongitude();
			if(longdiff == 0){
				double latdiff = loc1.getLatitude() - loc2.getLatitude();
				if(latdiff == 0){
					return 0;
				}else{
					return latdiff > 0 ? 1:-1;
				}
			}
			return longdiff > 0 ? 1: -1;
		}   
	};

	private Comparator <Location> locationComparatorByLat = new Comparator <Location> (){

		@Override
		public int compare(Location loc1, Location loc2) {
			double latdiff = loc1.getLatitude() - loc2.getLatitude();
			if(latdiff == 0){
				double longdiff = loc1.getLongitude() - loc2.getLongitude();
				if(longdiff == 0){
					return 0;
				}else{
					return longdiff > 0 ? 1: -1;
				}
			}
			return latdiff > 0 ? 1:-1;
		}
	}; 


	public STEP_TypeIIAftershockZone_Calc(ObsEqkRupList newObsEventsList, STEP_CombineForecastModels aftershockModel) {
		ObsEqkRupture mainshock = aftershockModel.getMainShock();
		Location mainshockLoc = mainshock.getHypocenterLocation();
		//TODO double check the following 2 variables should be class level
		gridSpacing = aftershockModel.get_GridSpacing();
		zoneRadius = aftershockModel.get_AftershockZoneRadius();
		//double faultRadius = aftershockModel.get_AftershockZoneRadius();
		calc_SyntheticFault(newObsEventsList, mainshockLoc);
	}

	/**
	 * calc_SyntheticFault
	 * This method is not yet finished.  A sort method is needed in LocationList
	 * to sort on lats and longs.
	 */
	public  void calc_SyntheticFault(ObsEqkRupList newObsEventsList, Location mainshockLoc) {
		ListIterator eventIt = newObsEventsList.listIterator();
		int numEvents = newObsEventsList.size();
		//double[] eLat = new double[numEvents];
		//double[] eLong = new double[numEvents];
		ObsEqkRupture event = new ObsEqkRupture();
		Location eLoc = new Location();
		//LocationList latLongList = new LocationList();
		List <Location> latLongList = new ArrayList <Location> ();

		//int ind = 0;
		while (eventIt.hasNext()){
			event = (ObsEqkRupture)eventIt.next();
			eLoc = event.getHypocenterLocation();
			latLongList.add (eLoc);
			//eLat[ind] = eLoc.getLatitude();
			//eLong[ind] = eLoc.getLongitude();
			//ind++;
		}

		/**
		 * sort the lat long pairs and ignore the extreme values (.01 and .99)
		 */
		int minInd = (int)Math.round(0.01*numEvents);
		int maxInd = (int)Math.round(0.99*numEvents);
		int numIn = (int)Math.round(.8*numEvents);

		//Arrays.sort(eLat);
		//Arrays.sort(eLong);  
		//1. sort locations by lattitude 
		Collections.sort(latLongList,locationComparatorByLat);

		double maxLat_LatSort =  latLongList.get(maxInd).getLatitude();//eLat[maxInd];
		double minLat_LatSort =  latLongList.get(minInd).getLatitude(); //eLat[minInd];
		double maxLong_LatSort = latLongList.get(maxInd).getLongitude();
		double minLong_LatSort = latLongList.get(minInd).getLongitude();

		//2. sort by longitude     
		Collections.sort(latLongList,locationComparatorByLong);

		double maxLong_LongSort = latLongList.get(maxInd).getLongitude();//eLong[maxInd];
		double minLong_LongSort = latLongList.get(minInd).getLongitude();//eLong[minInd];
		double maxLat_LongSort = latLongList.get(maxInd).getLatitude();//0;
		double minLat_LongSort = latLongList.get(minInd).getLatitude();;

		/**
		 * THESE WILL NEED TO BE SET ONCE THE SORT METHOD IS
		 * implemented in LocationList
		 */ 
		double latDiff = maxLat_LatSort-minLat_LatSort;
		double longDiff = maxLong_LongSort-minLong_LongSort;

		/** find the largest dimension - either in Lat or in Long
		 *  this needs to be improved
		 */

		faultSegments = new LocationList();
		double topEventLat, topEventLong, bottomEventLat, bottomEventLong;
		Location topEndPoint = new Location();
		Location bottomEndPoint = new Location();
		if (latDiff > longDiff){
			topEndPoint.setLatitude(maxLat_LatSort);
			topEndPoint.setLongitude(maxLong_LatSort);
			bottomEndPoint.setLatitude(minLat_LatSort);
			bottomEndPoint.setLongitude(minLong_LatSort);
		}
		else {
			topEndPoint.setLatitude(maxLat_LongSort);
			topEndPoint.setLongitude(maxLong_LongSort);
			bottomEndPoint.setLatitude(minLat_LongSort);
			bottomEndPoint.setLongitude(minLong_LongSort);
		}

		/**
		 * Create a two segment fault that passes thru the mainshock
		 * using the extreme widths defined above
		 */
		faultSegments.addLocation(topEndPoint);
		faultSegments.addLocation(mainshockLoc);
		//faultSegments.addLocation(mainshockLoc);//TODO check correct?
		faultSegments.addLocation(bottomEndPoint);
	}

	/**
	 * CreateAftershockZoneDef
	 */
	public void CreateAftershockZoneDef() {
		typeIIAftershockZone =
				new GriddedRegion(faultSegments,zoneRadius,gridSpacing, new Location(0,0));
		/**
		 * The rest will have to be filled in for a "Sausage" Geographic
		 * Region on a SausageGeographicRegion is defined.
		 */
	}

	/**
	 * get_TypeIIAftershockZone
	 * This needs to be changed to return a sausage region once
	 * this type of region is defined.
	 */
	public GriddedRegion get_TypeIIAftershockZone() {
		return typeIIAftershockZone;
	}

	/**
	 * getTypeIIFaultModel
	 */
	public LocationList getTypeIIFaultModel() {
		return faultSegments;
	}

}
