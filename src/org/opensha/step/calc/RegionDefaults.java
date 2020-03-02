package org.opensha.step.calc;

import org.apache.log4j.Logger;
import org.opensha.commons.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
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
public class RegionDefaults {
	private static Logger logger = Logger.getLogger(RegionDefaults.class);
	
	public RegionDefaults() {
	}

	/**
	 * This class contains many of the variables that are specific
	 * to a region.  Default values are set. 
	 * 
	 */

	private static final String CONFIG_FILE = "config/defaults.properties";
	public static String INPUT_DIR = "data/mattg_test"; ///home/baishan/eclipse/workspace/opensha/trunk/data/mattg_test
	public static String OUTPUT_DIR = "output";
	//
	public static  int GENERIC_MODEL_TYPE = 0; //0==r&j, 1= new
	//test model, CSEP has special format requirement
	public static final int MODEL_FORMAT_CSEP = 1;
	public static final int MODEL_FORMAT_OTHER = 0;
	public static int MODEL_FORMAT = MODEL_FORMAT_OTHER;
	public static boolean PARAM_FILE_BY_COMMAND_ARGS = false;//for csep, specify param file in commandline args?
	
	public static int REGION_CF = 0;
	public static int REGION_NZ = 1;


	//input files
	public static String paramFilePath =  INPUT_DIR + "/CSEP_params.txt";//csep params
	public static String cubeFilePath =  INPUT_DIR + "/merge_NZ.nts"; //"/merge.nts", merge_synthNZ
	public static String backgroundHazardPath = INPUT_DIR +  "/NZDailyHaz_shifted.txt"; //"/STEP_NZHazProb.txt"; //STEP_NZHazProb.txt STEP_backGround
	public static String BACKGROUND_RATES_FILE_NAME =  INPUT_DIR +  "/NZZeroHaz05.dat.txt"; //"/NZdailyRates.txt"; //AllCal96ModelDaily.txt;//"org/opensha/sha/earthquake/rupForecastImpl/step/AllCal96ModelDaily.txt";
	//public static String backgroundHazardPath = INPUT_DIR +  "/NZZeroHaz05.dat";
	//public static String BACKGROUND_RATES_FILE_NAME =  INPUT_DIR +  "/NZZeroRate05.dat";
	//public static String backgroundHazardPath = INPUT_DIR +  "/STEP_NZHazProb.txt"; //STEP_NZHazProb.txt STEP_backGround
	//public static String BACKGROUND_RATES_FILE_NAME =  INPUT_DIR +  "/NZdailyRates.txt"; //AllCal96ModelDaily.txt;//"org/opensha/sha/earthquake/rupForecastImpl/step/AllCal96ModelDaily.txt";
	//STEP_source_Probs.txt---used to calc step probility directly from file instead of from quake events
	public static String STEP_SORCE_FILE = INPUT_DIR +  "/ChCh/Determin6.0.txt";
	
	//output files
	public static String outputHazardPath = OUTPUT_DIR + "/STEP_Probs.txt"; 
	public static String STEP_AftershockObjectFile = OUTPUT_DIR +  "/STEP_AftershockObj";
	public static String outputAftershockRatePath =  OUTPUT_DIR + "/TimeDepRates.txt";
	public static String forecastStartTimeFile = OUTPUT_DIR + "/forecastStartTime.txt"; 
	//this is for Damage States
	public static String outputHazCurvePath = OUTPUT_DIR + "/HazCurve_Probs.txt";
	//STEP_Rates
	public static String outputSTEP_Rates = OUTPUT_DIR + "/STEP_Rates.txt";

	public static double minMagForMainshock = 3.0;
	public static double minForecastMag = 4.0;
	public static double maxForecastMag = 8.0;
	public static double deltaForecastMag = 0.1;

	public static double forecastLengthDays = 1;
	public static boolean startForecastAtCurrentTime = true;
	public static GregorianCalendar forecastStartTime;  // set this if startForecastAtCurrentTime is False
	//main.shock.time=30/5/2009 13:25:00
	public static GregorianCalendar EVENT_START_TIME;
	//days before current time, from which periods events are subject to change
	public static int daysFromQDM_Cat = 7;

	//the minimum radius for aftertshock zone
	public static  double Min_Aftershock_R = 5;

	//California
	public final static double searchLatMin_CF = 32.0;
	public  final static double searchLatMax_CF = 42.2;
	public  final static double searchLongMin_CF = -124.6;
	public  final static double searchLongMax_CF = -112;
	//nz WGTN
	public final static double searchLatMin_NZ = -42.6;
	public  final static double searchLatMax_NZ = -39.6;
	public  final static double searchLongMin_NZ = 172.8;
	public  final static double searchLongMax_NZ = 175.8;
	// CHCH REGION
//	public final static double searchLatMin_NZ = -45.0;
//	public  final static double searchLatMax_NZ = -42.0;
//	public  final static double searchLongMin_NZ = 171.1;
//	public  final static double searchLongMax_NZ = 174.1; //-176
	
	//public final static double searchLatMin_NZ = -47.95;
	//public  final static double searchLatMax_NZ = -34.05;
	//public  final static double searchLongMin_NZ = 164.05;
	//public  final static double searchLongMax_NZ = 179.95; //-176

	public static double searchLatMin = searchLatMin_NZ;
	public static double searchLatMax = searchLatMax_NZ;
	public static double searchLongMin = searchLongMin_NZ;
	public static double searchLongMax = searchLongMax_NZ;	
	
	//public final static double SOURCE_MAX_DEPTH = 40.0;
	//public  final static double SOURCE_MIN_MAG = 2.5;

	public static double gridSpacing = 0.1;
	public static double gridPrecisionCF = 0.1;
	public static double gridPrecisionNZ = 0.01;
	public static double gridPrecision  = gridPrecisionNZ;
	public static double grid_anchor = 0.0; //0.05 for nz
	//min and max depth for CSEP output, z_min and z_max can be 0 and 20
	public static double MIN_Z = 0;
	public static double MAX_Z = 40;
	
	public static double addToMc = 0.05;

	// this is for defining the fault surface for the aftershock zone.
	// 2D for now so the values are the same.
	public static double lowerSeismoDepth = 10.0;
	public static double upperSeismoDepth = 10.0;

	public static boolean useFixed_cValue = true;

	// set the parameters for the AIC Calcs for the model elements
	public static int genNumFreeParams = 0;
	public static int seqNumFreeParams = 0;
	public static int spaNumFreeParams = 3;  // should be 4 if c is not fixed

	// the minimum mag to be used when comparing the cummulative of the 
	// background to that of an individual sequence
	public static int minCompareMag = 0;

	public static final double RAKE=100.0;
	public static final double DIP=90.0;
	
	// coulomb scaling parameters
	public static boolean useCoulomb=false; // whether or not the coulomb filter is applied
	public static double bluePercent=0.07;
	public static double redPercent=0.93;
	public static double coulombFaultRadius = 5.1; // distance in km from fault to use STEP only forecast within
	public static String coulombFilterPath =  INPUT_DIR + "/stress_tst_big.dat";
	public static String outputCoulombRatePath = OUTPUT_DIR + "/coulombRates.txt";
	public static double minCoulombMS_Mag = 7.0; // dont apply coulomb filter based on MS below this (should only be 1 above)
	//public static double extFaultLat1 = -41.07;
	//public static double extFaultLon1=175.195;
	//public static double extFaultLat2 = -41.275;
	//public static double extFaultLon2=174.775;
	//public static double extFaultLat3 = -41.47;
	//public static double extFaultLon3=174.552;
	// darfield fault
	//public static double extFaultLat1=-43.5604;
	//public static double extFaultLon1=172.65924;
	//public static double extFaultLat2=-43.52731;
	//public static double extFaultLon2=172.16794;
	//public static double extFaultLat3=-43.57513;
	//public static double extFaultLon3=171.83232;
	 // AlpineF2K for AF8 Exercise
	 //public static double extFaultLat1 =-42.89;
     //public static double extFaultLon1=171.15;
     //public static double extFaultLat2 =-44.067;
     //public static double extFaultLon2=168.717;
     //public static double extFaultLat3 =-45.0416;
     //public static double extFaultLon3=166.9833;
     
   //Alpine Fault both segments
     //public static double extFaultLat1 =-41.7967;
     //public static double extFaultLon1=172.8783;
     //public static double extFaultLat2 =-42.8933;
     //public static double extFaultLon2=171.15;
     //public static double extFaultLat3 =-45.0416;
     //public static double extFaultLon3=166.9833;

     // Kaikoura Fault
     public static double extFaultLat1 =-42.755;
     public static double extFaultLon1=172.691;
     public static double extFaultLat2 =-42.328;
     public static double extFaultLon2=173.731;
     public static double extFaultLat3 =-41.684;
     public static double extFaultLon3=174.301;
	
	
	public static boolean hasExternalFaultModel=true;

	//the cutoff distance (of the grid spacing) in calculating forecast
	public static double CUTOFF_DISTANCE = 0.5;
	
	
	//event data source
	public static final int  EVENT_SOURCE_FILE = 0;
	public static final int  EVENT_SOURCE_GEONET = 1;
	public static final int  EVENT_SOURCE_CHINA= 2;
	public static final int  EVENT_SOURCE_CSEP= 3;
	
	public static int EVENT_DATA_SOURCE = EVENT_SOURCE_FILE; //specify the source to query quakes
	
	//forecast parameters 
	public static boolean FORECAST_PARAM_FIXED = true;
	public static double FORECAST_A_VALUE = -1.59; //-2.18
	public static double FORECAST_B_VALUE = 1.03;//0.84
	public static double FORECAST_P_VALUE = 1.07;//1.05	  
	public static double FORECAST_C_VALUE = 0.04;
	  
	public static String GEONET_QUAKEML_URL="http://app-dev.geonet.org.nz/services/quake/quakeml/1.0.1/query";
	public static String DEFAULT_TEST_TIMES = "{1}{1}{1,3,7,30,100,300}";
	//default.test.read.back
	public static int DEFAULT_TEST_READ_BACK = 7;
	public static SimpleDateFormat dateformater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static final String PATTERN_SPACE = "[ \t]+";
	//limit to restrict events from catalog
	public static final double SOURCE_MAX_DEPTH = 40;
	public static final double SOURCE_MIN_MAG = 2.5;
	
	public static  boolean SAVE_MODELS = true;
	public static  boolean SAVE_FORECAST_TIME = true;
	
    static{    	 
    	initProperties();
    	// RegionDefaults.setRegion(RegionDefaults.REGION_NZ);
		}
	/**
	 * define the search boundary
	 * used to switch regions (e.g. California, NZ)
	 * @param minLat
	 * @param maxLat
	 * @param minLon
	 * @param maxLon
	 */
	public static  synchronized void setBoundary(double minLat, double maxLat, double minLon, double maxLon){
		searchLatMin = minLat;
		searchLatMax = maxLat;
		searchLongMin = minLon;
		searchLongMax = maxLon;
	}

	/**
	 * set region default to CF or NZ
	 */
	public static  synchronized void setRegion(int regionNum){
		if(regionNum == REGION_CF){
			cubeFilePath =  INPUT_DIR + "/merge.nts"; //"/merge.nts", merge_synthNZ
			backgroundHazardPath = INPUT_DIR +  "/STEP_backGround.txt"; //STEP_NZHazProb.txt STEP_backGround
			BACKGROUND_RATES_FILE_NAME =  INPUT_DIR +  "/AllCal96ModelDaily.txt"; //AllCal96ModelDaily.txt;//"org/opensha/sha/earthquake/rupForecastImpl/step/AllCal96ModelDaily.txt";

			gridPrecision  = gridPrecisionCF;
			grid_anchor = 0; //0 for california
			setBoundary(RegionDefaults.searchLatMin_CF, RegionDefaults.searchLatMax_CF,
					RegionDefaults.searchLongMin_CF, RegionDefaults.searchLongMax_CF);

		}else if(regionNum == REGION_NZ){
			cubeFilePath =  INPUT_DIR + "/merge_NZ.nts"; //"/merge.nts", merge_synthNZ
			backgroundHazardPath = INPUT_DIR +  "/NZDailyHaz_shifted.txt"; //STEP_NZHazProb.txt STEP_backGround
			BACKGROUND_RATES_FILE_NAME =  INPUT_DIR +  "/NZdailyRates_shifted.txt"; //AllCal96ModelDaily.txt;//"org/opensha/sha/earthquake/rupForecastImpl/step/AllCal96ModelDaily.txt";
			System.out.println("Why are we here in setRegion? BACKGROUND_RATES_FILE_NAME=" + BACKGROUND_RATES_FILE_NAME );
			gridPrecision  = gridPrecisionNZ;
			grid_anchor = 0.0; //0.05 for nz

			setBoundary(RegionDefaults.searchLatMin_NZ, RegionDefaults.searchLatMax_NZ,
					RegionDefaults.searchLongMin_NZ, RegionDefaults.searchLongMax_NZ);

		}
	}

	/**
	 * load properties from config file	
	 */
	public static  synchronized void initProperties( ){
		dateformater.setTimeZone(TimeZone.getTimeZone("UTC"));  
		Properties props = new Properties();
		//URL url = ClassLoader.getSystemResource(CONFIG_PATH);
		try {
			System.out.println("CONFIG_FILE=" + new File(CONFIG_FILE).getAbsolutePath());
			File testFilename = new File(CONFIG_FILE);
			if( !testFilename.exists()) {
				System.out.println( "FILE DOES NOT EXIST" + testFilename.getAbsolutePath());
			}
			else {
				System.out.println( "FILE DOES EXIST" + testFilename.getAbsolutePath());
			}
			props.load(new FileInputStream(CONFIG_FILE));
			
			//1. input dir		
			INPUT_DIR =  props.getProperty("data.dir", "data/mattg_test");			
			backgroundHazardPath = INPUT_DIR + "/" +  props.getProperty("input.file.bg.haz", "NZDailyHaz_shifted.txt");
			BACKGROUND_RATES_FILE_NAME = INPUT_DIR + "/" +  props.getProperty("input.file.bg.rates", "NZdailyRates_shifted.txt");
			System.out.println("initProperties BACKGROUND_RATES_FILE_NAME=" + BACKGROUND_RATES_FILE_NAME );

			//2. output dir
			OUTPUT_DIR  =  props.getProperty("output.dir", "output");
			File outputDirectory = new File(OUTPUT_DIR);
			File outputParent = outputDirectory.getParentFile();
			if(outputParent != null && !outputParent.exists()){
				outputParent.mkdir();
			}
			if(!outputDirectory.exists()){
				outputDirectory.mkdir();
			}
			logger.info("outputDirectory " + outputDirectory.getAbsolutePath());
			outputHazardPath = OUTPUT_DIR + "/" +  props.getProperty("output.file.step.prob", "STEP_Probs.txt");
			STEP_AftershockObjectFile = OUTPUT_DIR + "/" +  props.getProperty("output.file.step.aftershock.obj", "STEP_AftershockObj");
			
			outputHazCurvePath = OUTPUT_DIR + "/" +  props.getProperty("output.file.haz.curv.prob", "HazCurve_Probs.txt");
			
			//3. param values
			minMagForMainshock = Double.parseDouble(props.getProperty("min.mag.main", "3.0"));
			minForecastMag = Double.parseDouble(props.getProperty("min.mag.forcast", "4.0"));;//4.0;
			maxForecastMag = Double.parseDouble(props.getProperty("max.mag.forcast", "8.0"));;//8.0;
			deltaForecastMag = Double.parseDouble(props.getProperty("delta.mag.forcast", "0.1"));;//0.1;						
			
			daysFromQDM_Cat = Integer.parseInt(props.getProperty("days.from.qdm", "7.0"));;//7;	 
			Min_Aftershock_R = Double.parseDouble(props.getProperty("min.aftershock.radium", "5.0"));;//5;
			gridSpacing = Double.parseDouble(props.getProperty("grid.spacing", "0.1")); ;//0.1;	 
			gridPrecision  = Double.parseDouble(props.getProperty("grid.precision", "0.01")); ;//0.01
			grid_anchor  = Double.parseDouble(props.getProperty("grid.anchor", "0.0")); ;//0.05
			CUTOFF_DISTANCE = Double.parseDouble(props.getProperty("grid.cutoff", "0.5")); ;//0.5	
			//4. grid coords	  
			searchLatMin = Double.parseDouble(props.getProperty("bg.min.lat", "-47.9")); 
			searchLatMax = Double.parseDouble(props.getProperty("bg.max.lat", "-34.1")); 
			searchLongMin = Double.parseDouble(props.getProperty("bg.min.lon", "164.1")); 
			searchLongMax =Double.parseDouble(props.getProperty("bg.max.lon", "179.9")); 	
			
			
			//5. model format
			MODEL_FORMAT = Integer.parseInt(props.getProperty("model.format", "" + MODEL_FORMAT_OTHER)); 
			SAVE_MODELS = Integer.parseInt(props.getProperty("save.models", "1" )) == 1; 
			logger.info("SAVE_MODELS " + SAVE_MODELS);
			
			//5.1 genric model type
			GENERIC_MODEL_TYPE =  Integer.parseInt(props.getProperty("generic.model.type", "0"  )); 
			//6.model dependent params
			if(MODEL_FORMAT == MODEL_FORMAT_CSEP){
				PARAM_FILE_BY_COMMAND_ARGS = Integer.parseInt(props.getProperty("params.file.option", "0")) == 1;
				logger.info("PARAM_FILE_BY_COMMAND_ARGS " + PARAM_FILE_BY_COMMAND_ARGS);
				if(!PARAM_FILE_BY_COMMAND_ARGS){
					paramFilePath = props.getProperty("model.params.file", INPUT_DIR + "/CSEP_params.txt");				
					setCsepParams(paramFilePath);	
				}
				//other properties
				outputSTEP_Rates = OUTPUT_DIR + "/" +  props.getProperty("output.file.step.rates", "STEP_Rates.txt");
				startForecastAtCurrentTime = false;
				EVENT_DATA_SOURCE = EVENT_SOURCE_CSEP;				
			}else{//not csep
				cubeFilePath = INPUT_DIR + "/" +  props.getProperty("input.file.cube", "merge_NZ.nts");
				EVENT_START_TIME  = parseTime2Cal(props.getProperty("event.start.time"));	
				forecastStartTime = parseTime2Cal(props.getProperty("forecast.start.time"));
				startForecastAtCurrentTime = Integer.parseInt(props.getProperty("start.forecast.current", "1")) == 1 ;
				forecastLengthDays = Double.parseDouble(props.getProperty("forecast.len.days", "1.0"));;//1;				
				outputSTEP_Rates = OUTPUT_DIR + "/" +  props.getProperty("output.file.step.rates", "STEP_Rates.txt");
				outputAftershockRatePath = OUTPUT_DIR + "/" +  props.getProperty("output.file.time.dep.rates", "TimeDepRates.txt");
				EVENT_DATA_SOURCE = Integer.parseInt(props.getProperty("quake.datasource", "0"));
				 //. geonet quake url
				//quake.datasource=0			
				GEONET_QUAKEML_URL = props.getProperty("geonet.quake.url", "http://app-dev.geonet.org.nz/services/quake/quakeml/1.0.1/query");
			}	
		
			//6. default test times ##default continuous test times{minutes}{hours}{days}
		     //default.test.times={1}{1}{1,3,7,30,100,300}
			DEFAULT_TEST_TIMES = props.getProperty("default.test.times");
			//default.test.read.back
			DEFAULT_TEST_READ_BACK = Integer.parseInt(props.getProperty("default.test.read.back", "7"));
			//7. forecast parameters
			//forecast.param.fixed=0
			FORECAST_PARAM_FIXED = 1==Integer.parseInt(props.getProperty("forecast.param.fixed", "0"));
			FORECAST_A_VALUE = Double.parseDouble(props.getProperty("a.value", "-1.67"));//-1.67; //-2.18
			FORECAST_B_VALUE = Double.parseDouble(props.getProperty("b.value", "0.91"));//0.91;//0.84
			FORECAST_P_VALUE = Double.parseDouble(props.getProperty("p.value", "1.08"));//1.08;//1.05	  
			FORECAST_C_VALUE = Double.parseDouble(props.getProperty("c.value", "0.05"));//0.05;
			
			//7. Coulomb filter defaults
			useCoulomb= Boolean.parseBoolean(props.getProperty("cf.useCoulomb","FALSE")) ; // whether or not the coulomb filter is applied
			System.out.print(props.getProperty("cf.useCoulomb"));
			logger.info("Use Coulomb = " + useCoulomb);
			bluePercent = Double.parseDouble(props.getProperty("cf.bluePercent","0.07"));
			redPercent = Double.parseDouble(props.getProperty("cf.redPercent","0.93"));
			coulombFaultRadius = Double.parseDouble(props.getProperty("cf.coulombFaultRadius","0")); // distance in km from fault to use STEP only forecast within
			coulombFilterPath =   INPUT_DIR + "/" + props.getProperty("cf.coulombFilterFile","stress_tst_big.dat");
			outputCoulombRatePath = OUTPUT_DIR + "/" + props.getProperty("cf.outputCoulombRateFile","coulombRates.txt");
			minCoulombMS_Mag = Double.parseDouble(props.getProperty("cf.minCoulombMS_Mag","7.0")); // dont apply coulomb filter based on MS below this (should only be 1 above)
			extFaultLat1 = Double.parseDouble(props.getProperty("cf.extFaultLat1","-42.755"));
			extFaultLon1= Double.parseDouble(props.getProperty("cf.extFaultLon1","172.691"));
			extFaultLat2 = Double.parseDouble(props.getProperty("cf.extFaultLat2","-42.328"));
			extFaultLon2 = Double.parseDouble(props.getProperty("cf.extFaultLon2","173.731"));
			extFaultLat3 = Double.parseDouble(props.getProperty("cf.extFaultLat3","-41.684"));
			logger.info("extFault = " + extFaultLat3);
			extFaultLon3 = Double.parseDouble(props.getProperty("cf.extFaultLon3","174.301"));
			
			hasExternalFaultModel = Boolean.parseBoolean(props.getProperty("cf.hasExternalFaultModel","TRUE"));

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setCsepParams(String filePath) throws FileNotFoundException, IOException {	
		logger.info(">> setCsepParams filePath " + filePath);
		//load csel params
		ArrayList<String> fileLines = FileUtils.loadFile(filePath);
		//logger.error(e);
		String line = fileLines.get(0);
		logger.info("catalog start date " + line);
		EVENT_START_TIME  = parseCsepTime2Cal(line);
		logger.info("EVENT_START_TIME " +  dateformater.format(EVENT_START_TIME.getTime()));
		line = fileLines.get(1);
		logger.info("catalog end date " + line);
		forecastStartTime =  parseCsepTime2Cal(line);
		logger.info("forecastStartTime " +  dateformater.format(forecastStartTime.getTime()));
		line = fileLines.get(2);
		logger.info("forecast length " + line);
		forecastLengthDays = Integer.parseInt(line.trim());
		line = fileLines.get(3);
		logger.info("catalog input file " + line);
		cubeFilePath = line;
		line = fileLines.get(4);
		logger.info("forecast output file " + line);
		outputAftershockRatePath = line;//OUTPUT_DIR + "/" +  props.getProperty("output.file.time.dep.rates", "TimeDepRates.txt");	

		line = fileLines.get(5);
		logger.info("background rate input file " + line);
		BACKGROUND_RATES_FILE_NAME = line;
		
		line = fileLines.get(6);
		logger.info("Coulomb input file " + line);
		coulombFilterPath = line;

		
		
	}

	private static GregorianCalendar parseCsepTime2Cal(String timestr) {
		GregorianCalendar cal = null;
		if(timestr != null){
			String[] dateElements = timestr.split(PATTERN_SPACE);
			//logger.info("dateElements "+ dateElements.length );
			if(dateElements.length == 6){				
				cal = STEP_main.getCurrentGregorianTime();
				cal.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(dateElements[0]));
				cal.set(GregorianCalendar.MONTH, Integer.parseInt(dateElements[1]) - 1);
				cal.set(GregorianCalendar.YEAR, Integer.parseInt(dateElements[2]));
				cal.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(dateElements[3]));
				cal.set(GregorianCalendar.MINUTE, Integer.parseInt(dateElements[4]));
				cal.set(GregorianCalendar.SECOND, Integer.parseInt(dateElements[5]));
			}	
		}	
		return cal;
	}

	private static GregorianCalendar parseTime2Cal(String timestr  ) {
		GregorianCalendar cal ;
		if(timestr != null){
			try {
				Date time = dateformater.parse(timestr);
				cal = STEP_main.getCurrentGregorianTime();
				cal.setTime(time);
			} catch (ParseException e) {						
				logger.error(e);
				cal = null;
			}
		}else{
			cal = null;
		}	
		return cal;
	}


}
