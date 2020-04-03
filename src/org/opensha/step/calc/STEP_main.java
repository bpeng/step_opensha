package org.opensha.step.calc;



import jargs.gnu.CmdLineParser;
import org.apache.log4j.Logger;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.earthquake.griddedForecast.STEP_CombineForecastModels;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


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
public class STEP_main {
	private static Logger logger = Logger.getLogger(STEP_main.class);

	private  GregorianCalendar currentTime; //forecast start time, also the end time to load events
	GregorianCalendar lastCurrTime; //crrent time of last run (if STEP_Model has been serialized)
	private  GregorianCalendar eventStartTime; //the start time to read events from file, for testing
	//private final static String BACKGROUND_RATES_FILE_NAME = RegionDefaults.BACKGROUND_RATES_FILE_NAME;//RegionDefaults.INPUT_DIR + "/NZdailyRates.txt"; //AllCal96ModelDaily.txt

	private DecimalFormat locFormat = new DecimalFormat("0.0000");
	private DecimalFormat magFormat = new DecimalFormat("0.00");
	public static SimpleDateFormat localDateformater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private String eventsFilePath = RegionDefaults.cubeFilePath;
	private String bgRatesFilePath = RegionDefaults.BACKGROUND_RATES_FILE_NAME;
	//indicate this is run as scheduled operation
	private boolean scheduledOperation = false;
	/*//when a main shock falls into the aftershock zone of a sausage model, and it is added to the aftershock list of the sausage model, 
	 * and its aftershocks are added as well, shall the aftershocks be checked as well to see each of them are winthin the sausage aftershock zone?
	 */
	private boolean recheckAftershocks4sausageModel = false; 
	//public static final String STEP_AFTERSHOCK_OBJECT_FILE = RegionDefaults.STEP_AftershockObjectFile;

	private UpdateSTEP_Forecast updateModel;
	
	/**
	 * First load the active aftershock sequence objects from the last run
	 * load: ActiveSTEP_AIC_AftershockForecastList
	 * each object is a STEP_AftershockHypoMagFreqDistForecast
	 * 
	 */
	private  ArrayList <STEP_CombineForecastModels> STEP_AftershockForecastList;// for EQ event 
	private BackGroundRatesGrid bgGrid = null;
	private ArrayList<PointEqkSource> sourceList; //for each bg grid

	public STEP_main() {		
	}


	/**
	 *
	 * @param args String[]
	 * 0 or no arg-- run STEP_main
	 * 1 -- run STEP_HazardDataSet
	 * others -- print help
	 */
	public static void main(String[] args) {	
		//args = new String []{"0","-f", "data/csep/CSEP_params.txt"}; //TODO test, remove!!
		//check param file is specified!! for csep only
		long start = System.currentTimeMillis();
		CmdLineParser parser = new CmdLineParser();     
		CmdLineParser.Option paramFile = parser.addStringOption('f', "file");
		try {
			parser.parse(args);
			String paramFilePath = (String)parser.getOptionValue(paramFile);
			log(">> BBB paramFilePath=" + paramFilePath);
//			System.out.println("TEST!!$$$"+RegionDefaults.PARAM_FILE_BY_COMMAND_ARGS);
			if(RegionDefaults.EVENT_DATA_SOURCE == RegionDefaults.EVENT_SOURCE_CSEP 
					&& RegionDefaults.PARAM_FILE_BY_COMMAND_ARGS){
				setCsepParamFile(paramFilePath);
			}
		} catch ( CmdLineParser.OptionException e ) {
			System.err.println(e.getMessage());
			printUsage();
			System.exit(2);
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			printUsage();
			System.exit(2);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			printUsage();
			System.exit(2);
		}

		// The remaining command-line arguments -- those that do not start
		// with a minus sign -- can be captured like this:
		args = parser.getRemainingArgs();
		System.out.println("args.length" + args.length);
		if(args.length > 0  ){
			if("1".equals(args[0])){//run 
				log("run STEP_HazardDataSet");
				STEP_HazardDataSet.main(args);
			}else if("0".equals(args[0])){//run {
				STEP_main step = new STEP_main();
				step.calc_STEP(null,null);
			}else {//print help
				printUsage();
			}			
		}else{	//default	
			STEP_main step = new STEP_main();
			step.calc_STEP(null,null);
			//System.out.println("args[0] Options:\n0 -- run STEP_main\n1 -- run STEP_HazardDataSet \n  args[1] Options:c -- run continuously, args[2]specifies number of runs, args[3] specifies time interval in minutes. e.g. 1 c 10 30 \n2 -- help");
		}	
		long elapsedTimeMillis = System.currentTimeMillis()-start;
		float elapsedTimeMin = elapsedTimeMillis/(60*1000F);
		System.out.println(elapsedTimeMin);
	}

	/**
	 * set param-file-path from commandline
	 * @param paramFilePath
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static void setCsepParamFile(String paramFilePath) throws FileNotFoundException, IOException {
		if(paramFilePath == null ){
			System.err.println("Please enter a valid parameter file!");
			printUsage();
			System.exit(2);
		}else{
			RegionDefaults.setCsepParams(paramFilePath);   
		}		
	}


	private static void printUsage() {
		System.out.println(" Usage:\n run_step [0] {-f param-file-path}  \n run_step [1] {-f param-file-path}  \n run_step [1] {c} {-f param-file-path}  \n run_step [1] {s} {-f param-file-path}  \n");

		//System.out.println("args[0] Options:\n0 -- run STEP_main\n1 -- run STEP_HazardDataSet \n  args[1] Options:c -- run continuouslys, s -- run as scheduled operation \n2 -- help");

	}


	/**
	 * Returns the Gridded Region applicable for STEP forecast
	 * @return
	 */
	public GriddedRegion getGriddedRegion(){
		return bgGrid.getRegion();
	}


	public  ArrayList <STEP_CombineForecastModels> getSTEP_AftershockForecastList(){
		return STEP_AftershockForecastList;
	}

	/**
	 * Returns the List of PointEqkSources
	 * @return
	 */
	public ArrayList<PointEqkSource> getSourceList(){
		return sourceList;
	}

	/**
	 * calc_STEP
	 */
	public  void calc_STEP(GregorianCalendar startTime, GregorianCalendar forecastTime) {
		//ArrayList New_AftershockForecastList = null;	
		//read serialized model objects from file
		readSTEP_AftershockForecastListFromFile();		
		if(this.isScheduledOperation()){//get last current time			
			//startTime = getLastCurrTime();
			if(startTime != null){
				//look back N day as events during that period are subject to change
				//startTime.setTimeInMillis(startTime.getTimeInMillis() - RegionDefaults.daysFromQDM_Cat*24L*60*60*1000);
			}
		}

		log("Starting STEP startTime=" + startTime);	

		eventStartTime = null;
		if(startTime != null){
			this.eventStartTime = startTime;
		}else if(!this.isScheduledOperation() && RegionDefaults.EVENT_START_TIME != null){//use default specified
			eventStartTime = getCurrentGregorianTime();	
			eventStartTime.setTimeInMillis(RegionDefaults.EVENT_START_TIME.getTimeInMillis()); //
		}

		//backupFile(new File(RegionDefaults.outputAftershockRatePath), eventStartTime);
		/**
		 * 
		 * this sets the forecast start time
		 * 
		 */
		if(forecastTime == null){
			if(RegionDefaults.startForecastAtCurrentTime){
				currentTime = getCurrentGregorianTime();
			}else{
				currentTime = RegionDefaults.forecastStartTime;
			}
			/***********************
			 *TODO test only, remove!!!		
			 */	
			//setTestTime(eventStartTime);
			/***********************
			 *test end	
			 */
		}else{
			currentTime = forecastTime;
		}
		//if()
		log("eventStartTime " + (eventStartTime != null? RegionDefaults.dateformater.format(eventStartTime.getTime()):"null"));
		log("currentTime " + RegionDefaults.dateformater.format(currentTime.getTime()));
		/***************
		 * testing end >>>>>> 
		 */

		/**
		 * 1. Now obtain all events that have occurred since the last time the code
		 *    was run:
		 * NewObsEqkRuptureList
		 */		
		ObsEqkRupList newObsEqkRuptureList = this.loadNewEvents();
		if(newObsEqkRuptureList == null){
			return;
		}

		/**
		 * 2. load background rates/grid list
		 * BackgroundRatesList
		 * 
		 * this list is different from <code>bgGrid.getMagDistList();</code>
		 * with the values init to 0 ????
		 * which is used for later forcast
		 */    
		ArrayList<HypoMagFreqDistAtLoc> hypList = loadBgGrid();			

		/**
		 * 3. now loop over all new events and assign them as an aftershock to
		 *    a previous event if appropriate (loop thru all existing mainshocks)
		 */
		processAfterShocks(currentTime, newObsEqkRuptureList);

		//re check aftershock zone for sausage type zones
		checkTypeIIAftershockZones();//TODO this replaces code in UpdateSTEP_Forecast

		/**
		 * 4. Next loop over the list of all forecast model objects and create
		 *    a forecast for each object
		 */
		processForcasts(hypList );  


		/**
		 * 5. results output
		 */
		saveRatesFile(bgGrid);//rates for bgGrid only
		
		if(RegionDefaults.useCoulomb){
			ApplyCoulombFilter cf_filter = new ApplyCoulombFilter();
			cf_filter.applyFilter(bgGrid, updateModel,STEP_AftershockForecastList);
		}
		
		createStepSources(hypList);//add to sourceList
		//not sure what this file is for, commented for csep, TODO check!
		//saveRatesFile(sourceList,RegionDefaults.outputSTEP_Rates );
		/**
		 * now remove all model elements that are newer than
		 * 7 days (or whatever is defined in RegionDefaults)
		 * -OR- did not produce rates higher than the background
		 * level anywhere.
		 */
		// PurgeMainshockList.removeModels(STEP_AftershockForecastList);
		//createRateFile(sourceList);
		if(RegionDefaults.SAVE_FORECAST_TIME){
			saveForecastTime();
		}
	}

	private void saveForecastTime() {
		//save forecast time
		//File forecastTimeFile = new File(RegionDefaults.forecastStartTimeFile);
		FileWriter forecastTimeFileWriter;
		try {
			File file = new File(RegionDefaults.forecastStartTimeFile);
			if(file.exists()){
				file.delete();
			}
			forecastTimeFileWriter = new FileWriter(RegionDefaults.forecastStartTimeFile);		
			forecastTimeFileWriter.write(RegionDefaults.dateformater.format(currentTime.getTime()));//minLon
			forecastTimeFileWriter.close();		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public ObsEqkRupList loadNewEvents() {
		ObsEqkRupList newObsEqkRuptureList = null;
		if(RegionDefaults.EVENT_DATA_SOURCE == RegionDefaults.EVENT_SOURCE_GEONET){
			newObsEqkRuptureList = loadGeoNetEvents(this.eventStartTime, this.currentTime);
		}else if((RegionDefaults.EVENT_DATA_SOURCE == RegionDefaults.EVENT_SOURCE_FILE)
				||(RegionDefaults.EVENT_DATA_SOURCE == RegionDefaults.EVENT_SOURCE_CHINA)
				||(RegionDefaults.EVENT_DATA_SOURCE == RegionDefaults.EVENT_SOURCE_CSEP)){
			newObsEqkRuptureList = loadEventsFromFile(this.eventStartTime, 
					this.currentTime,RegionDefaults.EVENT_DATA_SOURCE); 
		}else{
			logger.error("please specify quake data source!!");
			//System.exit(1);
		}
		return newObsEqkRuptureList;
	}

	/**
	 * load quake events from GeoNet quakeML client
	 * @param start
	 * @param end
	 * @return
	 */
	public ObsEqkRupList loadGeoNetEvents(Calendar start, Calendar end) {
		QuakeMLClient quakemlClient = new QuakeMLClient();
		return quakemlClient.retrieveEvents(start, end);
		//return null;
	}


	private void setTestTime(GregorianCalendar startTime) {
		//* <<<<<< for testing only, make the current time being the main shock time
		// -- Sat May 30 13:25:03 NZST 2009
		//eventTime=2009,2009,4,30,13,25,03	
		if(startTime == null){
			logger.error("setTestTime, event startTime not specified");
			System.exit(1);
		}

		// * 
		//test 1,3,7,30,100,300 days after main shock
		int numRun = 3; //1 minute, 1 hour, 1,,3,7,30,100,300 days after main shock

		//currentTime = new GregorianCalendar(2009, 4, 30, 13, 25, 0);	
		//shift current time to n*4 hours after main shock		
		currentTime.setTimeInMillis(startTime.getTimeInMillis() + numRun*24*60*60*1000l);
		//log("currentTime " + currentTime.getTime());
		//change output file accordingly, outputAftershockRatePath =  INPUT_DIR + "/TimeDepRates.txt";
		if(numRun < 1000){
			RegionDefaults.outputAftershockRatePath = RegionDefaults.OUTPUT_DIR + "/TimeDepRates" + numRun + "d.txt";
		}

	}


	public void checkBgGrid(){
		LocationList bgLocList = bgGrid.getRegion().getNodeList();
		int bgRegionSize = bgLocList.size();
		for(int k=0;k<bgRegionSize;++k){
			Location bgLoc = bgLocList.getLocationAt(k);
			HypoMagFreqDistAtLoc bgDistAtLoc = bgGrid.getHypoMagFreqDistAtLoc(k); 
			double bgSumOver5 = bgDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag);
			log(">>>>>>bgLoc=" + bgLoc + " bgSumOver5=" +  bgSumOver5);}
	}

	/**
	 * update aftershock zone for the sausage models, re check aftershocks for all
	 * STEP_CombineForecastModels in the STEP_AftershockForecastList
	 */
	public void checkTypeIIAftershockZones() {
		//System.out.println("number of main shock="+numMainshocks);
		int numMainshocks = STEP_AftershockForecastList.size();
		ArrayList <STEP_CombineForecastModels> allSausageModels = new ArrayList <STEP_CombineForecastModels> ();
		//1. reprocess type II aftershockZones
		for (int msLoop = 0; msLoop < numMainshocks; ++msLoop) {
			STEP_CombineForecastModels model = (STEP_CombineForecastModels)STEP_AftershockForecastList.get(msLoop);	
			model.setGridIsUpdated(false);//reset to false
			if(!model.isUseSausageRegion()){//update only those not already sausage zone
				if(model.updateAftershockZone()){//update the aftershock zone
					//model.setGridDistanceFromFault(gridDist);
					//NEED TO UPDATE THE GRID DISTANCES AND SIZE HERE!!!!
					//if(model.isUseSausageRegion()){
					//recheck aftershocks
					ObsEqkRupList newAfterShocks = new ObsEqkRupList();
					for(int index = 0; index < model.getAfterShocks().size(); index++){						
						ObsEqkRupture shock = model.getAfterShocks().getObsEqkRuptureAt(index);
						//IsAftershockToMainshock_Calc seeIfAftershock =
						//	new IsAftershockToMainshock_Calc(shock, model);	
						//check all the aftershocks and keep only those still in the aftershock zone.
						if (IsAftershockToMainshock_Calc.calc_IsAftershockToMainshock(model, shock)) {					
							newAfterShocks.addObsEqkEvent(shock);
						}else{
							//TODO how to treat those no longer in the aftershock zone, should they be added to 
							// STEP_AftershockForecastList and accept aftershocks again??? yes create a new model? 
							// but  could it be aftershock of another model?? 
						}
					}
					log("1 reprocessAftershockZone existing model.getAfterShocks(). " + model.getAfterShocks().size() );
					model.getAfterShocks().clear();
					model.setAfterShocks(newAfterShocks);
					log("2 reprocessAftershockZone after model.getAfterShocks() " + model.getAfterShocks().size() );
					allSausageModels.add(model);
				}
			}
		}
		//2. re check other models to see if they fall into the aftershock zone of the sausage models
		for(STEP_CombineForecastModels sausageModel : allSausageModels){
			log("1  existing aftershocks " + sausageModel.getAfterShocks().size());
			numMainshocks = STEP_AftershockForecastList.size();
			log("-- numMainshocks " + numMainshocks);
			int numNew = 0;
			loop2:for (int msLoop = 0; msLoop < numMainshocks; ++msLoop) {
				STEP_CombineForecastModels mainModel = (STEP_CombineForecastModels)STEP_AftershockForecastList.get(msLoop);	
				ObsEqkRupture shock = mainModel.getMainShock();	
				//log("-- mainModel.getAfterShocks() " + mainModel.getAfterShocks().size());
				if(isObsEqkRupEventEqual(shock, sausageModel.getMainShock())){//same shock
					continue loop2;
				}				
				//check the shock not yet in the aftershock list
				if(sausageModel.getAfterShocks().getIndex(shock) >= 0){
					//log("shock already in the aftershock list ");
					continue loop2;
				}
				//check the main shock and all its aftershocks and add to the sausage model
				if (IsAftershockToMainshock_Calc.calc_IsAftershockToMainshock(sausageModel,shock)) {				
					sausageModel.addToAftershockList(shock);					
					for(int index = 0; index < mainModel.getAfterShocks().size(); index++){	
						ObsEqkRupture afterShock = mainModel.getAfterShocks().getObsEqkRuptureAt(index);
						//should the aftershocks be checked to seeIfAftershock as well?.-- set a switch
						if(recheckAftershocks4sausageModel){
							if (IsAftershockToMainshock_Calc.calc_IsAftershockToMainshock(sausageModel,afterShock)) {	
								sausageModel.addToAftershockList( afterShock);	
							}
						}else{
							//log("---  added aftershocks " + sausageModel.getAfterShocks().size());
							sausageModel.addToAftershockList( afterShock);	
						}
					}
					mainModel.set_isStatic(true);
					numNew++;
					//log("add to sausage..." + mainModel.getMainShock().getMag() + "aftershocks " + mainModel.getAfterShocks().size());
					//TODO should this be removed from the STEP_AftershockForecastList???? no
				}				
			}
			log("2 new added to sausage zone " + numNew + " new aftershocks " + sausageModel.getAfterShocks().size());
		}		
	}

	/**
	 * @param hypList
	 */
	public void processForcasts(ArrayList<HypoMagFreqDistAtLoc> hypList) {	

		int numAftershockModels = STEP_AftershockForecastList.size();

		log("processForcasts0 numAftershockModels  " + numAftershockModels);
		//log(  " loc bgSumOver5   seqSumOver5 " );
		//TODO remove debug
		// checkBgGrid();

		STEP_CombineForecastModels forecastModel;
		// StringBuffer bf = new StringBuffer();
		synchronized(bgGrid) {//lock bgGrid
			for (int modelLoop = 0; modelLoop < numAftershockModels; ++modelLoop){
				forecastModel = (STEP_CombineForecastModels)STEP_AftershockForecastList.get(modelLoop);
				if(forecastModel.getMainShock().getMag() < 7){//TODO debug only remove
					//continue;
				}	
				forecastModel.set_UsedInForecast(false);

				//log("getAfterShockZone " + forecastModel.getAfterShockZone() + " mainshock " +  + forecastModel.getMainShock().getMag()   + " getAfterShocks " + forecastModel.getAfterShocks().size()  );
				// update the combined model
				updateModel = new UpdateSTEP_Forecast(forecastModel);
				updateModel.updateAIC_CombinedModelForecast(); 

				/**
				 * after the forecasts have been made, compare the forecast to
				 *  the background at each location and keep whichever total 
				 *  is higher
				 */

				Location bgLoc, seqLoc;
				HypoMagFreqDistAtLoc seqDistAtLoc,bgDistAtLoc;
				//IncrementalMagFreqDist seqDist, bgDist;
				double bgSumOver5, seqSumOver5;

				LocationList bgLocList = bgGrid.getRegion().getNodeList();
				int bgRegionSize = bgLocList.size();
				LocationList aftershockZoneList = forecastModel.getAfterShockZone().getNodeList();
				int asZoneSize = aftershockZoneList.size();
				//TODO comment debug print
				//log("--mainshock mag " + forecastModel.getMainShock().getMag() + " centre " + forecastModel.getMainShock().getHypocenterLocation() + " getAfterShocks " + forecastModel.getAfterShocks().size()  + " getAfterShockZone " + forecastModel.getAfterShockZone().getName()+ " asZoneSize " + asZoneSize);
				//				
				double t_seqSumOver4 = 0;
				if (forecastModel.getMainShock().getMag() > 6.0){//TODO check this block is useful?? for debug
					//log("  -->   asZoneSize " + asZoneSize);
					HypoMagFreqDistAtLoc t_seqDistAtLoc;    	  
					for (int as = 0; as < asZoneSize; ++as){
						Location loc = aftershockZoneList.getLocationAt(as);						
						//log((Math.round(100*loc.getLatitude())/100d) + "," + (Math.round(100*loc.getLongitude())/100d));
						t_seqDistAtLoc = forecastModel.getHypoMagFreqDistAtLoc(as);
						//log("aftershockZone " + aftershockZoneList.getLocationAt(as));
						if(t_seqDistAtLoc != null)
							t_seqSumOver4 += t_seqDistAtLoc.getFirstMagFreqDist().getCumRate(0);
						//System.out.println(t_seqDistAtLoc.getFirstMagFreqDist().getCumRate(0));

						//loc = forecastModel.getMainShock().getHypocenterLocation();
						//log("main shock " + (Math.round(100*loc.getLatitude())/100d) + "," + (Math.round(100*loc.getLongitude())/100d));
					}
					log("Total Forecast " + t_seqSumOver4); 
				}

				//log("  -- asZoneSize " + asZoneSize + " getZoneRadius=" + forecastModel.getZoneRadius());
				/**
				 * make sure the locations involved equal to each other
				 */				

				for(int k=0;k<bgRegionSize;++k){					
					bgLoc = bgLocList.getLocationAt(k);
					//log("bgLoc=" + bgLoc);
					// ListIterator seqIt = forecastModel.getAfterShockZone().getGridLocationsIterator();
					for(int g=0;g < asZoneSize;++g){
						seqLoc = aftershockZoneList.getLocationAt(g);						
						if (seqLoc != null){
							if (bgGrid.checkLocaionEquals(bgLoc, seqLoc, RegionDefaults.gridPrecision)){//location check 1
								//log(">>>> bgLoc == seqLoc");
								seqDistAtLoc = forecastModel.getHypoMagFreqDistAtLoc(g);
								bgDistAtLoc = bgGrid.getHypoMagFreqDistAtLoc(k); 
								//if(bgDistAtLoc == null ) 
								//	log(">>>> bgDistAtLoc " + bgDistAtLoc);
								bgSumOver5 = bgDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag);
								seqSumOver5 = seqDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag);;

								//log( seqLoc +  ", " + bgSumOver5 +	 " , " + seqSumOver5 );	

								if (seqSumOver5 > bgSumOver5) {

									HypoMagFreqDistAtLoc hypoMagDistAtLoc= hypList.get(k); 
									Location loc= hypoMagDistAtLoc.getLocation();

									hypList.set(k, new HypoMagFreqDistAtLoc(seqDistAtLoc.getFirstMagFreqDist(),loc));
									bgGrid.setMagFreqDistAtLoc(seqDistAtLoc.getFirstMagFreqDist(),k); 
									//log(">>>> setMagFreqDist loc " + loc + " rate=" + seqDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag));
									// record the index of this aftershock sequence in an array in
									// the background so we know to save the sequence (or should it just be archived somehow now?)
									bgGrid.setSeqIndAtNode(k,modelLoop);
									// The above may not be necessary here I set a flag
									// to true that the model has been used in a forecast
									forecastModel.set_UsedInForecast(true);
								}
							}
						}
					}
				}
				//TODO debug, remove
				//log("models used in forcast ");
				if(forecastModel.get_UsedInForecast()){
					//log( forecastModel.getMainShock().getMag() + " centre " + forecastModel.getMainShock().getHypocenterLocation() + " getAfterShocks " + forecastModel.getAfterShocks().size()  + " getAfterShockZone " + forecastModel.getAfterShockZone().getName()+ " asZoneSize " + asZoneSize);

				}

			}//end of ModelLoop			
		}
		double total_combined = 0;
		HypoMagFreqDistAtLoc tcom_SeqDistLoc;    	  
		for(int gg = 0; gg<bgGrid.getNumHypoLocs(); ++gg){
			tcom_SeqDistLoc = bgGrid.getHypoMagFreqDistAtLoc(gg);
			if(tcom_SeqDistLoc != null)
				total_combined = total_combined+tcom_SeqDistLoc.getFirstMagFreqDist().getCumRate(0);
		}
		log("Total Combine Forecast = "+total_combined);
		//log(bf.toString());
	}


	/**
	 * @param currtTime--current time
	 * @param newObsEqkRuptureList--new eq events
	 * @return
	 */
	public int processAfterShocks(GregorianCalendar currtTime, ObsEqkRupList newObsEqkRuptureList) {
		ObsEqkRupture newEvent, mainshock;
		STEP_CombineForecastModels mainshockModel, foundMsModel = null, staticModel;
		ListIterator newIt = newObsEqkRuptureList.listIterator();
		log("new events size " + newObsEqkRuptureList.size());
		boolean isAftershock = false;
		//int maxMagInd = -1;
		int numMainshocks = STEP_AftershockForecastList.size();	    
		log("start numMainshocks  " + numMainshocks);	    
		double maxMag = 0, msMag, newMag ;
		//load external faults from file
		Map<String,FaultTrace> externalFaults = loadExternalFaultModelFromFile(RegionDefaults.EXTERNAL_FAULT_MODEL_SOURCE_FILE);

		synchronized(STEP_AftershockForecastList) {//lock STEP_AftershockForecastList
			int numBigEvent =0, numSameEvent = 0;
			boolean faultTraceFound = false;
			// loop over new events
			loop1: while (newIt.hasNext()) {
				newEvent = (ObsEqkRupture) newIt.next();
				newMag = newEvent.getMag();	
				//check event is inside background region
				if(!this.bgGrid.getRegion().contains(newEvent.getHypocenterLocation())){
				//	logger.info("event outside bg region " + newEvent.getHypocenterLocation());
					continue loop1;
				}
				//logger.info("event " + newEvent.getMag() + " " + RegionDefaults.dateformater.format(newEvent.getOriginTime().getTime()));
				//
				isAftershock = false;
				//System.out.println("number of main shock="+numMainshocks);
				numMainshocks = STEP_AftershockForecastList.size();

				int maxMagInd = -1; //!!! set to init value each round
				maxMag = 0;   //!!! reset
				//loop over existing mainshocks
				loop2: for (int msLoop = 0; msLoop < numMainshocks; ++msLoop) {
					mainshockModel = (STEP_CombineForecastModels)STEP_AftershockForecastList.get(msLoop);	
					mainshock = mainshockModel.getMainShock();


					//see if the event already in the list ??????
					if(isObsEqkRupEventEqual(mainshock, newEvent)){//this event is already in the list
						//log (">>> same event" + newEvent.getMag());
						numSameEvent++;
						continue loop1;
					}	
					msMag = mainshock.getMag();

					// update the current time (as defined at the start of STEP_Main)
					// in this mainshock while we're at it.
					mainshockModel.updateCurrentTime(currtTime);					
					//
					// returns boolean if event is in aftershockzone, but does not set anything
					//IsAftershockToMainshock_Calc seeIfAftershock =
					//	new IsAftershockToMainshock_Calc(newEvent, mainshockModel);	
					if (IsAftershockToMainshock_Calc.calc_IsAftershockToMainshock(mainshockModel, newEvent)) {						
						// if the new event is larger than the mainshock, make the mainshock
						// static so that it will no longer accept aftershocks.
						if (newMag >= msMag) {
							mainshockModel.set_isStatic(true);
						}

						/**
						 * to be a mainshock an event must be most recent and largest "mainshock"
						 * with this new event as an aftershock.
						 * Check to see if this mainshock is the largest mainshock for this event
						 * (it will be the newest as the ms are in chrono order) if it is, keep
						 * the index for the mainshock so we can add the aftershock later.
						 * Also any older mainshock that had this new event as an aftershock
						 * should be set to static (as the aftershock zones apparently overlap)
						 */
						if (msMag > maxMag) {
							if (maxMagInd > -1){
								/***
								 * getting the mafterShocks inshock index which had
								 * maximum magnitude upto this point, setting that static 
								 * as it no longer has maximum magnitude. 
								 */
								//   staticModel =
								//(STEP_CombineForecastModels)STEP_AftershockForecastList.get(msLoop);
								staticModel = (STEP_CombineForecastModels)STEP_AftershockForecastList.get(maxMagInd);
								staticModel.set_isStatic(true);//the previous maxMag
							}
							// set the index and mag of the new ms so it can be compared against
							// Correct?!?!
							maxMagInd = msLoop;
							maxMag = msMag;
						}
					}
				}//end of loop 2--mainshocks

				// now add the new event to the aftershock list of the largest appropriate
				// mainshock - if one has been found
				if (maxMagInd > -1) {
					foundMsModel = (STEP_CombineForecastModels)STEP_AftershockForecastList.get(maxMagInd);
					foundMsModel.addToAftershockList(newEvent);
					//added as  aftershock to a main shock.
					isAftershock = true;
				}				

				// add the new event to the list of mainshocks if it is greater than
				// magnitude 3.0 (or what ever mag is defined)
				if (newMag >= RegionDefaults.minMagForMainshock) {
					//System.out.println("Creating new main shock model");
					numBigEvent++;

					STEP_CombineForecastModels newForecastMod = new STEP_CombineForecastModels(newEvent,bgGrid,currtTime);		

					// if the new event is already an aftershock to something else
					// set it as a secondary event.  Default is true
					if (isAftershock) {
						newForecastMod.set_isPrimary(false);
					}	
					//System.out.println(newEvent.getMag());					
					//System.out.println(+newForecastMod.getMainShock().getMag()+" minCoulombMS_Mag"+RegionDefaults.minCouloumbMS_Mag);
					// if there is an external fault model available
					// and the mag is greater than the min mag for this, set the boolean to true
					if (RegionDefaults.hasExternalFaultModel){
						//if(newForecastMod.getMainShock().getMag() >= RegionDefaults.minCoulombMS_Mag){
						FaultTrace faultTrace = externalFaults.get(newForecastMod.getMainShock().getEventId());
                        if(faultTrace != null) {
                            log("# eventsForExternalFaultsModel " + newForecastMod.getMainShock().getEventId());
							newForecastMod.set_FaultSurface(faultTrace);
							newForecastMod.setHasExternalFaultModel(true);
							faultTraceFound = true;
						}
					}

					// add the new event to the list of mainshocks and increment the number
					// of total mainshocks (for the loop)				
					STEP_AftershockForecastList.add(newForecastMod);
					++numMainshocks;
				}
			}//end of loop1 -- new events		
			numMainshocks = STEP_AftershockForecastList.size();	    
			log("end numMainshocks  " + numMainshocks);

			//error message if faultTrace not found
			if (!faultTraceFound && RegionDefaults.hasExternalFaultModel) {
				logger.error("## externalFault trace not found!!");
			}
		}

		return numMainshocks;
	}


	public ArrayList<HypoMagFreqDistAtLoc>  loadBgGrid() {
		bgGrid = new BackGroundRatesGrid(bgRatesFilePath );	
		return  initStepHypoMagFreqDistForBGGrid(bgGrid);

	}


	public static GregorianCalendar getCurrentGregorianTime() {
		GregorianCalendar curTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));		
		curTime.set(GregorianCalendar.MILLISECOND, 0);
		// log("year " +  year + "," + month + "," + day + "," + hour24 + "," + min + "," + sec);
		return  curTime;//new GregorianCalendar(year, month, day, hour24, min, sec);

	}	


	public boolean isScheduledOperation() {
		return scheduledOperation;
	}


	public void setScheduledOperation(boolean scheduledOperation) {
		this.scheduledOperation = scheduledOperation;
	}


	/**
	 * load quake events from data file
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public ObsEqkRupList  loadEventsFromFile(GregorianCalendar start, GregorianCalendar end, int format) {
		CubeToObsEqkRupture getNewEvents = null;
		try {
			//logger.info("loadEventsFromFile:  " + eventsFilePath );
			logger.info("loadEventsFromFile:  " + new File(eventsFilePath).getName() + " datasource " + RegionDefaults.EVENT_DATA_SOURCE );
			if(RegionDefaults.EVENT_DATA_SOURCE == RegionDefaults.EVENT_SOURCE_FILE){
				getNewEvents = new CubeToObsEqkRupture(eventsFilePath,start, end );
			}else if((RegionDefaults.EVENT_DATA_SOURCE == RegionDefaults.EVENT_SOURCE_CHINA)){
				getNewEvents = new CubeToObsEqkRuptureChinaFormat(eventsFilePath,start, end );
				//EVENT_DATA_SOURCE = EVENT_SOURCE_CSEP;
			}else if((RegionDefaults.EVENT_DATA_SOURCE == RegionDefaults.EVENT_SOURCE_CSEP)){
				getNewEvents = new CubeToObsEqkRuptureCsepFormat(eventsFilePath,start, end );
			}			
			logger.info("getNewEvents  " + getNewEvents.getAllObsEqkRupEvents().size() );
			return  getNewEvents.getAllObsEqkRupEvents(); 
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;	
	}

	/**
	 * load external faults model from file
	 *
	 * @return a map of FaultTrace keyed by eventId
	 */
	public Map<String,FaultTrace> loadExternalFaultModelFromFile(String filePath) {
		Map<String,FaultTrace> faultTraces = new HashMap<String, FaultTrace>();
		log("# loadExternalFaultModelFromFile filePath " + filePath);
		try {
			ArrayList fileLines = FileUtils.loadFile(filePath);
			log("# loadExternalFaultModelFromFile fileLines " + fileLines.size());
			for (int i = 0; i <  fileLines.size(); i++) {
				String line = (String) fileLines.get(i);
				if(line.startsWith("#")) {
					continue; //header line
				}
				FaultTrace fault = makeFaultTrace(line);
				if(fault != null) {
					faultTraces.put(fault.getName(), fault);
				}
			}
		} catch (IOException e) {
			logger.error("error loading ExternalFaultModelFromFile ", e);
		}
		log("# loadExternalFaultModelFromFile " + faultTraces.size());
		return faultTraces;
	}

	/**
	 * create a FaultTrace defined by a line in the external_fault_model file
	 * eventId lon1 lat1 lon2 lat2 lon3 lat3
	 * @param line
	 * @return
	 */
	private FaultTrace makeFaultTrace(String line) {
		String [] modelParams = line.split(RegionDefaults.PATTERN_SPACE);
		if(modelParams.length == 7) {
			String evenId = modelParams[0];
			String lon1 = modelParams[1];
			String lat1 = modelParams[2];
			String lon2 = modelParams[3];
			String lat2 = modelParams[4];
			String lon3 = modelParams[5];
			String lat3 = modelParams[6];
			FaultTrace fault_trace = new FaultTrace(evenId);
			fault_trace.addLocation((new Location(Double.parseDouble(lat1), Double.parseDouble(lon1), 0)));
			fault_trace.addLocation((new Location(Double.parseDouble(lat2), Double.parseDouble(lon2), 0)));
			fault_trace.addLocation((new Location(Double.parseDouble(lat3), Double.parseDouble(lon3), 0)));
			return fault_trace;
		}
		return null;
	}


	private void saveRatesFile(ArrayList<PointEqkSource> sourcelist, String outputFile){
		File existingFile = new File(outputFile);
		if(this.isScheduledOperation() && existingFile.exists()){
			backupFile(existingFile, this.lastCurrTime);
		}
		int size = sourcelist.size();
		FileWriter fw = null;
		log("saveRatesFile " + existingFile.getAbsolutePath());
		try {
			fw = new FileWriter(outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log("NumSources = "+size);
		for(PointEqkSource source:sourcelist){
			//PointEqkSource source = sourcelist.get(i);
			double locRatesSum = 0;
			Location loc = source.getLocation();
			int numRuptures = source.getNumRuptures();
			for(int j=0;j<numRuptures;++j){
				ProbEqkRupture rupture = source.getRupture(j);
				double prob = rupture.getProbability();
				double rate = -Math.log(1-prob);
				locRatesSum += rate;
			}
			try {
				fw.write(locFormat.format(loc.getLatitude())+"  "+ locFormat.format(loc.getLongitude()) + "  " + locRatesSum + "\n");
				//fw.write(loc.toString()+"   "+ rate+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * backupfile before it is overwritten
	 * @param existingFile
	 * @param fileTime
	 */
	public  void backupFile(File existingFile, GregorianCalendar fileTime) {
		SimpleDateFormat timeformater = new SimpleDateFormat("yyyy_MM_dd'T'HH_mm_ss");
		String filename = existingFile.getName();
		filename = filename.replace(".", timeformater.format(fileTime.getTime()) + ".");
		File desFile = new File( existingFile.getParentFile().getAbsolutePath() + "/"+ filename);
		//boolean sc = existingFile.renameTo(desFile);
		InputStream in;
		try {
			in = new FileInputStream(existingFile);
			OutputStream out = new FileOutputStream(desFile);
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			logger.info("backup File " + desFile.getAbsolutePath() );
		} catch (FileNotFoundException e) {
			//logger.error(e);
		} catch (IOException e) {
			//logger.error(e);
		}	
		//logger.info("backup File " + desFile.getAbsolutePath() + " success?" +sc);
	}


	/**
	 * convert  hypoMagDist list to PointEqkSource list
	 * @param hypoMagDist
	 */
	public void createStepSources(ArrayList<HypoMagFreqDistAtLoc> hypoMagDist){
		System.out.println("Creating STEP sources");
		if(sourceList == null){
			sourceList = new ArrayList<PointEqkSource>();
		}else{
			sourceList.clear();
		}
		int size = hypoMagDist.size();
		log("NumSources in hypList = "+size);
		int nullSource = 0, zeroSource=0;
		synchronized(sourceList){
			for(HypoMagFreqDistAtLoc hypoLocMagDist : hypoMagDist){
				//HypoMagFreqDistAtLoc hypoLocMagDist = hypoMagDist.get(i);
				//log("hypoLocMagDist " + hypoLocMagDist);
				if(hypoLocMagDist == null) {//TODO check!!, no bgGrid value since its not in the bgGrid file
					nullSource++;
					continue;
				}
				Location loc = hypoLocMagDist.getLocation();
				IncrementalMagFreqDist magDist = hypoLocMagDist.getFirstMagFreqDist();
				double rate = magDist.getY(0);
				//log("rate " + rate);
				if(rate !=0){
					//System.out.println("Writing out sources with rates not zero");
					PointEqkSource source = new PointEqkSource(loc,magDist,
							RegionDefaults.forecastLengthDays,RegionDefaults.RAKE,
							RegionDefaults.DIP,RegionDefaults.minForecastMag);
					//log("source loc=" + source.getLocation());
					sourceList.add(source);     
				}else{
					zeroSource++;
				}
			}
		}
		//log("sourceList >>" + sourceList.size() + " zeroSource " + zeroSource + " nullSource " + nullSource);
	}

	/**
	 * init all HypForecastMagDist value to 0 in stepHypForecastList
	 * @param bgGrid -- backgroud rates
	 * @return
	 */
	private ArrayList<HypoMagFreqDistAtLoc>  initStepHypoMagFreqDistForBGGrid(BackGroundRatesGrid bgGrid){
		ArrayList<HypoMagFreqDistAtLoc> hypForecastList = bgGrid.getHypoMagFreqDist(); //this has been read from the gris file
		ArrayList<HypoMagFreqDistAtLoc>  stepHypForecastList = new ArrayList<HypoMagFreqDistAtLoc> ();
		int size = hypForecastList.size();
		//System.out.println("Size of BG magDist list = "+size);
		for(HypoMagFreqDistAtLoc hypForcast: hypForecastList ){
			//HypoMagFreqDistAtLoc hypForcast = hypForecastList.get(i);
			HypoMagFreqDistAtLoc newHypoMagFreqDistAtLoc = null;
			//log("hypForcast=" + hypForcast);
			IncrementalMagFreqDist hypForecastMagDist = null;
			if(hypForcast != null){
				Location loc = hypForcast.getLocation();
				//log("hypForcast loc" + loc);
				IncrementalMagFreqDist magDist = hypForcast.getFirstMagFreqDist();
				hypForecastMagDist = new IncrementalMagFreqDist(magDist.getMinX(),
						magDist.getNum(),magDist.getDelta());

				for(int j=0;j<hypForecastMagDist.getNum();++j)
					hypForecastMagDist.set(j, 0.0);
				newHypoMagFreqDistAtLoc = new HypoMagFreqDistAtLoc(hypForecastMagDist,loc);
			}else{
				//log("hypForcast == null"   );
			}
			stepHypForecastList.add(newHypoMagFreqDistAtLoc );
		}
		return stepHypForecastList;
	}

	/**
	 * write the HypoMagFreqDistAtLoc from bggrid to file
	 * this is those read from the input file: (NZdailyRates.txt)
	 * whats the output file used for???
	 * 
	 * @param bggrid
	 */
	private void saveRatesFile(BackGroundRatesGrid bggrid){
		Location bgLoc;
		HypoMagFreqDistAtLoc bgDistAtLoc;
		double bgSumOver5;

		try{
			File existingFile = new File(RegionDefaults.outputAftershockRatePath);
			if(this.isScheduledOperation() && existingFile.exists()){
				//pick up the time the existing file is saved
				lastCurrTime = new GregorianCalendar();
				lastCurrTime.setTimeInMillis(existingFile.lastModified());
				backupFile(existingFile, this.lastCurrTime);
			}

			FileWriter fr = new FileWriter(RegionDefaults.outputAftershockRatePath);
			LocationList bgLocList = bggrid.getRegion().getNodeList();			
			int bgRegionSize = bgLocList.size();
			log("saveRatesFile: " +   new File(RegionDefaults.outputAftershockRatePath).getAbsolutePath() );//+"  bgRegionSize " + bgRegionSize);
			//int hypoMagFreqDistSize = bggrid.getHypoMagFreqDist().size();
			//log("hypoMagFreqDistSize " + hypoMagFreqDistSize);
			//SitesInGriddedRectangularRegion region = (SitesInGriddedRectangularRegion)bgGrid.getEvenlyGriddedGeographicRegion();
			for(int k=0;k <  bgRegionSize ;++k){
				bgLoc = bgLocList.getLocationAt(k);
				//get the hypoMag for the location				
				bgDistAtLoc = bggrid.getHypoMagFreqDistAtLoc(k); //k
				//bgDistAtLoc = bgGrid.getHypoMagFreqDistAtLoc(index);
				//log("bgLoc=" + bgLoc + " bgDistAtLoc=" + bgDistAtLoc.getLocation());
				if(bgDistAtLoc != null){	
					//if(RegionDefaults.MODEL_FORMAT == RegionDefaults.MODEL_FORMAT_CSEP){
						int numForecastMags = 1 + (int) ( (RegionDefaults.maxForecastMag - RegionDefaults.minForecastMag) / RegionDefaults.deltaForecastMag);
						//log("numForecastMags=" + numForecastMags  );						
						for(int index = 0; index < numForecastMags; index++){
							double mag = RegionDefaults.minForecastMag  + index*RegionDefaults.deltaForecastMag;
							//log("1 mag=" + mag  );
							//double rate = bgDistAtLoc.getFirstMagFreqDist().getCumRate(mag);
							double rate = bgDistAtLoc.getFirstMagFreqDist().getIncrRate(mag);
							// make sure there are no ZERO rate bins
							if(rate == 0) rate = 1E-11;
							//if(rate > 0) log("2 mag=" + mag + " rate=" + rate);
							fr.write(locFormat.format(bgLoc.getLongitude() - RegionDefaults.gridSpacing/2)//minLon
									+ "    " + locFormat.format(bgLoc.getLongitude() + RegionDefaults.gridSpacing/2) //maxLon 
									+ "    " + locFormat.format(bgLoc.getLatitude() - RegionDefaults.gridSpacing/2) //minLat
									+ "    " + locFormat.format(bgLoc.getLatitude() + RegionDefaults.gridSpacing/2)  //maxLat
									+ "    " +  RegionDefaults.MIN_Z //minZ
									+ "    " + RegionDefaults.MAX_Z  //maxZ							
									+ "    " + magFormat.format(mag - RegionDefaults.deltaForecastMag /2) //minMag
									+ "    " + magFormat.format(mag + RegionDefaults.deltaForecastMag /2)  //maxMag						
									+ "    " +  rate
									+ "     1\n");  // masking bit 1=valid forecast; 0=not valid 
						}
				//	}else{
					//	bgSumOver5 = bgDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minCompareMag); 
					//	fr.write(locFormat.format(bgLoc.getLatitude())+"    "+locFormat.format(bgLoc.getLongitude())+"      "+bgSumOver5+"\n");
					//}
				}
			}
			fr.close();			
					
		}catch(IOException ee){
			ee.printStackTrace();

		}    

	}


	public GregorianCalendar getLastCurrTime() {
		return lastCurrTime;
	}

	/**
	 * read the serialized step forcast objects back from file
	 * this object is written into file in the class:STEP_HazardDataSet
	 * to save and get objects in the format of serialized object 
	 * could cause exception when the classes change between subsequent runs
	 * and since the List STEP_CombineForecastModels stores all the events ever 
	 * in such case, we will need to run all the events to restore this List
	 * ??? is it better to save / read in plain file or database?
	 */
	public  synchronized void   readSTEP_AftershockForecastListFromFile( ){		
		//stepAftershockList
		STEP_AftershockForecastList = null;
		lastCurrTime = null;
		try{
			File sourceFile = new File(RegionDefaults.STEP_AftershockObjectFile);
			log("readSTEP_AftershockForecastListFromFile: " +   sourceFile.getAbsolutePath() );
			if(sourceFile.exists()){ 
				//logger.info("file last modified:" + new Date(sourceFile.lastModified()));
				STEP_AftershockForecastList = (ArrayList<STEP_CombineForecastModels>) FileUtils.loadObject(RegionDefaults.STEP_AftershockObjectFile);
				if(STEP_AftershockForecastList.size() > 0){
					STEP_CombineForecastModels model0 = STEP_AftershockForecastList.get(0);
					lastCurrTime = model0.getCurrentTime();
					logger.info("lastCurrTime " + localDateformater.format(lastCurrTime.getTime()));
				}
			}else{
				logger.error("error STEP_AftershockObjectFile not exist!!"  +  sourceFile.getAbsolutePath());
			}
			if(STEP_AftershockForecastList == null)
				STEP_AftershockForecastList =  new ArrayList <STEP_CombineForecastModels> ();
		} catch ( Exception ex) {
			//ex.printStackTrace();
			logger.error("readSTEP_AftershockForecastListFromFile error " + ex );
			//create an empty List
			STEP_AftershockForecastList =  new ArrayList <STEP_CombineForecastModels> ();
		}
		logger.info("readSTEP_AftershockForecastListFromFile size " + STEP_AftershockForecastList.size());
	}


	public String getEventsFilePath() {
		return eventsFilePath;
	}


	public void setEventsFilePath(String eventsFilePath) {
		this.eventsFilePath = eventsFilePath;
	}


	public String getBgRatesFilePath() {
		return bgRatesFilePath;
	}


	public void setBgRatesFilePath(String bgRatesFilePath) {
		this.bgRatesFilePath = bgRatesFilePath;
	}


	public BackGroundRatesGrid getBgGrid() {
		return bgGrid;
	}


	/**
	 * check two event is equal
	 * the equalsObsEqkRupEvent method in ObsEqkRupture class
	 * doesnt seem correct
	 * 
	 * @param obsRupEvent0
	 * @param obsRupEvent
	 * @return
	 */
	public boolean isObsEqkRupEventEqual(ObsEqkRupture obsRupEvent0, ObsEqkRupture obsRupEvent){
		obsRupEvent0.equalsObsEqkRupEvent(obsRupEvent);
		//if any of the condition is not true else return false

		if(!obsRupEvent0.getEventId().equals(obsRupEvent.getEventId() )||
				obsRupEvent0.getOriginTime().getTimeInMillis() != obsRupEvent.getOriginTime().getTimeInMillis() || //add time!!
				obsRupEvent0.getEventVersion() != obsRupEvent.getEventVersion() ||
				!obsRupEvent0.getMagType().equals(obsRupEvent.getMagType() )||
				//obsRupEvent0.getMagError() != obsRupEvent.getMagError()||
				//obsRupEvent0.getHypoLocHorzErr() != obsRupEvent.getHypoLocHorzErr()||
				//obsRupEvent0.getHypoLocVertErr() != obsRupEvent.getHypoLocVertErr()||		    
				obsRupEvent0.getMag() != obsRupEvent.getMag()){
			if(obsRupEvent0.getEventId().equals(obsRupEvent.getEventId() )){
				//logger.info(">> check 6");
			}
			return false;
		}
		//logger.info(">> check 7");
		return true;
	}




	public  GregorianCalendar getCurrentTime() {
		return currentTime;
	}


	public static  void log(String string) {
		logger.info(string);

	}


	/**
	 * save step models as serialized object
	 */
	public void saveModels() {
		//remove models in the uncertainty period
		if(this.isScheduledOperation()){
			PurgeMainshockList.removeModels(this.STEP_AftershockForecastList);
		}
		//ArrayList<STEP_CombineForecastModels> stepAftershockList= stepMain.getSTEP_AftershockForecastList();
		//saving the STEP_Aftershock list object to the file, why not do it in stepMain?
		logger.info("save STEP_AFTERSHOCK_OBJECT_FILE " + RegionDefaults.STEP_AftershockObjectFile );
		synchronized(this.getSTEP_AftershockForecastList()){
			FileUtils.saveObjectInFile(RegionDefaults.STEP_AftershockObjectFile, this.getSTEP_AftershockForecastList());
		}
	}

}

