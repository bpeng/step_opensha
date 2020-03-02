package org.opensha.sha.earthquake.observedEarthquake;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.opensha.commons.data.Location;
import org.opensha.step.calc.RegionDefaults;



/**
 * customised class to read china earthquake catalogue
 * 
 * 1. note: catalogue time is Local time--GMT+8
 * 
 * @author baishan
 *
 */
public class CubeToObsEqkRuptureChinaFormat extends CubeToObsEqkRupture {
	private static Logger logger = Logger.getLogger(CubeToObsEqkRuptureChinaFormat.class);
	
	public CubeToObsEqkRuptureChinaFormat(String eventFile, Calendar startTime,
			Calendar endTime) throws FileNotFoundException, IOException {
		super(eventFile, startTime, endTime);		
	}

	/* 
	 * yyyymmddhhmmss lat  long   ML  Depth source
 		1970 1 2 614 0 38.10 119.473.20 26  3
 		1x,i4,5(i2),1x,f5.2,1x,f6.2,f4.2,2(i3)
 		
 		issues: 
 		 1. eventID 
 		 2. lat > 90
	 		- yrStr 2007 monthStr  2 dayStr 19 hrStr 12 minuteStr 39 secStr 17 latStr 92.30 lonStr 38.18 magStr 3.20 dpthStr  -1 sourceStr   0
			- yrStr 2007 monthStr  2 dayStr 19 hrStr 12 minuteStr 42 secStr 23 latStr 92.28 lonStr 38.20 magStr 2.80 dpthStr  -1 sourceStr   0
			- yrStr 2007 monthStr  2 dayStr 19 hrStr 12 minuteStr 44 secStr  9 latStr 92.28 lonStr 38.10 magStr 2.90 dpthStr  -1 sourceStr   0
		 
         3. timezone? UTC+8
	 * 
	 * (non-Javadoc)
	 * @see org.opensha.sha.earthquake.observedEarthquake.CubeToObsEqkRupture#readFile(java.lang.String)
	 */
	@Override
	protected ObsEqkRupture readFile(String obsEqkEventstr) {
		obsEqkEventstr = obsEqkEventstr.trim();
		//i4
		String yrStr = obsEqkEventstr.substring(0,4 );
		//5(i2)
		String monthStr = obsEqkEventstr.substring(4,6 );
		String dayStr = obsEqkEventstr.substring(6,8 );
		String hrStr = obsEqkEventstr.substring(8,10 );
		String minuteStr = obsEqkEventstr.substring(10,12 );
		String secStr = obsEqkEventstr.substring(12,14 );
		//1x, f5.2
		String latStr = obsEqkEventstr.substring(14,20 ).trim();
		//1x, f6.2
		String lonStr = obsEqkEventstr.substring(20,27 ).trim();
		//f4.2
		String magStr = obsEqkEventstr.substring(27,31 );
		//i3
		String dpthStr = obsEqkEventstr.substring(31,34 );
		String sourceStr = obsEqkEventstr.substring(34,37 );
		
		
		double lat = Double.parseDouble(latStr);
		double lon = Double.parseDouble(lonStr);
		double depth = Double.parseDouble(dpthStr);
		//logger.info("check 1 latMin" + RegionDefaults.searchLatMin + " latMax " + RegionDefaults.searchLatMax);
		//if lat or lon of the events are outside the region bounds then neglect them.
		if(lat < RegionDefaults.searchLatMin || lat >RegionDefaults.searchLatMax)
			return null;
		if(lon < RegionDefaults.searchLongMin || lon > RegionDefaults.searchLongMax)
			return null;		
		
		int year = Integer.parseInt(yrStr);
		int month = Integer.parseInt(monthStr.trim());
		int day = Integer.parseInt(dayStr.trim());
		int hour = Integer.parseInt(hrStr.trim());
		int min = Integer.parseInt(minuteStr.trim());
		int sec = Integer.parseInt(secStr.trim());
		GregorianCalendar originTime = new GregorianCalendar(TimeZone.getTimeZone("GMT+8:00"));			
		originTime.set(year, month-1, day, hour, min, sec);
		originTime.set(GregorianCalendar.MILLISECOND, 0);//!!
		double mag = Double.parseDouble(magStr);
		
//		if( mag >= 7d) {//
//			logger.info(" latStr " + latStr +  " lonStr " + lonStr
//					 + " magStr " + magStr +  " dpthStr " + dpthStr
//					 + " UTC time " + RegionDefaults.dateformater.format(originTime.getTime()) 
//					 + "  time zone " + TimeZone.getTimeZone("GMT+8:00").getID());
//		}
		//logger.info("check 2");
		
		return new ObsEqkRupture(RegionDefaults.dateformater.format(originTime.getTime()) ,
				sourceStr,'0',
				originTime,0,0,0,
				"",new Location(lat, lon, depth),mag);
		//return null;
	}
	
	/**
	 * Method to test and see if we are reading the network catalog file correctly.
	 * @param args String[]
	 */
	public static void main(String args[]){
		//CubeToObsEqkRupture cubeToRup = null;
		try {
			CubeToObsEqkRuptureChinaFormat	cubeToRup = new CubeToObsEqkRuptureChinaFormat( "data/china/china2ml_197001_201001.eqt",null, null);
			ObsEqkRupList rupList = cubeToRup.getAllObsEqkRupEvents();
			rupList.sortObsEqkRupListByOriginTime();
			int size = rupList.size();
			logger.info("rupList size " + size);
		}
		
		
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}

}
