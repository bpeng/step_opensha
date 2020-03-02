package org.opensha.sha.earthquake.observedEarthquake;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.opensha.commons.data.Location;
import org.opensha.step.calc.RegionDefaults;

public class CubeToObsEqkRuptureCsepFormat extends CubeToObsEqkRupture {
	private static Logger logger = Logger.getLogger(CubeToObsEqkRuptureChinaFormat.class);
	
	public CubeToObsEqkRuptureCsepFormat(String eventFile, Calendar startTime,
			Calendar endTime) throws FileNotFoundException, IOException {
		super(eventFile, startTime, endTime);		
	}

	/* 
	 * 	1				2				3				4				5				6				7				8				9				10				11				12				13				14				15				 
		lon				lat				dec year		month			day				mag				depth			hour			minute			sec				horz.err		depth.err		mag.err	
	 * (non-Javadoc)
	 * @see org.opensha.sha.earthquake.observedEarthquake.CubeToObsEqkRupture#readFile(java.lang.String)
	 */
	@Override
	protected ObsEqkRupture readFile(String obsEqkEventstr) {
		obsEqkEventstr = obsEqkEventstr.trim();
		String [] eventsParams = obsEqkEventstr.split(RegionDefaults.PATTERN_SPACE);	
		String yrStr = eventsParams[2];
		//5(i2)
		String monthStr =eventsParams[3];
		String dayStr = eventsParams[4];
		String hrStr = eventsParams[7];
		String minuteStr = eventsParams[8];
		String secStr = eventsParams[9];
		//1x, f5.2
		String latStr = eventsParams[1];
		//1x, f6.2
		String lonStr = eventsParams[0];
		//f4.2
		String magStr = eventsParams[5];
		//i3
		String dpthStr = eventsParams[6];
		String magErrStr = eventsParams[12];
		String depthErrStr = eventsParams[11];
		String horrErrStr = eventsParams[10];		
		
		double lat = Double.parseDouble(latStr);
		double lon = Double.parseDouble(lonStr);
		double depth = Double.parseDouble(dpthStr);
		//logger.info("check 1 latMin" + RegionDefaults.searchLatMin + " latMax " + RegionDefaults.searchLatMax);
		//if lat or lon of the events are outside the region bounds then neglect them.
		if(lat < RegionDefaults.searchLatMin || lat >RegionDefaults.searchLatMax)
			return null;
		if(lon < RegionDefaults.searchLongMin || lon > RegionDefaults.searchLongMax)
			return null;		
		if(depth > RegionDefaults.MAX_Z )
			return null;
		
		int year = (int)Double.parseDouble(yrStr);
		int month = (int)Double.parseDouble(monthStr.trim());
		int day = (int)Double.parseDouble(dayStr.trim());
		int hour = (int)Double.parseDouble(hrStr.trim());
		int min = (int)Double.parseDouble(minuteStr.trim());
		int sec = (int)Double.parseDouble(secStr.trim());
		GregorianCalendar originTime = new GregorianCalendar(TimeZone.getTimeZone("GMT+8:00"));			
		originTime.set(year, month-1, day, hour, min, sec);
		originTime.set(GregorianCalendar.MILLISECOND, 0);//!!
		double mag = Double.parseDouble(magStr);		
		double horzErr =0, vertErr=0, magErr=0;
		if(horrErrStr !=null && !horrErrStr.equals(""))
			horzErr = Double.parseDouble(horrErrStr);
		if(depthErrStr !=null && !depthErrStr.equals(""))
			vertErr = Double.parseDouble(depthErrStr);
		if(magErrStr !=null && !magErrStr.equals(""))
			magErr = Double.parseDouble(magErrStr);		
	
		return new ObsEqkRupture(RegionDefaults.dateformater.format(originTime.getTime()) ,
				"CS",'0',
				originTime,horzErr,vertErr,magErr,
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
			CubeToObsEqkRuptureCsepFormat	cubeToRup = new CubeToObsEqkRuptureCsepFormat( "data/csep/example_CSEP_cat.txt",null, null);
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