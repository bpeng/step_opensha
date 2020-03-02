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

package org.opensha.sha.earthquake.griddedForecast;

import org.apache.log4j.Logger;
import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.commons.data.region.Region;
import org.opensha.sha.cybershake.openshaAPIs.CyberShakeEvenlyGriddedSurface;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.SimpleFaultData;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.step.calc.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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
public class STEP_CombineForecastModels
extends AfterShockHypoMagFreqDistForecast {

	public double minForecastMag;
	private double maxForecastMag;
	private double deltaMag;
	//private int numHypoLocation;
	//private double[] grid_aVal, grid_bVal, grid_cVal, grid_pVal, grid_kVal;
	//private double[] node_CompletenessMag;
	private SimpleFaultData mainshockFault;
	public boolean useFixed_cValue = true;
	private boolean useCircularRegion = false;
	private boolean useSausageRegion = false;
	private boolean hasExternalFaultModel = false;
	public double addToMc;
	private double zoneRadius;
	private double gridSpacing;
	public double[] gridDistanceFromFault;
	private GregorianCalendar forecastEndTime, currentTime;
	private boolean isStatic = false, isPrimary = true,
	isSecondary = false, useSeqAndSpatial = false;
	private ArrayList griddedMagFreqDistForecast;
//	private ObsEqkRupList newAftershocksInZone;
	//private RegionDefaults rDefs;
	private TimeSpan timeSpan;
	private double daysSinceMainshockStart, daysSinceMainshockEnd;
	private BackGroundRatesGrid backgroundRatesGrid;
	private double[] kScaler;
	private GenericAfterHypoMagFreqDistForecast genElement;
	private SequenceAfterHypoMagFreqDistForecast seqElement = null;
	private SpatialAfterHypoMagFreqDistForecast spaElement = null;
	private HypoMagFreqDistAtLoc combinedForecast[];
	//private double sampleSizeAIC;
	private GriddedRegion aftershockZone;
	private boolean existSeqElement = false, existSpaElement = false; 
	private boolean usedInForecast = false;
	//grid is updated when aftershock zone becomes sausage
	private boolean gridIsUpdated = false;
	//private double magComplete ;
	private static Logger logger = Logger.getLogger(STEP_CombineForecastModels.class);
	
	 
	/**
	 * STEP_AftershockForecast
	 */
	public STEP_CombineForecastModels(ObsEqkRupture mainshock,
			BackGroundRatesGrid
			backgroundRatesGrid, GregorianCalendar currentTime) {
		this.mainShock = mainshock;
		this.backgroundRatesGrid = backgroundRatesGrid;
		this.currentTime = currentTime;
		//this.rDefs = rDefs;
		this.gridSpacing = backgroundRatesGrid.getRegion().getSpacing();
		//TODO check!!, changed to use the current time from calling method instead of creating one		
		this.calcTimeSpan(this.getCurrentTime());
		this.setDaysSinceMainshock();		
		//this.set_CurrentTime();
		//this.calcTimeSpan();
		
		//this.setDaysSinceMainshock();
		
		//
		//logger.info("daysSinceMainshockEnd=" + this.daysSinceMainshockEnd + " daysSinceMainshockStart=" + this.daysSinceMainshockStart);

		/**
		 * initialise the aftershock zone and mainshock for this model
		 */
		this.set_AftershockZoneRadius();
		this.calcTypeI_AftershockZone();
		ObsEqkRupList emptyAftershocks = new ObsEqkRupList();
		this.setAfterShocks(emptyAftershocks);
		//logger.info("mainShock " + this.mainShock.getMag());
		//this.aftershockZone = this.getAfterShockZone();
		double[] kScaler = DistDecayFromRupCalc.getDensity(this.mainShock,aftershockZone);
		this.setGridDistanceFromFault(DistDecayFromRupCalc.getRupDistList());
		
		GenericAfterHypoMagFreqDistForecast genElement = null;
		if(RegionDefaults.GENERIC_MODEL_TYPE  == 0){
			genElement = new GenericAfterHypoMagFreqDistForecast(this.mainShock,aftershockZone,kScaler);
		}else{//new generic model
			genElement = new NewGenericAfterHypoMagFreqDistForecast(this.mainShock,aftershockZone,kScaler);
		}
		this.genElement = genElement;
		this.setChildParms(genElement);
	}

	/**
	 * setChildParms
	 */
	private void setChildParms(STEP_AftershockForecast model) {
		model.setTimeSpan(timeSpan);
		model.setMinForecastMag(RegionDefaults.minForecastMag);
		model.setMaxForecastMag(RegionDefaults.maxForecastMag);
		model.setDeltaForecastMag(RegionDefaults.deltaForecastMag);
		model.setUseFixed_cValue(RegionDefaults.useFixed_cValue);
		model.dayStart = this.daysSinceMainshockStart;
		model.dayEnd = this.daysSinceMainshockEnd;
	}


	/**
	 * createSequenceElement
	 */
	public void createSequenceElement() {
		this.existSeqElement = true;
		SequenceAfterHypoMagFreqDistForecast seqElement =
			new SequenceAfterHypoMagFreqDistForecast(this.mainShock,this.getAfterShockZone(),this.getAfterShocks());
		seqElement.set_kScaler(kScaler);
		this.setChildParms(seqElement); 
		this.seqElement = seqElement;
	}

	/**
	 * createSpatialElement
	 */
	public void createSpatialElement() {
		this.existSpaElement = true;
		SpatialAfterHypoMagFreqDistForecast spaElement =
			new SpatialAfterHypoMagFreqDistForecast(this.mainShock,this.getAfterShockZone(),this.getAfterShocks());
		this.setChildParms(spaElement);    
		this.spaElement = spaElement;
	}

	/**
	 * createCombinedForecast
	 * 
	 * Combine the 3 forecasts into a single forecast using AIC weighting
	 *
	 */
	public void createCombinedForecast(){
		IncrementalMagFreqDist genDist, seqDist, spaDist, combDist;
		//HypoMagFreqDistAtLoc combHypoMagFreqDist;
		double genLikelihood, seqLikelihood, spaLikelihood;
		double genAIC, seqAIC, spaAIC;
		double[] genOmoriVals = new double[3];
		double[] seqOmoriVals = new double[3];
		double[] spaOmoriVals = new double[3];

		// first we must calculate the Ogata-Omori likelihood score at each
		// gridnode, for each model element created
		if (this.useSeqAndSpatial) {
			int numGridNodes = this.getAfterShockZone().getNodeCount();

			combinedForecast = new HypoMagFreqDistAtLoc[numGridNodes];

			// first find the number observed number within each grid cell for AIC calcs
			CountObsInGrid numInGridCalc =  new CountObsInGrid(this.afterShocks,this.aftershockZone);
			int[] griddedNumObs = numInGridCalc.getNumObsInGridList();

			// get the generic p and c - the same everywhere
			genOmoriVals[2] = this.genElement.get_p_valueGeneric();
			genOmoriVals[1] = this.genElement.get_c_valueGeneric();

			// get the sequence p and c - the same everywhere
			seqOmoriVals[2] = this.seqElement.get_pValSequence();
			seqOmoriVals[1] = this.seqElement.get_cVal_Sequence();

			/** loop over all grid nodes
			 *  get the likelihood for each node, for each model element
			 *  then calc the AICc score
			 */
			//int gLoop = 0;

			// is this correct to be iterating over region, an EvenlyGriddedAPI??
			//Iterator<Location> gridIt = getRegion().getNodeList().iterator();

			//while ( gridIt.hasNext() ){
			for (int gLoop = 0; gLoop < numGridNodes; gLoop++){
				// first find the events that should be associated with this grid cell
				// gridSearchRadius is the radius used for calculating the Reasenberg & Jones params
				double radius = this.spaElement.getGridSearchRadius();
				ObsEqkRupList gridEvents;
				Region nodeRegion = new Region(getRegion().locationForIndex(gLoop),radius);
				gridEvents = this.afterShocks.getObsEqkRupsInside(nodeRegion);
				//logger.info("gridEvents = " +gridEvents.size());
				gridEvents.addObsEqkEventAt(this.mainShock, 0);//TODO check, add the main shock anyway
				
				// get the smoothed generic k val for the grid node
				genOmoriVals[0] = this.genElement.get_k_valueGenericAtLoc(gLoop);
				// 1st calculate the Ogata likelihood of the generic parameters
				OgataLogLike_Calc genOgataCalc = new OgataLogLike_Calc(genOmoriVals,gridEvents);
				genLikelihood = genOgataCalc.get_OgataLogLikelihood();
				// get the AIC score for the generic likelihood
				AkaikeInformationCriterion genAIC_Calc = new AkaikeInformationCriterion(griddedNumObs[gLoop],RegionDefaults.genNumFreeParams,genLikelihood);
				genAIC = genAIC_Calc.getAIC_Score();

				// get the smoothed k val for the sequence model for the grid node
				seqOmoriVals[0] = this.seqElement.get_kVal_SequenceAtLoc(gLoop);
				// now calculate the likelihood for the sequence parameters
				OgataLogLike_Calc seqOgataCalc = new OgataLogLike_Calc(seqOmoriVals,gridEvents);
				seqLikelihood = seqOgataCalc.get_OgataLogLikelihood();
				// get the AIC score for the sequence likelihood
				AkaikeInformationCriterion seqAIC_Calc = new AkaikeInformationCriterion(griddedNumObs[gLoop],RegionDefaults.seqNumFreeParams,seqLikelihood);
				seqAIC = seqAIC_Calc.getAIC_Score();

				// get the parameters for the spatial  model for the grid node
				spaOmoriVals[0] = this.spaElement.get_Spa_kValueAtLoc(gLoop);
				spaOmoriVals[1] = this.spaElement.get_Spa_cValueAtLoc(gLoop);
				spaOmoriVals[2] = this.spaElement.get_Spa_pValueAtLoc(gLoop);
				// now calculate the likelihood for the spatial parameters
				OgataLogLike_Calc spaOgataCalc = new OgataLogLike_Calc(spaOmoriVals,gridEvents);
				spaLikelihood = spaOgataCalc.get_OgataLogLikelihood();
				// get the AIC score for the spatial likelihood
				AkaikeInformationCriterion spaAIC_Calc = new AkaikeInformationCriterion(griddedNumObs[gLoop],RegionDefaults.spaNumFreeParams,spaLikelihood);
				spaAIC = spaAIC_Calc.getAIC_Score();

				// calculate the weighting factor for each model element
				// forecast based on its AIC score.  
				CalcAIC_Weights calcAIC = new CalcAIC_Weights(genAIC,seqAIC,spaAIC);
				double genWeight = calcAIC.getGenWeight();
				double seqWeight = calcAIC.getSeqWeight();
				double spaWeight = calcAIC.getSpaWeight();

				// get the HypoMagFreqDist forecast for this location for each model element
				genDist = this.genElement.getHypoMagFreqDistAtLoc(gLoop).getFirstMagFreqDist();
				seqDist = this.seqElement.getHypoMagFreqDistAtLoc(gLoop).getFirstMagFreqDist(); 
				spaDist = this.spaElement.getHypoMagFreqDistAtLoc(gLoop).getFirstMagFreqDist();


				int numMags = (int)((genDist.getMaxX()-genDist.getMinX())/genDist.getDelta())+1;
				//System.out.println("MaxMag = "+RegionDefaults.maxForecastMag);
				combDist = new IncrementalMagFreqDist(RegionDefaults.minForecastMag,RegionDefaults.maxForecastMag,
						numMags);
				for (int mLoop = 0; mLoop < numMags; mLoop++){
					double distValue = 0;
					double genRates = genDist.getIncrRate(mLoop)*genWeight;
					double seqnRates = seqDist.getIncrRate(mLoop)*seqWeight;
					double spaRates = spaDist.getIncrRate(mLoop)*spaWeight;
					//TODO pls check, exclude those is NaN
					if(!Double.isNaN(genRates)){
						distValue += genRates;						
					}
					if(!Double.isNaN(seqnRates)){
						distValue += seqnRates;							
					}
					if(!Double.isNaN(spaRates)){
						distValue += spaRates;						
					}
					
					// set the combined rate for each mag in the entire mag range
					combDist.set(mLoop,distValue); 
				}
				
				HypoMagFreqDistAtLoc combHypoMagFreqDist = new HypoMagFreqDistAtLoc(combDist,genElement.getLocInGrid(gLoop));

				this.combinedForecast[gLoop]=combHypoMagFreqDist;
			}
		} else {
			this.combinedForecast = genElement.griddedMagFreqDistForecast; //if there is no spatial or seq element just use the generic
		}


	}

	/**
	 * setRegionDefaults
	 */
	//public void setRegionDefaults(RegionDefaults rDefs) {
	//  this.rDefs = rDefs;
	//}


	/**
	 * calc_NodeCompletenessMag
	 * calculate the completeness at each node
	 */
	//public abstract void calc_NodeCompletenessMag();

	/**
	 * set_minForecastMag
	 * the minimum forecast magnitude
	 */
	public void set_minForecastMag(double min_forecastMag) {
		minForecastMag = min_forecastMag;
	}

	/**
	 * set_maxForecastMag
	 * the maximum forecast magnitude
	 */
	public void set_maxForecastMag(double max_forecastMag) {
		maxForecastMag = max_forecastMag;
	}

	/**
	 * set_deltaMag
	 * the magnitude step for the binning of the forecasted magnitude
	 */
	public void set_deltaMag(double delta_mag) {
		deltaMag = delta_mag;
	}

	/**
	 * set_GridSpacing
	 */
	public void set_GridSpacing(double grid_spacing) {
		gridSpacing = grid_spacing;
	}

	/**
	 * setUseFixed_cVal
	 * if true c will be fixed for the Omori calculations
	 * default is fixed
	 */
	public void setUseFixed_cVal(boolean fix_cVal) {
		useFixed_cValue = fix_cVal;
	}

	/**
	 * set_UsedInForecast
	 * This will be set to true is any node in this model
	 * forecasts greater rates than the background
	 * the default is false.
	 * @param used
	 */
	public void set_UsedInForecast(boolean used){
		usedInForecast = used;
	}

	/**
	 * set_addToMcConstant
	 */
	public void set_addToMcConstant(double mcConst) {
		addToMc = mcConst;
	}

	/**
	 * set_isStatic
	 * if true the sequence will take no more aftershocks
	 */
	public void set_isStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	/**
	 * set_isPrimary
	 * if true the sequence can be any model type (generic, sequence, sp. var)
	 * set_isPrimary controls both primary and secondary.
	 */
	public void set_isPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
		if (isPrimary) {
			this.set_isSecondary(false);
		}
		else {
			this.set_isSecondary(true);
		}

	}

	/**
	 * set_isSecondary
	 * if isSecondary is true the model will be forced to be generic.
	 *
	 */
	private void set_isSecondary(boolean isSecondary) {
		this.isSecondary = isSecondary;
	}

	/**
	 * setNewObsEventsList
	 * This should contain All new events - this is the list that will
	 * be used to look for new aftershocks.
	 */
	//public void setNewObsEventsList(ObsEqkRupList newObsEventList) {
	//}

	/**
	 * set_PreviousAftershocks
	 * this will pass the aftershocks for this sequence that were saved in
	 * the last run of the code.
	 */
	public void set_PreviousAftershocks(ObsEqkRupList previousAftershockList) {
	}

	/**
	 * set_AftershockZoneRadius
	 * set the radius based on Wells and Coppersmith
	 *
	 * THIS USES A DIFFERENT RADIUS THAN I HAVE PREVIOUSLY USED!
	 * NEED TO ADD THE SUBSURFACE RUPTURE LENGTH REL TO WC1994
	 */
	public void set_AftershockZoneRadius() {
		ObsEqkRupture mainshock = this.getMainShock();
		double mainshockMag = mainshock.getMag();
		WC1994_MagLengthRelationship WCRel = new WC1994_MagLengthRelationship();
		//this.zoneRadius = WCRel.getMedianLength(mainshockMag);
//		logger.info("USING DOUBLE WC RADIUS IN STEP_CombinedForecast");
		this.zoneRadius = 2*WCRel.getMedianLength(mainshockMag);
		//set the minimum aftetshock zone radius added by baishan discuss with matt 26/1/2010 TODO check
		if(this.zoneRadius < RegionDefaults.Min_Aftershock_R){
			this.zoneRadius = RegionDefaults.Min_Aftershock_R;
		}
	}

	/**
	 * calcTypeI_AftershockZone
	 */
	public void calcTypeI_AftershockZone() {

		if (getHasExternalFaultModel()) {
			// This needs to be set up to read an external fault model.
		}
		else {
			ObsEqkRupture mainshock = this.getMainShock();
			Location mainshockLocation = mainshock.getHypocenterLocation();
//			this.aftershockZone = new GriddedRegion(
//			mainshockLocation, zoneRadius, RegionDefaults.gridSpacing, new Location(0,0));
//			this.aftershockZone.createRegionLocationsList(backgroundRatesGrid.getRegion());
			//logger.info(">> 1  mainShock " + this.mainShock.getMag() + " zoneRadius=" + zoneRadius  );
			// NOTE: baishan this may not be working right; replaces above code      
			Region asZoneGR = new Region(mainshockLocation, zoneRadius);    //create a circle region  
			aftershockZone = backgroundRatesGrid.getRegion().subRegion(asZoneGR);	
			int n = 1;
			while(aftershockZone.isEmpty()){//TODO check, aftershock too small, add at least one grid????
				//treat the anchor as the nearest location to the quake center
				//Location closestGrid =  Location.immutableLocation(aftershockZone.getAnchor().getLatitude(), aftershockZone.getAnchor().getLongitude());
				double closestGrid2CentreDistance = RelativeLocation.getHorzDistance(mainshockLocation, aftershockZone.getAnchor());
				//logger.info("closestGrid2CentreDistance=" + closestGrid2CentreDistance);
				//make the radius bigger to include this loc
				zoneRadius = closestGrid2CentreDistance + 0.01*n;								
				asZoneGR = new Region(mainshockLocation, zoneRadius);    //create a circle region  
				aftershockZone = backgroundRatesGrid.getRegion().subRegion(asZoneGR);	
				n++;
				//aftershockZone.addGridLocation(closestGrid);
			}
			//logger.info("new zoneRadius=" + zoneRadius);
			//logger.info("## check getNodeCount " + aftershockZone.getNodeCount() + " empty? " + aftershockZone.isEmpty());
			
			aftershockZone.setName("circlarAFZ");
			//logger.info(">> 2  mainShock " + this.mainShock.getMag() + " zoneRadius=" + zoneRadius + " aftershockZone isempty "  +  aftershockZone.isEmpty());
			setRegion(aftershockZone);
			this.useCircularRegion = true;
			
			//logger.info(">> 2  mainShock " + this.mainShock.getMag() + " zoneRadius=" + zoneRadius + " aftershockZone size "  +  aftershockZone.getNodeCount());
					
			// make a fault that is only a single point.
			//String faultName = "typeIfault";
			//FaultTrace fault_trace = new FaultTrace(faultName);
			//fault_trace.addLocation(mainshock.getHypocenterLocation());
			//set_FaultSurface(fault_trace);
		}
	}

	/**
	 * This will calculate the appropriate afershock zone based on the availability
	 * of an external model, a circular Type I model, and a sausage shaped Type II model
	 * Type II is only calculated if more than 100 events are found in the circular
	 * Type II model.
	 *
	 * This will also set the aftershock list.
	 */

	public void calcTypeII_AfterShockZone(ObsEqkRupList aftershockList,
			GriddedRegion
			backGroundRatesGrid) {
		logger.info(">>000 calcTypeII_AfterShockZone "   );
		System.out.println(this.hasExternalFaultModel);
		if (this.hasExternalFaultModel) {
			
			STEP_TypeIIAftershockZone_Calc typeIIcalc = new STEP_TypeIIAftershockZone_Calc(aftershockList, this);
			typeIIcalc.CreateAftershockZoneDef();//??
			GriddedRegion typeII_AS_Zone = typeIIcalc.get_TypeIIAftershockZone();
			//typeII_AS_Zone.createRegionLocationsList(backGroundRatesGrid); 

			// TODO NOTE: baishan this may not be working right; replaces above code
			GriddedRegion typeII_Zone = 
				backGroundRatesGrid.subRegion(typeII_AS_Zone);
			// end NOTE
			//logger.info(">> calcTypeII_AfterShockZone " + typeII_Zone );
			typeII_Zone.setName("sausageAFZ");
			this.aftershockZone = typeII_Zone;
			setRegion(typeII_Zone);
			//this.region = typeII_Zone;
			//TODO double check the following 3 setters, the aftershockzone of all the elements are identical      
			if(this.seqElement != null ) {
				this.seqElement.setAftershockZone(typeII_Zone);
			}
			if(this.genElement != null ){
				this.genElement.setAfterShockZone(typeII_Zone);   
				//TODO check !! also need to update the castSausageRegion
				//this.genElement.setCastSausageRegion(typeII_Zone);
			}
			if(this.spaElement != null ) {
				this.spaElement.setAftershockZone(typeII_Zone);    	 
			}
			
			this.useSausageRegion = true;
			
			String faultName = "external_typeIIfault";
			// add the synthetic fault to the fault trace
			// do not add the 2nd element as it is the same as the 3rd (the mainshock location)
			LocationList extFaultPoints = new LocationList();
			extFaultPoints.addLocation(RegionDefaults.extFaultLat1, RegionDefaults.extFaultLon1, 0);
			extFaultPoints.addLocation(RegionDefaults.extFaultLat2, RegionDefaults.extFaultLon2, 0);
			extFaultPoints.addLocation(RegionDefaults.extFaultLat3, RegionDefaults.extFaultLon3, 0);

			FaultTrace fault_trace = new FaultTrace(faultName);
			fault_trace.addLocation(extFaultPoints.getLocationAt(0));
			fault_trace.addLocation(extFaultPoints.getLocationAt(1));
			fault_trace.addLocation(extFaultPoints.getLocationAt(2));
			System.out.println("EXTERNAL "+this.mainShock.getMag()+" fault_trace " + extFaultPoints.getLocationAt(0) + " " + extFaultPoints.getLocationAt(1) + " " + extFaultPoints.getLocationAt(2) );
					
			
			//			
			set_FaultSurface(fault_trace);
			
			// This needs to be set up to read an external fault model.
		}
		else {
			STEP_TypeIIAftershockZone_Calc typeIIcalc = new STEP_TypeIIAftershockZone_Calc(aftershockList, this);
			typeIIcalc.CreateAftershockZoneDef();//??
			GriddedRegion typeII_AS_Zone = typeIIcalc.get_TypeIIAftershockZone();
			//typeII_AS_Zone.createRegionLocationsList(backGroundRatesGrid); 

			// TODO NOTE: baishan this may not be working right; replaces above code
			GriddedRegion typeII_Zone = 
				backGroundRatesGrid.subRegion(typeII_AS_Zone);
			// end NOTE
			//logger.info(">> calcTypeII_AfterShockZone " + typeII_Zone );
			typeII_Zone.setName("sausageAFZ");
			this.aftershockZone = typeII_Zone;
			setRegion(typeII_Zone);
			//this.region = typeII_Zone;
			//TODO double check the following 3 setters, the aftershockzone of all the elements are identical      
			if(this.seqElement != null ) {
				this.seqElement.setAftershockZone(typeII_Zone);
			}
			if(this.genElement != null ){
				this.genElement.setAfterShockZone(typeII_Zone);   
				//TODO check !! also need to update the castSausageRegion
				//this.genElement.setCastSausageRegion(typeII_Zone);
			}
			if(this.spaElement != null ) {
				this.spaElement.setAftershockZone(typeII_Zone);    	 
			}
			
			this.useSausageRegion = true;

			LocationList faultPoints = typeIIcalc.getTypeIIFaultModel();
			String faultName = "typeIIfault";
			// add the synthetic fault to the fault trace
			// do not add the 2nd element as it is the same as the 3rd (the mainshock location)
			FaultTrace fault_trace = new FaultTrace(faultName);
			fault_trace.addLocation(faultPoints.getLocationAt(0));
			fault_trace.addLocation(faultPoints.getLocationAt(1));
			fault_trace.addLocation(faultPoints.getLocationAt(2));
			System.out.println("fault_trace " + faultPoints.getLocationAt(0) + " " + faultPoints.getLocationAt(1) + " " + faultPoints.getLocationAt(2) );
			
			//			
			set_FaultSurface(fault_trace);
		}
	}
	

	/**
	 * try set the ruptureSurface for the main shock
	 * TODO check the class used is correct, CyberShakeEvenlyGriddedSurface??
	 * @param fault_trace
	 */
	private void set_FaultSurface(FaultTrace fault_trace) {	
		CyberShakeEvenlyGriddedSurface griddedSurface = new CyberShakeEvenlyGriddedSurface(1, 
				fault_trace.size(), 
				this.aftershockZone.getSpacing());
	    ArrayList<Location> locs = new ArrayList <Location> ();
	    for(int i = 0 ; i < fault_trace.getNumLocations(); i++){
	    	Location loc = fault_trace.getLocationAt(i);
	    	locs.add(loc);
	    }
		griddedSurface.setAllLocations( locs );	    
	    
//		SimpleListricGriddedSurface griddedSurface 
//		    = new SimpleListricGriddedSurface(fault_trace, new ArrayList(), new ArrayList(),
//		    		this.aftershockZone.getGridSpacing());
		this.mainShock.setRuptureSurface(griddedSurface);		
	}


	/**
	 * set_CurrentTime
	 * this sets the forecast start time as the current time.
	 */
	private void set_CurrentTime() {
		Calendar curTime = new GregorianCalendar(TimeZone.getTimeZone(
		"GMT"));
		int year = curTime.get(Calendar.YEAR);
		int month = curTime.get(Calendar.MONTH);
		int day = curTime.get(Calendar.DAY_OF_MONTH);
		int hour24 = curTime.get(Calendar.HOUR_OF_DAY);
		int min = curTime.get(Calendar.MINUTE);
		int sec = curTime.get(Calendar.SECOND);

		this.currentTime = new GregorianCalendar(year, month,
				day, hour24, min, sec);
	}

	public void set_CurrentTime(GregorianCalendar currentTime){
		this.currentTime = currentTime;
	}

	/**
	 * calcTimeSpan
	 */
	public void calcTimeSpan() {
		String durationUnits = "Days";
		String timePrecision = "Seconds";
		timeSpan = new TimeSpan(timePrecision, durationUnits);

		//if (RegionDefaults.startForecastAtCurrentTime) {
			set_CurrentTime();
			timeSpan.setStartTime(currentTime);
			timeSpan.setDuration(RegionDefaults.forecastLengthDays);
//		}
//		else {
//			timeSpan.setStartTime(RegionDefaults.forecastStartTime);
//			timeSpan.setDuration(RegionDefaults.forecastLengthDays);
//		}
		this.setTimeSpan(timeSpan);
	}

	/**
	 * setDaysSinceMainshock
	 */
	public void setDaysSinceMainshock() {
		String durationUnits = "Days";
		GregorianCalendar startDate = this.timeSpan.getStartTimeCalendar();
		//logger.info("From Timespan ="+startDate.toString());
		double duration = this.timeSpan.getDuration(durationUnits);
		ObsEqkRupture mainshock = this.getMainShock();
		GregorianCalendar mainshockDate = mainshock.getOriginTime();
		long startInMils = startDate.getTimeInMillis();
		long mainshockInMils = mainshockDate.getTimeInMillis();
		long timeDiffMils = startInMils - mainshockInMils;
		//logger.info("  mainshockDate " + mainshockDate.getTime() + " mag " + mainshock.getMag());
		//logger.info("  startDate " + startDate.getTime());
		
		this.daysSinceMainshockStart = timeDiffMils / 1000.0 / 60.0 / 60.0 / 24.0;
		this.daysSinceMainshockEnd = this.daysSinceMainshockStart + duration;
	}

	/**
	 * get_useSeqAndSpatial
	 */
	public void set_useSeqAndSpatial() {
		int size = afterShocks.size();
		if(size < 100)
			useSeqAndSpatial = false;
		else{
			CompletenessMagCalc compMagCalc = new CompletenessMagCalc(this.getAfterShocks());		   
			double mComplete = compMagCalc.getMcBest();	
			if (this.mainShock.getMag() >= 7.0){
				mComplete = 4.0;
				System.out.println("Seq Mc for M>7.0 forced to = "+mComplete);
			}
			ObsEqkRupList compList = this.afterShocks.getObsEqkRupsAboveMag(mComplete);
			if (compList.size() > 100){
				this.useSeqAndSpatial = true;
				System.out.println("Mag is seq = "+this.mainShock.getMag());
				}
			else
				this.useSeqAndSpatial = false;
			//this.useSeqAndSpatial = false;
			//System.out.println("FORCING GENERIC ONLY IN STEP_COMBINEFORECAST");
		}
	}

	/**
	 * getDaysSinceMainshockStart
	 */
	public double getDaysSinceMainshockStart() {
		return daysSinceMainshockStart;
	}

	/**
	 * getDaysSinceMainshockEnd
	 */
	public double getDaysSinceMainshockEnd() {
		return daysSinceMainshockEnd;
	}

	/**
	 * returns true is this model has been used in a forecast and should
	 * be retained.
	 * @return boolean
	 */
	public boolean get_UsedInForecast(){
		return usedInForecast;
	}


	/**
	 * setHasExternalFaultModel
	 */
	public void setHasExternalFaultModel(boolean hasExternalFaultModel) {
		this.hasExternalFaultModel = hasExternalFaultModel;
	}

	/**
	 * Set the fault surface that will be used do define a Type II
	 * aftershock zone.
	 * This will not be used in a spatially varying model.
	 */

	/**public void set_FaultSurface(FaultTrace fault_trace) {
    mainshockFault = new SimpleFaultData();
    mainshockFault.setAveDip(90.0);
    mainshockFault.setFaultTrace(fault_trace);
    mainshockFault.setLowerSeismogenicDepth(rDefs.lowerSeismoDepth);
    mainshockFault.setUpperSeismogenicDepth(rDefs.upperSeismoDepth);
  } */

	/**
	 * get_minForecastMag
	 */
	public double get_minForecastMag() {
		return minForecastMag;
	}

	/**
	 * get_maxForecastMag
	 */
	public double get_maxForecastMag() {
		return maxForecastMag;
	}

	/**
	 * get_deltaMag
	 */
	public double get_deltaMag() {
		return deltaMag;
	}

	/**
	 * get_useSeqAndSpatial
	 */
	public boolean get_useSeqAndSpatial() {
		return this.useSeqAndSpatial;
	}

	/**
	 * get_GridSpacing
	 */
	public double get_GridSpacing() {
		return gridSpacing;
	}

	/**
	 * get_FaultModel
	 */
	public SimpleFaultData get_FaultModel() {
		return mainshockFault;
	}

	/**
	 * get_addToMcConst
	 */
	public double get_addToMcConst() {
		return this.addToMc;
	}

	/**
	 * get_isStatic
	 */
	public boolean get_isStatic() {
		return this.isStatic;
	}

	/**
	 * get_isPrimary
	 */
	public boolean get_isPrimary() {
		return this.isPrimary;
	}

	/**
	 * get_isSecondary
	 */
	public boolean get_isSecondary() {
		return this.isSecondary;
	}

	/**
	 * get_AftershockZoneRadius
	 */
	public double get_AftershockZoneRadius() {
		return zoneRadius;
	}

	/**
	 * get_griddedMagFreqDistForecast
	 */
	public ArrayList get_griddedMagFreqDistForecast() {
		return griddedMagFreqDistForecast;
	}

	/**
	 * getHasExternalFaultModel
	 */
	public boolean getHasExternalFaultModel() {
		return this.hasExternalFaultModel;
	}

	/**
	 * getGenElement
	 */
	public GenericAfterHypoMagFreqDistForecast getGenElement() {
		return this.genElement;
	}

	/**
	 * getSeqElement
	 */
	public SequenceAfterHypoMagFreqDistForecast getSeqElement() {
		return this.seqElement;
	}

	/**
	 * getSpaElement
	 */
	public SpatialAfterHypoMagFreqDistForecast getSpaElement() {
		return this.spaElement;
	}

	/**
	 * getHypoMagFreqDistAtLoc
	 * this will return the AIC combined forecast at the location
	 */
	public HypoMagFreqDistAtLoc getHypoMagFreqDistAtLoc(int ithLocation) {
		return combinedForecast[ithLocation];
	}

	/**
	 * getHypoMagFreqDistAtLoc
	 * this will return the AIC combined forecast at the location
	 */
	/**public HypoMagFreqDistAtLoc getHypoMagFreqDistAtLoc(Location loc) {
	 HypoMagFreqDistAtLoc locDist = combinedForecast[loc];
	 return locDist;
  }**/

	/**
	 * getGriddedAIC_CombinedForecast
	 * @return return an array of HypoMagFreqDistAtLoc for the gridded
	 * AIC combined forecast
	 */
	public HypoMagFreqDistAtLoc[] getGriddedAIC_CombinedForecast(){
		return combinedForecast;
	}

	/**
	 * getExistSeqModel
	 * @return boolean
	 * returns true if the seq model has been created
	 */
	public boolean getExistSeqElement(){
		return this.existSeqElement;
	}

	/**
	 * getExistSpaModel
	 * @return boolean
	 * returns true is the spa model has been created
	 */
	public boolean getExistSpaElement(){
		return this.existSpaElement;
	}

	public GregorianCalendar getCurrentTime() {
		return currentTime;
	}
	
	public void setCurrentTime(GregorianCalendar currentTime) {
		this.currentTime = currentTime;
	}	

	/**
	 * update time span etc based on current time
	 * @param currtTime 
	 */
	public void updateCurrentTime(GregorianCalendar currtTime){
		this.currentTime = currtTime;

		this.calcTimeSpan(this.getCurrentTime());
		this.setDaysSinceMainshock();
		if(this.genElement != null){
			this.setChildParms(genElement);
		}
		if(this.seqElement != null){
			this.setChildParms(seqElement);
		}
		if(this.spaElement != null){
			this.setChildParms(spaElement);
		}
	}
	
	/**
	 * calcTimeSpan
	 * @param currentTime2 
	 */
	public void calcTimeSpan(GregorianCalendar currentTime2) {
		String durationUnits = "Days";
		String timePrecision = "Seconds";
		timeSpan = new TimeSpan(timePrecision, durationUnits);

		//if (RegionDefaults.startForecastAtCurrentTime) {
			setCurrentTime(currentTime2);
			timeSpan.setStartTime(currentTime);
			timeSpan.setDuration(RegionDefaults.forecastLengthDays);
//		}
//		else {
//			timeSpan.setStartTime(RegionDefaults.forecastStartTime);
//			timeSpan.setDuration(RegionDefaults.forecastLengthDays);
//		}
		this.setTimeSpan(timeSpan);
	}
	
	public BackGroundRatesGrid getBackgroundRatesGrid() {
		return backgroundRatesGrid;
	}	
	

	public boolean isUseSausageRegion() {
		return useSausageRegion;
	}	
	

	public boolean isGridIsUpdated() {
		return gridIsUpdated;
	}

	public void setGridIsUpdated(boolean gridIsUpdated) {
		this.gridIsUpdated = gridIsUpdated;
	}

	public boolean updateAftershockZone() {	
		gridIsUpdated  = false;
		int numAftershocks = getAfterShocks().size();
		// logger.info(">> updateAftershockZone numAftershocks " + numAftershocks + " aftershockZone " +aftershockZone.getName()  + " aftershockZone size " + this.aftershockZone.getNodeList().size()  + " zoneRadius " + this.zoneRadius);	  
		  //boolean hasExternalFault = getHasExternalFaultModel();	
		  if ((numAftershocks >= 100)) {
		  //if ((numAftershocks >= 100) && (getHasExternalFaultModel() == false)) {
			  calcTypeII_AfterShockZone(getAfterShocks(),getBackgroundRatesGrid().getRegion());
			  gridIsUpdated  = true;
			 //return true;
		  } 
		  return gridIsUpdated;		
	}
	
	public void setGridDistanceFromFault(double[] gridDist){
		this.gridDistanceFromFault = gridDist;
	}
	
	public double[] getGridDistanceFromFault(){
		return this.gridDistanceFromFault;
	}

}
