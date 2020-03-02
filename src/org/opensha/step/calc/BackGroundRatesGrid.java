package org.opensha.step.calc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.commons.data.region.SitesInGriddedRegion;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.earthquake.griddedForecast.GriddedHypoMagFreqDistForecast;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

public class BackGroundRatesGrid extends GriddedHypoMagFreqDistForecast  {
	private static Logger logger = Logger.getLogger(BackGroundRatesGrid.class);

	private double minForecastMag, maxForecastMag, deltaForecastMag;
	private double minMagInSourceFile = 2.0;
	private double[] seqIndAtNode;
	// booleans to help decide if sources need to be made
	private boolean deltaSourcesAlreadyMade = false;
	private boolean backgroundSourcesAlreadyMade = false;
	private boolean backgroundRatesFileAlreadyRead = false;
	//private final static String BACKGROUND_RATES_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/step/AllCal96ModelDaily.txt";
	private String bgGridFilename = RegionDefaults.BACKGROUND_RATES_FILE_NAME;

	// misc values
	private static final double RAKE=0.0;
	private static final double DIP=90.0;
	public static final double DEPTH=0;
	private final double GRID_SPACING= 0.1; 
    
	//private Comparator <HypoMagFreqDistAtLoc> locationComparator;

	/**
	 * stores HypoMagFreqDistAtLoc, 
	 * N.B.
	 * it is an assumption that the hypoMagFreqDistAtLoc list contain the same number and order of locations as 
	 * those in the region grid list, but this is not true for california, may be a map should be used to store the 
	 * hypoMagFreqDist
	 */
	private ArrayList<HypoMagFreqDistAtLoc> hypoMagFreqDist;
	//this map will eventually replace the above list, to cope with the location match problem
	//private HashMap<String,HypoMagFreqDistAtLoc> hypoMagFreqDistMap;
	
	private ArrayList backGroundSourceList;

	public BackGroundRatesGrid()
	{
	  minForecastMag = RegionDefaults.minForecastMag;
	  maxForecastMag =  RegionDefaults.maxForecastMag;
	  deltaForecastMag = RegionDefaults.deltaForecastMag;

	}

	public BackGroundRatesGrid(double minForecastMag, double maxForecastMag, double deltaForecastMag)
	{
		this.minForecastMag = minForecastMag;
		this.maxForecastMag = maxForecastMag;
		this.deltaForecastMag = deltaForecastMag;
	}

	/**
	 * 
	 * @param fileName
	 */
	public BackGroundRatesGrid(String fileName){	
		//take default values for minForecastMag, maxForecastMag, deltaForecastMag??		
		this();		
		setBgGridFilename(fileName);
		initialize();
	}

    public void initialize(){
	    //create the region grid
	    createBackGroundRegion();		
	    loadBackGroundRatesFromFile(bgGridFilename);		
		initSeqIndices();
	}

	/**
	 * //region must be initialised before this can be done
		// set to a dummy value representing the background so that 
		// it can be changed to a sequence index if required later
		// this indicates the sequence that contributes rates at this
		// index.  -1 means no sequence does.
	 */
	public void initSeqIndices()
	{
		if(seqIndAtNode == null)
			seqIndAtNode = new double[getRegion().getNodeCount()];
		logger.info((new StringBuilder()).append("seqIndAtNode ").append(seqIndAtNode.length).toString());
		Arrays.fill(seqIndAtNode, -1D);
	}

	public void setBgGridFilename(String bgGridFilename)
	{
		this.bgGridFilename = bgGridFilename;
	}

	

	/**
	 * Setting the Relm Region to the region for the STEP code.
	 */
	private void createBackGroundRegion(){
		//SitesInGriddedRegion sites = getDefaultRegion();
		this.setBackGroundRegion(getDefaultRegion());

	}

	/**
	 * create GriddedRegion based on the region grid definition
	 * @return
	 */
	public GriddedRegion getDefaultRegion() {		
		return  new GriddedRegion(
					new Location(RegionDefaults.searchLatMin,RegionDefaults.searchLongMin),
					new Location(RegionDefaults.searchLatMax,RegionDefaults.searchLongMax),
					RegionDefaults.gridSpacing, 
					new Location(RegionDefaults.grid_anchor,RegionDefaults.grid_anchor));//NOTE: anchor point at 0.05, 0.05 ensures the grid initialized at .05 
		
	}

	/**
	 * read background grid mag freq dist rates from file and store in a list, 
	 * make sure that the order of the <code> hypoMagFreqDist</code>
	 * are the same as the regions grid list, by using a HashMap
	 * 
	 *
	 */
	public void loadBackGroundRatesFromFile(String fileName){
		// Debug
		ArrayList<String> backgroundRateFileLines = null;
		//read background rates file if needed
		if(!backgroundRatesFileAlreadyRead){
			try {
				logger.info("loadBackGroundGridFromFile $$$ fileName " + new File(fileName).getAbsolutePath());
				backgroundRateFileLines = FileUtils.loadFile( fileName );
			} catch(Exception e) {

				throw new RuntimeException("Background file could not be loaded");
			}
			backgroundRatesFileAlreadyRead = true;
		}
		
		GriddedRegion region =  this.getRegion();
		HashMap <String, HypoMagFreqDistAtLoc> hypoMagFreqDistMap = new HashMap<String, HypoMagFreqDistAtLoc>();
		double lat, lon;

		IncrementalMagFreqDist magFreqDist;
		PointEqkSource ptSource;

		// Get iterator over input-file lines
		ListIterator<String> it = backgroundRateFileLines.listIterator();

		StringTokenizer st;

		int forecastMagStart = getForecastMagStart();
		logger.info(" backgroundRateFileLines " + backgroundRateFileLines.size());
		
		while( it.hasNext() ) {
			// get next line
			String line = it.next().toString();
			//logger.info(" loadBackGroundGridFromFile line=" + line);
			st = new StringTokenizer(line);

			// skip the event ID
			st.nextToken();

			// get lat and lon
			lon =  Double.parseDouble(st.nextToken());
			lat =  Double.parseDouble(st.nextToken());

			//logger.info(" loadBackGroundGridFromFile lat=" + lat + " lon=" + lon);
			
			int numForecastMags = 1 + (int) ( (maxForecastMag - minForecastMag) / this.deltaForecastMag);
			magFreqDist = new IncrementalMagFreqDist(minForecastMag, maxForecastMag, numForecastMags);

			//??? skip the mag=2, 2.1, ... 3.9 -- numForecastMags=20?, is it necessarily 3.9?, is it dependent on minForecastMag?
			for(int j=0; j < forecastMagStart; j++) st.nextToken();
			
			//get the forecast rates
			for(int i = 0;i < numForecastMags; i++) {
				if(st.hasMoreTokens()){
					double rate = Double.parseDouble(st.nextToken());
					magFreqDist.set(i,rate);
				}
			}
			Location loc = new Location(lat,lon,DEPTH);
			//logger.info(" check 1 loc " + loc);
			//store in the map for quick search
			hypoMagFreqDistMap.put(getKey4Location(loc), new HypoMagFreqDistAtLoc(magFreqDist,loc));
		}
		//convert to list, make sure the hypoMagFreqList has the same order as the region grid location list
		hypoMagFreqDist = new ArrayList <HypoMagFreqDistAtLoc>();
		//System.out.println("region.getNumGridLocs() " + region.getNumGridLocs());
		//System.out.println("region.getGridLocationsList " + region.getGridLocationsList().size());
		
		for(int i = 0; i < region.getNodeCount(); i++){
			Location loc = region.getNodeList().getLocationAt(i);
			HypoMagFreqDistAtLoc hypoMagFreqDistAtLoc = hypoMagFreqDistMap.get(getKey4Location(loc));
			if(hypoMagFreqDistAtLoc == null){
				//logger.info(" check 2 loc " + loc);
			} 
			hypoMagFreqDist.add(hypoMagFreqDistAtLoc );
			//}
		}
		
		backgroundSourcesAlreadyMade = true;
	}
	
	

	/**
	 * retujrn a key to identify a location
	 * @param loc
	 * @return
	 */
	public String getKey4Location(Location loc) {		
		return Math.round(loc.getLatitude()/RegionDefaults.gridPrecision) + "_" + Math.round(loc.getLongitude()/RegionDefaults.gridPrecision);
	}

	/**
	 * get the index of the minForecastMag in the Bg grid rate source file 
	 * 
	 * @return
	 */
	public int getForecastMagStart()
	{
		double diff = minForecastMag - minMagInSourceFile;
		if(diff <= 0.0D){
			return 0;
		}else{
			return (int)Math.round(diff / deltaForecastMag);
		}
	}

	public void setBackGroundRegion(GriddedRegion backGroundRegion){
		setRegion(backGroundRegion);
		//this.region = backGroundRegion;
	}

	/**
	 * setMinForecastMag
	 */
	public void setMinForecastMag(double minMag) {
		this.minForecastMag = minMag;
	}

	/**
	 * setMaxForecastMag
	 */
	public void setMaxForecastMag(double maxMag) {
		this.maxForecastMag = maxMag;
	}

	/**
	 * setDeltaForecastMag
	 */
	public void setDeltaForecastMag(double deltaMag) {
		this.deltaForecastMag = deltaMag;
	}

	/**
	 * setSeqIndAtNode
	 * @param ithLocation
	 * @param seqInd
	 * seqIndAtNode[] contains the index of the STEP_CombineForecast
	 * that contributes rates to this node if applicable.  if = -1, the rates
	 * come from the background.  
	 */
	public void setSeqIndAtNode(int ithLocation, int seqInd){
		seqIndAtNode[ithLocation] = seqInd;
	}

	public HypoMagFreqDistAtLoc getHypoMagFreqDistAtLoc(int ithLocation){

		return (HypoMagFreqDistAtLoc)hypoMagFreqDist.get(ithLocation);
	}
	

	public ArrayList<HypoMagFreqDistAtLoc> getMagDistList(){
		return hypoMagFreqDist;
	}


	/**
	 * setMagFreqDistAtLoc
	 * @param locDist
	 * @param ithLocation
	 * set the (gridded) IncrementalMagFreqDist at this location 
	 */

	public void setMagFreqDistAtLoc(IncrementalMagFreqDist locDist, int ithLocation){
		/**
		 * changed Nitin's original definition here as hypoMagFreqDist needs a Location
		 * and this was passing an IncrementalMagFreqDist
		 */
		/**
		GutenbergRichterMagFreqDist tgrd = new GutenbergRichterMagFreqDist(4,8,.1);
		if (locDist.getClass().isInstance(tgrd)){
	        System.out.println("is GRDdist" + ithLocation);
		}
		 **/

		HypoMagFreqDistAtLoc tmpFreqDistAtLoc = (HypoMagFreqDistAtLoc)this.hypoMagFreqDist.get(ithLocation);	
		Location tmpLoc = tmpFreqDistAtLoc.getLocation();
		HypoMagFreqDistAtLoc newFreqDistAtLoc = new HypoMagFreqDistAtLoc(locDist,tmpLoc);

		//hypoMagFreqDist.set(ithLocation, locDist);
		hypoMagFreqDist.set(ithLocation, newFreqDistAtLoc);
	}
	
	/**
	 * setMagFreqDistAtLoc
	 * @param locDist
	 * @param ithLocation
	 * set the (gridded) IncrementalMagFreqDist at this location 
	 */

	public void setMagFreqDistAtLocation(IncrementalMagFreqDist locDist, Location loc){	
		HypoMagFreqDistAtLoc newFreqDistAtLoc = new HypoMagFreqDistAtLoc(locDist,loc);
		//this.hypoMagFreqDistMap.put(this.getKey4Location(loc), newFreqDistAtLoc);
	}
	

	public double[] getSeqIndAtNode() {
		return seqIndAtNode;
	}

	public boolean isBackgroundSourcesAlreadyMade() {
		return backgroundSourcesAlreadyMade;
	}

	public boolean isBackgroundRatesFileAlreadyRead() {
		return backgroundRatesFileAlreadyRead;
	}

	public ArrayList getBackGroundSourceList() {
		return backGroundSourceList;
	}

	public ArrayList getHypoMagFreqDist() {
		return hypoMagFreqDist;
	}

	/**
	 * check two locations equal to each other
	 * @param loc1
	 * @param loc2
	 * @param precision
	 * @return
	 */
	public boolean checkLocaionEquals(Location loc1, Location loc2, double precision) {
	  if ((Math.round(loc1.getLatitude()/precision)) != (Math.round(loc2.getLatitude()/precision))) return false;
   	  if ((Math.round(loc1.getLongitude()/precision)) != (Math.round(loc2.getLongitude()/precision))) return false;
   	  if ((Math.round(loc1.getDepth()/precision)) != (Math.round(loc2.getDepth()/precision))) return false; 
      
   	  return true;
		
	}

	
}
