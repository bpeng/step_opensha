package org.opensha.step.calc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.region.SitesInGriddedRegion;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.WarningParameterAPI;
import org.opensha.commons.param.event.ParameterChangeWarningEvent;
import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.earthquake.griddedForecast.STEP_CombineForecastModels;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.step.STEP_BackSiesDataAdditionObject;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.McVerryetal_2000_AttenRel;
import org.opensha.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.opensha.sha.imr.attenRelImpl.depricated.BA_2006_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.util.SiteTranslator;


public class STEP_HazardDataSet implements ParameterChangeWarningListener{
	private static Logger logger = Logger.getLogger(STEP_HazardDataSet.class);
	private boolean calcProbBySourceFile = false; //TODO !! this will calculate probabilities from source file instead of from step rates from STEP_main
	
	private boolean willSiteClass = false;
	//private boolean willSiteClass = false;
	protected AttenuationRelationship attenRel;
	//public  String STEP_BG_FILE_NAME = RegionDefaults.backgroundHazardPath;
	//private static final String STEP_HAZARD_OUT_FILE_NAME = RegionDefaults.outputHazardPath;
	public static final double IML_VALUE = Math.log(.38);// 0.04; //Math.log(0.126) = -2.071473372030659
	private static final double SA_PERIOD = 1;
	private static final double VS30 = 200;
	public static final String STEP_AFTERSHOCK_OBJECT_FILE = RegionDefaults.STEP_AftershockObjectFile;
	private DecimalFormat locFormat = new DecimalFormat("0.0000");
	protected STEP_main stepMain ;
	private double[] bgProbVals;
	private double[] probVals;
	//indicate this is run as scheduled operation
	private boolean scheduledOperation = false;
	
	// use mag weighting in attenuation - only works with McVerry for now (??)
	private boolean USE_MAG_WEIGHTING = true;
    // use stress drop scaling - only works in McVerry for now and only for SA 0.5!!!
	private boolean USE_STRESSDROP_SCALAR = true;

	public STEP_HazardDataSet(boolean includeWillsSiteClass){
		this.willSiteClass = includeWillsSiteClass;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		STEP_HazardDataSet step = new STEP_HazardDataSet(false);		
		//args = new String []{"1","c","20","30"}; //TODO test, remove!!
		//args = new String []{"1","c"}; //TODO test, remove!!
		//args = new String []{"1","s"}; //TODO test, remove!!
		if(args.length > 1 && ("c".equalsIgnoreCase(args[1]))){//run continuous test 
			if(args.length > 2){
				try{
					int runs = Integer.parseInt(args[2]);
					int interval = Integer.parseInt(args[3]);
					step.continuesTest(runs,interval);
				}catch(Exception e){
					logger.error("Please specify number of runs and time interval!!!!" + e);
					System.exit(1);
				}
			}else{//run default continuous test
				step.defaultContinuesTest();
			}
		//scheduled operation, e.g. once 1 hour, read event from last run			
		} else if(args.length > 1 && ("s".equalsIgnoreCase(args[1]))){//
			 //
			step.runScheduledOperation();
		}else{
			step.runSTEP(null,null);
			
		}		
		logger.info("STEP is finito!");
	}

    
	/**
	 * run scheduled operation of step application
	 * the schedule is made as cron job, basically once an hour/day
	 * get the last current time, and
	 * look back N day as events during that period are subject to change
	 * 
	 */
	private void runScheduledOperation() {
		//set this to true for the timeSpan to work correctly
		RegionDefaults.startForecastAtCurrentTime = true;
		stepMain = new STEP_main();
		this.scheduledOperation  = true;
		stepMain.setScheduledOperation(true);
		GregorianCalendar  startTime = STEP_main.getCurrentGregorianTime();	
	     startTime.setTimeInMillis(RegionDefaults.EVENT_START_TIME.getTimeInMillis()); //
		runSTEP(startTime,STEP_main.getCurrentGregorianTime());		
	}

	/**
	 * run continueous test 
	 * test default specified times
	 * e.g. 1 minute, 1 hour, 1,3,7,30,100,300 days after main shock
	 */
	private void defaultContinuesTest() {
		//set this to true for the timeSpan to work correctly
		RegionDefaults.startForecastAtCurrentTime = true;
		
		if(RegionDefaults.EVENT_START_TIME == null){
			logger.error("event start time must be specified in config file!!");
			System.exit(1);
		}
		
		String conTestTimes = RegionDefaults.DEFAULT_TEST_TIMES;
		logger.info("-------------- defaultContinuesTest ----------------conTestTimes = " + conTestTimes);	
		if(conTestTimes == null){
			logger.error("Please specify default.test.times!!!"  );
			System.exit(1);
		}		
		String[] conTestTimesArr = conTestTimes.split("}");
		if(conTestTimesArr.length != 3){
			logger.error("Please specify default.test.times correctly!!!"  );
			System.exit(1);
		}
		
		int[] minutes = string2Array(conTestTimesArr[0].replace("{", ""));		
		int[] hours = string2Array(conTestTimesArr[1].replace("{", ""));	
		int[] days = string2Array(conTestTimesArr[2].replace("{", ""));	
		//just read n days back from event start time
		GregorianCalendar startTime = STEP_main.getCurrentGregorianTime();	
		startTime.setTimeInMillis(RegionDefaults.EVENT_START_TIME.getTimeInMillis()
				     - RegionDefaults.DEFAULT_TEST_READ_BACK*24L*60*60*1000); //
		
		GregorianCalendar forecastTime = STEP_main.getCurrentGregorianTime();		 	
		forecastTime.setTimeInMillis(RegionDefaults.EVENT_START_TIME.getTimeInMillis());
	    //1. run minutes after main shock
		//int[] minutes = new int[]{1};
		if(minutes.length > 0){
			 runContinueousTest(startTime, forecastTime, minutes, 60*1000l, 'm');
		}
		
		//2. run hours after main shock	
		if(hours.length > 0){
		   runContinueousTest(startTime, forecastTime, hours, 60*60*1000l, 'h');
		}
		
		//3. run days after main shock	
		if(days.length > 0){
		  runContinueousTest(startTime, forecastTime, days, 24*60*60*1000l, 'd');	
		}
	}
	

	/**
	 * transfer a string to array
	 * @param timeStr -- format:1,2,3
	 * @return -- int[]{1,2,2}
	 */
	private int[] string2Array(String timeStr) {
		String [] timeArr = timeStr.split(",");
		int [] timeArrInt = new int[timeArr.length];
		for(int index = 0; index < timeArr.length; index++){
			timeArrInt[index] = Integer.parseInt(timeArr[index]);
		}
		return timeArrInt;
	}

	/**
	 * run continuous test for specified times 
	 * @param startTime -- start time for reading events
	 * @param forecastTime -- forecast start time, also end time for reading events
	 * @param times -- the array of times storing time after main shock
	 * @param timeMultiplier -- time multiplier to convert the times to millisec
	 * @param timeIndicator -- day, hour or minutes
	 */
	private void runContinueousTest(GregorianCalendar startTime,
			GregorianCalendar forecastTime, int[] times, long timeMultiplier, char timeIndicator) {		
		
		//set this to true for the timeSpan to work correctly
		RegionDefaults.startForecastAtCurrentTime = true;
		for(int time: times)	{
			logger.info("-------------- " + time + " " + timeIndicator + "  ----------------");			
			forecastTime.setTimeInMillis(RegionDefaults.EVENT_START_TIME.getTimeInMillis() +  time*timeMultiplier);
			RegionDefaults.outputAftershockRatePath = RegionDefaults.OUTPUT_DIR + "/TimeDepRates_con_" +  time + timeIndicator + ".txt";
			RegionDefaults.outputSTEP_Rates = RegionDefaults.OUTPUT_DIR + "/STEP_Rates_con_" +  time + timeIndicator + ".txt";
			RegionDefaults.outputHazardPath = RegionDefaults.OUTPUT_DIR + "/STEP_Probs_con_" +  time + timeIndicator + ".txt";
			runSTEP(startTime, forecastTime);
			//update start time
			if(startTime == null){
				startTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			}
			startTime.setTimeInMillis(forecastTime.getTimeInMillis());
			try {
				Thread.sleep(500);				
			} catch (InterruptedException e) {				
				logger.error(e);
			}
		}		
	}

	/**
	 * run continuous test every specified minutes from main shock
	 */
	private void continuesTest(int runs, int interval) {
		if(RegionDefaults.EVENT_START_TIME == null){
			logger.error("event start time must be specified in config file!!");
			System.exit(1);
		}
		
		GregorianCalendar forecastTime = STEP_main.getCurrentGregorianTime();		 	
		forecastTime.setTimeInMillis(RegionDefaults.EVENT_START_TIME.getTimeInMillis());
	     
		//forecastTime.set(2009, 4, 30, 13, 25, 03); //main shock time
		GregorianCalendar startTime = null;
		for(int numRun = 0; numRun < runs; numRun++)	{
			logger.info("-------------- " + numRun + " ----------------");
			
		    forecastTime.setTimeInMillis(forecastTime.getTimeInMillis() + interval*60*1000l);
		     //log("forecastTime " + forecastTime.getTime());
		     //change output file accordingly, outputAftershockRatePath =  INPUT_DIR + "/TimeDepRates.txt";		
			RegionDefaults.outputAftershockRatePath = RegionDefaults.OUTPUT_DIR + "/TimeDepRates_con_" + (numRun*interval) + "m.txt";	
			RegionDefaults.outputSTEP_Rates = RegionDefaults.OUTPUT_DIR + "/STEP_Rates_con_" + (numRun*interval) + "m.txt";
			RegionDefaults.outputHazardPath = RegionDefaults.OUTPUT_DIR + "/STEP_Probs_con_" + (numRun*interval) + "m.txt";
			
			runSTEP(startTime, forecastTime);
			
			try {
				Thread.sleep(500);				
			} catch (InterruptedException e) {				
				logger.error(e);
			}
			//set the current time as the start time for next run
			if(startTime == null){
				startTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			}
			startTime .setTimeInMillis(forecastTime.getTimeInMillis());
		}
		
	}

	/**
	 * run step code to calculate forecast for specified time
	 * @param start start time to read event from file
	 * @param forecastTime
	 */
	public void runSTEP(GregorianCalendar start, GregorianCalendar forecastTime){
//		if(start != null) logger.info("start " + RegionDefaults.dateformater.format(start.getTime()));
//		logger.info("forecastTime " + RegionDefaults.dateformater.format(forecastTime.getTime()));
//		
//		if(forecastTime != null) return;
		//1. create step main
		if(!calcProbBySourceFile){
			runStepmain(start,forecastTime);
			logger.info("STEP earthquake rates are done.");
		}else{
			if(stepMain == null){
			   stepMain = new STEP_main();
			}
			stepMain.loadBgGrid();
		}
		
		//2. 
		createShakeMapAttenRelInstance();

		//3.get default region
		SitesInGriddedRegion regionSites = getDefaultRegion();//
		logger.info("getNumGridLocs=" + regionSites.getRegion().getNodeCount());	
//		for(Location loc:region.getGridLocationsList()){
//			System.out.println("loc=" +loc.getLatitude() + "," + loc.getLongitude());
//		}
		
		//4. calc probability values
		double[] stepBothProbVals = calcStepProbValues(regionSites);
		
		//5. output
		saveProbValues2File(stepBothProbVals,regionSites);
		
		//5.1. backup aftershocks
		if(RegionDefaults.SAVE_MODELS  ){//TODO !!for Darfield quake, don't save models at all!! 6/9/2010
		  stepMain.saveModels();
		}
		
	}


	/**
	 * 
	 */
	public void runStepmain(GregorianCalendar startTime,GregorianCalendar forecastTime) {
		
	    if(stepMain == null){
		   stepMain = new STEP_main();
		}
		//1. step main
		stepMain.calc_STEP(startTime,forecastTime);		
		
	}

	/**
	 * @return
	 *
	 */
	public SitesInGriddedRegion getDefaultRegion() {
		return new SitesInGriddedRegion( stepMain.getBgGrid().getDefaultRegion());
	}

	/**
	 * @param region
	 * @return
	 */
	public double[] calcStepProbValues(SitesInGriddedRegion region ) {
		region.addSiteParams(attenRel.getSiteParamsIterator());
		//getting the Attenuation Site Parameters Liat
		ListIterator it = attenRel.getSiteParamsIterator();
		//creating the list of default Site Parameters, so that site parameter values can be filled in
		//if Site params file does not provide any value to us for it.
		ArrayList defaultSiteParams = new ArrayList();
		SiteTranslator siteTrans= new SiteTranslator();
		while(it.hasNext()){
			//adding the clone of the site parameters to the list
			ParameterAPI tempParam = (ParameterAPI)((ParameterAPI)it.next()).clone();
			//getting the Site Param Value corresponding to the Will Site Class "DE" for the seleted IMR  from the SiteTranslator
			//siteTrans.setParameterValue(tempParam, siteTrans.WILLS_DE, Double.NaN);
			//defaultSiteParams.add(tempParam);
		}
		if(willSiteClass){
			region.setDefaultSiteParams(defaultSiteParams);
			region.setSiteParamsForRegionFromServlet(true);
		}
		//read background hazard values from file
		bgProbVals = loadBgProbValues(region,RegionDefaults.backgroundHazardPath);
		//get hazards values from new events
		ArrayList<PointEqkSource> sourceList;
		if(calcProbBySourceFile){
			sourceList = this.createStepSourcesFromFile();
		}else{
			sourceList = stepMain.getSourceList();
		}
		probVals = this.clacProbVals(attenRel, region, sourceList);
		//combining the backgound and Addon dataSet and wrinting the result to the file
		STEP_BackSiesDataAdditionObject addStepData = new STEP_BackSiesDataAdditionObject();
		return  addStepData.addDataSet(bgProbVals,probVals);

	}

	/**
	 * 
	 */
	public void createShakeMapAttenRelInstance(){
		// make the imr McVerryetal_2000_AttenRel ShakeMap_2003_AttenRel
		//attenRel = new McVerryetal_2000_AttenRel(this);
		System.out.println("Warning: using BA Attenuation!!");
		// WILL ALSO NEED TO CHANGE BACK SITE CLASS INFO
		attenRel = new BA_2006_AttenRel(this);
		// set the im as PGA
		//attenRel.setIntensityMeasure(PGA_Param.NAME);
		//attenRel.setIntensityMeasure(((ShakeMap_2003_AttenRel)attenRel).PGA_Param.NAME);
		//attenRel.setIntensityMeasure(((ShakeMap_2003_AttenRel)attenRel).SA_Param.NAME, SA_PERIOD);
		attenRel.setParamDefaults();
		//((McVerryetal_2000_AttenRel) attenRel).setUseMagWeight(USE_MAG_WEIGHTING);
		//((McVerryetal_2000_AttenRel) attenRel).setUSE_STRESSDROP_SCALER(USE_STRESSDROP_SCALAR);
	    attenRel.setIntensityMeasure(SA_Param.NAME);
	    //attenRel.setIntensityMeasure(PGA_Param.NAME);
	    attenRel.getParameter(PeriodParam.NAME).setValue(SA_PERIOD);
	    attenRel.getParameter(Vs30_Param.NAME).setValue(VS30);
	    //attenRel.getParameter(PeriodParam.NAME).setValue(SA_PERIOD);
	    
		//attenRel.setIntensityMeasure(((BA_2006_AttenRel)attenRel).SA_Param.NAME, SA_PERIOD);

	}
	//}



	/**
	 * craetes the output xyz files
	 * @param probVals : Probablity values ArrayList for each Lat and Lon
	 * @param fileName : File to create
	 */
	private void saveProbValues2File(double[] probVals,SitesInGriddedRegion sites){
		//int size = probVals.length;
		LocationList locList = sites.getRegion().getNodeList();
		int numLocations = locList.size();
		File existingFile = new File(RegionDefaults.outputHazardPath);
		if(this.scheduledOperation && existingFile.exists()){
			stepMain.backupFile(existingFile, this.stepMain.getLastCurrTime());
		}
		
        logger.info("saveProbValues2File " + new File(RegionDefaults.outputHazardPath).getAbsolutePath());
		try{
			FileWriter fr = new FileWriter(RegionDefaults.outputHazardPath);
			for(int i=0;i<numLocations;++i){
				Location loc = locList.getLocationAt(i);
				// System.out.println("Size of the Prob ArrayList is:"+size);
				fr.write(locFormat.format(loc.getLatitude())+"    " + locFormat.format(loc.getLongitude())+"      "+convertToProb(probVals[i])+"\n");
			}
			fr.close();
		}catch(IOException ee){
			ee.printStackTrace();
		}
	}

	private double convertToProb(double rate){
		return (1-Math.exp(-1*rate*RegionDefaults.forecastLengthDays));
	}

	/**
	 * returns the prob for the file( fileName)
	 * 
	 * number and order of locations should match those
	 * in grid loactions and the hypMagFreqAtLocs in SETP_main
	 * 
	 * TODO this is very inefficient, remove the map if the order in the bg 
	 *      file is the same as in the bg grid
	 * 
	 * @param fileName : Name of the file from which we collect the values
	 */
	public double[] loadBgProbValues(SitesInGriddedRegion sites,String fileName){
		BackGroundRatesGrid bgGrid = stepMain.getBgGrid();
		logger.info("loadBgProbValues numSites =" + sites.getRegion().getNodeCount() + " fileName=" + fileName);		
		double[] vals = new double[sites.getRegion().getNodeCount()];	
		 HashMap<String,Double> valuesMap = new  HashMap<String,Double>();
		try{
			ArrayList fileLines = FileUtils.loadFile(fileName);
			ListIterator it = fileLines.listIterator();
			STEP_main.log("fileLines.size() =" + fileLines.size());
			//int i=0;
			while(it.hasNext()){
				//if(i >= numSites) break;
				StringTokenizer st = new StringTokenizer((String)it.next());
				String latstr =st.nextToken().trim();
				String lonstr =st.nextToken().trim();
				String val =st.nextToken().trim();
				// get lat and lon
				double lon =  Double.parseDouble(lonstr );
				double lat =  Double.parseDouble(latstr);
				//STEP_main.log("lat =" + lat + " lon=" + lon);
				Location loc = new Location(lat,lon,BackGroundRatesGrid.DEPTH);
				double temp =0;
				if(!val.equalsIgnoreCase("NaN")){
					temp=(new Double(val)).doubleValue();
					//vals[i++] = convertToRate(temp);
					//vals[index] = convertToRate(temp);
				} else{
					temp=(new Double(Double.NaN)).doubleValue();
					//vals[i++] = convertToRate(temp);
					//vals[index] = convertToRate(temp);
				}
				valuesMap.put(bgGrid.getKey4Location(loc), temp);
			}
			//convert to an array in the order of the region grids locations
			//STEP_main.log(">> sites.getRegion().getNodeCount() =" + sites.getRegion().getNodeCount() );
			for(int i = 0; i < sites.getRegion().getNodeCount(); i++){
				Location loc = sites.getRegion().locationForIndex(i);
				Double val = valuesMap.get(bgGrid.getKey4Location(loc));
				//if (val==null) STEP_main.log(">> loc " + loc  +  " val " + val );
				vals[i] = val==null?0:val;
				//STEP_main.log(">> vals[" + i + "] =" + vals[i]  );
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return vals;
	}


	/**
	 * @param prob
	 * @return
	 */
	private double convertToRate(double prob){
		return (-1*Math.log(1-prob)/RegionDefaults.forecastLengthDays);
	}
	
	/**
	 * HazardCurve Calculator for the STEP
	 * @param imr : ShakeMap_2003_AttenRel for the STEP Calculation
	 * @param sites
	 * @param eqkRupForecast : STEP Forecast
	 * @returns the ArrayList of Probability values for the given region
	 *           --in the same order of the region grids
	 */
	public double[] clacProbVals(AttenuationRelationship imr,SitesInGriddedRegion sites,
			ArrayList sourceList){		
		double[] probVals = new double[sites.getRegion().getNodeCount()];
		double MAX_DISTANCE = 500;

		// declare some varibles used in the calculation
		double qkProb, distance;
		int k,i;
		try{
			// get total number of sources
			int numSources = sourceList.size();

			// this boolean will tell us whether a source was actually used
			// (e.g., all could be outside MAX_DISTANCE)
			boolean sourceUsed = false;

			int numSites = sites.getRegion().getNodeCount();
			int numSourcesSkipped =0;
			//long startCalcTime = System.currentTimeMillis();
			logger.info("--- clacProbVals numSites "  + numSites + " numSources "  + numSources);
			//clacProbVals numSites 22260 numSources 312
			for(int j=0; j< numSites;++j){
				sourceUsed = false;
				double hazVal =1;
				double condProb =0;
				Site site = sites.getSite(j);
				System.out.println("Warning: No McVerry Site Class Set!");
				//site.setValue("McVerryetal Site Type", "D-Soft-or-Deep-Soil");
				//site.setValue("McVerryetal Site Type", "C-Shallow-Soil");
				//imr.setSite(site);
				
				//adding the wills site class value for each site
				// String willSiteClass = willSiteClassVals[j];
				//only add the wills value if we have a value available for that site else leave default "D"
				//if(!willSiteClass.equals("NA"))
				//imr.getSite().getParameter(imr.WILLS_SITE_NAME).setValue(willSiteClass);
				//else
				// imr.getSite().getParameter(imr.WILLS_SITE_NAME).setValue(imr.WILLS_SITE_D);

				// loop over sources
				for(i=0;i < numSources ;i++) {
					// get the ith source
					ProbEqkSource source = (ProbEqkSource)sourceList.get(i);
					// compute it's distance from the site and skip if it's too far away
					distance = source.getMinDistance(sites.getSite(j));
					if(distance > MAX_DISTANCE){
						++numSourcesSkipped;
						//update progress bar for skipped ruptures
						continue;
					}
					// indicate that a source has been used
					sourceUsed = true;
					//logger.info("---> getTotExceedProbability"  );
					hazVal *= (1.0 - imr.getTotExceedProbability((PointEqkSource)source,IML_VALUE));
					//imr.getIML_AtExceedProb(PROB_VALUE);
					//logger.info("<--- getTotExceedProbability"  );
				}

				// finalize the hazard function
				if(sourceUsed) {
					//System.out.println("HazVal:"+hazVal);
					hazVal = 1-hazVal;
				} else {
					hazVal = 0.0;
				}
				//System.out.println("HazVal: "+hazVal);
				probVals[j]=this.convertToRate(hazVal);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		logger.info("<<< clacProbVals"  );
		return probVals;
	}

	public double[] getBgProbVals() {
		return bgProbVals;
	}

	public double[] getProbVals() {
		return probVals;
	}

	/**
	 *  Function that must be implemented by all Listeners for
	 *  ParameterChangeWarnEvents.
	 *
	 * @param  event  The Event which triggered this function call
	 */
	public void parameterChangeWarning( ParameterChangeWarningEvent e ){

		String S =  " : parameterChangeWarning(): ";

		WarningParameterAPI param = e.getWarningParameter();

		//System.out.println(b);
		param.setValueIgnoreWarning(e.getNewValue());

	}

	public STEP_main getStepMain() {
		return stepMain;
	}

	public void setStepMain(STEP_main stepMain) {
		this.stepMain = stepMain;
	}

	public AttenuationRelationship getAttenRel() {
		return attenRel;
	}  
	
	/**
	 * load step sources from file to calc probabilties
	 * @param 
	 */
	public ArrayList <PointEqkSource> createStepSourcesFromFile(){
		System.out.println("createStepSourcesFromFile");
		BackGroundRatesGrid bgGrid = stepMain.getBgGrid();
		ArrayList<PointEqkSource>	sourceList = new ArrayList<PointEqkSource>();
		ArrayList<Location>	locList = new ArrayList<Location>();
		HashMap<String,IncrementalMagFreqDist> magFreqMap = new  HashMap<String,IncrementalMagFreqDist>();

		ArrayList<String> fileLines;
		try {
			fileLines = FileUtils.loadFile(RegionDefaults.STEP_SORCE_FILE);		
			for(String line:fileLines){//create the magFrquencyDist
				line = line.trim();
				String[] sourceElements = line.split(RegionDefaults.PATTERN_SPACE);
				//System.out.println("createStepSourcesFromFile sourceElements " + sourceElements[0]);
				//System.out.println("createStepSourcesFromFile sourceElements " + sourceElements.length);
				double lat = Double.parseDouble(sourceElements[1].trim());
				double lon = Double.parseDouble(sourceElements[0].trim());
				if(lat < RegionDefaults.searchLatMin || lat >RegionDefaults.searchLatMax)
					continue;
				if(lon < RegionDefaults.searchLongMin || lon > RegionDefaults.searchLongMax)
					continue;
				
				double mag = Double.parseDouble(sourceElements[2].trim());
				//ignoring mags below 5.0
				if (mag < 5) continue;
				
				double prob = Double.parseDouble(sourceElements[3].trim());
				Location loc = new Location(lat, lon);
				// SETTING DEPTH TO 5km
				loc.setDepth(5);
				String locKey = bgGrid.getKey4Location(loc);
				IncrementalMagFreqDist magDist;
				if(magFreqMap.get(locKey) == null){
					int numForecastMags = 1 + (int) ( (RegionDefaults.maxForecastMag - 5.0) 
							/ RegionDefaults.deltaForecastMag);
					magDist = new IncrementalMagFreqDist(5.0, 
							RegionDefaults.maxForecastMag, numForecastMags);					
					locList.add(loc);
				}else{
					magDist = magFreqMap.get(locKey);
				}
				//magDist.add(mag, prob);//add the mag frequency
				int x = magDist.getXIndex(mag );
				magDist.set(x, prob);     
				magFreqMap.put(locKey, magDist);//put it back
				 

			}
			//create the sources
			System.out.println("Warning: Setting rake to -100!");
			for(Location loc: locList){
				IncrementalMagFreqDist magDist = magFreqMap.get(bgGrid.getKey4Location(loc));
				PointEqkSource source  = new PointEqkSource(loc,magDist,
						RegionDefaults.forecastLengthDays,RegionDefaults.RAKE,
						RegionDefaults.DIP,RegionDefaults.minForecastMag);
				sourceList.add(source); 				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("createStepSourcesFromFile sourceList " + sourceList.size());
		//log("sourceList >>" + sourceList.size() + " zeroSource " + zeroSource + " nullSource " + nullSource);
		return sourceList;
	}

	
}
