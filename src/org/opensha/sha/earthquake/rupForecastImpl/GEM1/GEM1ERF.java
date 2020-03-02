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

package org.opensha.sha.earthquake.rupForecastImpl.GEM1;


import java.util.ArrayList;


import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.MagScalingRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.PEER_testsMagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.earthquake.rupForecastImpl.FloatingPoissonFaultSource;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.PointToLineSource;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMFaultSourceData;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMPointSourceData;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSubductionFaultSourceData;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;
import org.opensha.sha.faultSurface.ApproxEvenlyGriddedSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.magdist.IncrementalMagFreqDist;


/**
 * <p>Title: 
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : 
 * @Date : 
 * @version 1.0
 */

public class GEM1ERF extends EqkRupForecast{

	// name of this ERF
	public final static String NAME = new String("GEM1 Eqk Rup Forecast");

	//for Debug purposes
	private static String  C = new String("GEM1ERF");
	private boolean D = false;
	
	protected ArrayList<GEMSourceData> gemSourceDataList;
	
	// some fixed parameters
	final static double MINMAG = 0;   // this sets the minimum mag considered in the forecast (overriding that implied in the source data)
	
	// calculation settings (primitive versions of the adj parameters)
	String backSeisValue;
	String backSeisRupValue;
	double lowerSeisDepthValue;
	MagScalingRelationship magScalingRelBackgr;
	double rupOffsetValue;
	double faultDiscrValue;
	MagScalingRelationship magScalingRel;
	double sigmaValue;
	double aspectRatioValue;
	int floaterTypeFlag;
	double duration;		// from the timeSpan

	
	// THE REST IS FOR ALL THE ADJUSTABLE PARAMERS:
	
	// Include or exclude background seis parameter
	public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
	public final static String BACK_SEIS_INCLUDE = new String ("Include");
	public final static String BACK_SEIS_EXCLUDE = new String ("Exclude");
	public final static String BACK_SEIS_ONLY = new String ("Only Background");
	// make the fault-model parameter
	StringParameter backSeisParam;

	// Treat background seis as point of finite ruptures parameter
	public final static String BACK_SEIS_RUP_NAME = new String ("Treat Background Seismicity As");
	public final static String BACK_SEIS_RUP_POINT = new String ("Point Sources");
	public final static String BACK_SEIS_RUP_LINE = new String ("Line Sources (random or given strike)");
	public final static String BACK_SEIS_RUP_CROSS_HAIR = new String ("Cross Hair Line Sources");
	public final static String BACK_SEIS_RUP_SPOKED = new String ("16 Spoked Line Sources");
	public final static String BACK_SEIS_RUP_FINITE_SURF = new String ("Finite Dipping Sources");
	StringParameter backSeisRupParam;

	// default lower seis depth of gridded/background source
	public final static String LOWER_SEIS_DEPTH_BACKGR_PARAM_NAME = "Default Lower Seis Depth";
	public final static Double LOWER_SEIS_DEPTH_BACKGR_PARAM_MIN = new Double(5.0);
	public final static Double LOWER_SEIS_DEPTH_BACKGR_PARAM_MAX = new Double(50);
	public final static Double LOWER_SEIS_DEPTH_BACKGR_PARAM_DEFAULT = new Double(14);
	public final static String LOWER_SEIS_DEPTH_BACKGR_PARAM_UNITS = "km";
	private final static String LOWER_SEIS_DEPTH_BACKGR_PARAM_INFO = "The default lower-seimogenic depth for gridded seismicity=";
	private DoubleParameter lowerSeisDepthParam ;

	// Mag-scaling relationship for turning grip points into finite ruptures
	public final static String MAG_SCALING_REL_BACKGR_PARAM_NAME = "Background Mag-Scaling";
	private final static String MAG_SCALING_REL_BACKGR_PARAM_INFO = " Mag-scaling relationship for computing size of background events";
	StringParameter magScalingRelBackgrParam;

	// For rupture offset length along fault parameter
	public final static String RUP_OFFSET_PARAM_NAME ="Rupture Offset";
	private final static Double RUP_OFFSET_DEFAULT = new Double(5);
	private final static String RUP_OFFSET_PARAM_UNITS = "km";
	private final static String RUP_OFFSET_PARAM_INFO = "The amount floating ruptures are offset along the fault";
	public final static double RUP_OFFSET_PARAM_MIN = 1;
	public final static double RUP_OFFSET_PARAM_MAX = 100;
	DoubleParameter rupOffsetParam;

	// For fault discretization
	public final static String FAULT_DISCR_PARAM_NAME ="Fault Discretization";
	private final static Double FAULT_DISCR_PARAM_DEFAULT = new Double(1);
	private final static String FAULT_DISCR_PARAM_UNITS = "km";
	private final static String FAULT_DISCR_PARAM_INFO = "The discretization of faults";
	public final static double FAULT_DISCR_PARAM_MIN = 1;
	public final static double FAULT_DISCR_PARAM_MAX = 10;
	DoubleParameter faultDiscrParam;

	// Mag-scaling relationship parameter stuff
	public final static String MAG_SCALING_REL_PARAM_NAME = "Rupture Mag-Scaling";
	private final static String MAG_SCALING_REL_PARAM_INFO = " Mag-scaling relationship for computing size of floaters";
	StringParameter magScalingRelParam;


	// Mag-scaling sigma parameter stuff
	public final static String SIGMA_PARAM_NAME =  "Mag Scaling Sigma";
	private final static String SIGMA_PARAM_INFO =  "The standard deviation of the Area(mag) or Length(M) relationship";
	private Double SIGMA_PARAM_MIN = new Double(0);
	private Double SIGMA_PARAM_MAX = new Double(1);
	private Double SIGMA_PARAM_DEFAULT = new Double(0.0);
	DoubleParameter sigmaParam;


	// rupture aspect ratio parameter stuff
	public final static String ASPECT_RATIO_PARAM_NAME = "Rupture Aspect Ratio";
	private final static String ASPECT_RATIO_PARAM_INFO = "The ratio of rupture length to rupture width";
	private Double ASPECT_RATIO_PARAM_MIN = new Double(Double.MIN_VALUE);
	private Double ASPECT_RATIO_PARAM_MAX = new Double(Double.MAX_VALUE);
	private Double ASPECT_RATIO_PARAM_DEFAULT = new Double(1.0);
	DoubleParameter aspectRatioParam;

	// Floater Type
	public final static String FLOATER_TYPE_PARAM_NAME = "Floater Type";
	public final static String FLOATER_TYPE_PARAM_INFO = "Specifies how to float ruptures around the faults";
	public final static String FLOATER_TYPE_FULL_DDW = "Only along strike ( rupture full DDW)";
	public final static String FLOATER_TYPE_ALONG_STRIKE_AND_DOWNDIP = "Along strike and down dip";
	public final static String FLOATER_TYPE_CENTERED_DOWNDIP = "Along strike & centered down dip";
	public final static String FLOATER_TYPE_PARAM_DEFAULT = FLOATER_TYPE_ALONG_STRIKE_AND_DOWNDIP;
	StringParameter floaterTypeParam;


	/**
	 *
	 * No argument constructor
	 */
	public GEM1ERF() {
		this(null);
	}


	/**
	 * This takes a gemSourceDataList
	 */
	public GEM1ERF(ArrayList<GEMSourceData> gemSourceDataList) {
		
		this.gemSourceDataList = gemSourceDataList;

		// create the timespan object with start time and duration in years
		timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
		timeSpan.addParameterChangeListener(this);
		timeSpan.setDuration(50);

		// create and add adj params to list
		initAdjParams();

	}

	// make the adjustable parameters & the list
	private void initAdjParams() {

		ArrayList<String> backSeisOptionsStrings = new ArrayList<String>();
		backSeisOptionsStrings.add(BACK_SEIS_EXCLUDE);
		backSeisOptionsStrings.add(BACK_SEIS_INCLUDE);
		backSeisOptionsStrings.add(BACK_SEIS_ONLY);
		backSeisParam = new StringParameter(BACK_SEIS_NAME, backSeisOptionsStrings,BACK_SEIS_EXCLUDE);

		ArrayList<String> backSeisRupOptionsStrings = new ArrayList<String>();
		backSeisRupOptionsStrings.add(BACK_SEIS_RUP_POINT);
		backSeisRupOptionsStrings.add(BACK_SEIS_RUP_LINE);
		backSeisRupOptionsStrings.add(BACK_SEIS_RUP_CROSS_HAIR);
		backSeisRupOptionsStrings.add(BACK_SEIS_RUP_SPOKED);
		backSeisRupOptionsStrings.add(BACK_SEIS_RUP_FINITE_SURF);
		backSeisRupParam = new StringParameter(BACK_SEIS_RUP_NAME, backSeisRupOptionsStrings,BACK_SEIS_RUP_POINT);

		lowerSeisDepthParam = new DoubleParameter(LOWER_SEIS_DEPTH_BACKGR_PARAM_NAME,LOWER_SEIS_DEPTH_BACKGR_PARAM_MIN,
				LOWER_SEIS_DEPTH_BACKGR_PARAM_MAX,LOWER_SEIS_DEPTH_BACKGR_PARAM_UNITS,LOWER_SEIS_DEPTH_BACKGR_PARAM_DEFAULT);
		lowerSeisDepthParam.setInfo(LOWER_SEIS_DEPTH_BACKGR_PARAM_INFO);

		// create the mag-scaling relationship param
		ArrayList<String> magScalingRelBackgrOptions = new ArrayList<String>();
		magScalingRelBackgrOptions.add(WC1994_MagAreaRelationship.NAME);
		magScalingRelBackgrOptions.add(WC1994_MagLengthRelationship.NAME);
		magScalingRelBackgrOptions.add(PEER_testsMagAreaRelationship.NAME);
		magScalingRelBackgrParam = new StringParameter(MAG_SCALING_REL_BACKGR_PARAM_NAME,magScalingRelBackgrOptions,
				WC1994_MagAreaRelationship.NAME);
		magScalingRelBackgrParam.setInfo(MAG_SCALING_REL_BACKGR_PARAM_INFO);

		rupOffsetParam = new DoubleParameter(RUP_OFFSET_PARAM_NAME,RUP_OFFSET_PARAM_MIN,
				RUP_OFFSET_PARAM_MAX,RUP_OFFSET_PARAM_UNITS,RUP_OFFSET_DEFAULT);
		rupOffsetParam.setInfo(RUP_OFFSET_PARAM_INFO);


		faultDiscrParam = new DoubleParameter(FAULT_DISCR_PARAM_NAME,FAULT_DISCR_PARAM_MIN,
				FAULT_DISCR_PARAM_MAX,FAULT_DISCR_PARAM_UNITS,FAULT_DISCR_PARAM_DEFAULT);
		faultDiscrParam.setInfo(FAULT_DISCR_PARAM_INFO);

		// create the mag-scaling relationship param
		ArrayList<String> magScalingRelOptions = new ArrayList<String>();
		magScalingRelOptions.add(WC1994_MagAreaRelationship.NAME);
		magScalingRelOptions.add(WC1994_MagLengthRelationship.NAME);
		magScalingRelOptions.add(PEER_testsMagAreaRelationship.NAME);
		magScalingRelParam = new StringParameter(MAG_SCALING_REL_PARAM_NAME,magScalingRelOptions,
				WC1994_MagAreaRelationship.NAME);
		magScalingRelParam.setInfo(MAG_SCALING_REL_PARAM_INFO);

		// create the mag-scaling sigma param
		sigmaParam = new DoubleParameter(SIGMA_PARAM_NAME,
				SIGMA_PARAM_MIN, SIGMA_PARAM_MAX, SIGMA_PARAM_DEFAULT);
		sigmaParam.setInfo(SIGMA_PARAM_INFO);

		// create the aspect ratio param
		aspectRatioParam = new DoubleParameter(ASPECT_RATIO_PARAM_NAME,ASPECT_RATIO_PARAM_MIN,
				ASPECT_RATIO_PARAM_MAX,ASPECT_RATIO_PARAM_DEFAULT);
		aspectRatioParam.setInfo(ASPECT_RATIO_PARAM_INFO);

		ArrayList<String> floaterTypeOptions = new ArrayList<String>();
		floaterTypeOptions.add(FLOATER_TYPE_FULL_DDW);
		floaterTypeOptions.add(FLOATER_TYPE_ALONG_STRIKE_AND_DOWNDIP);
		floaterTypeOptions.add(FLOATER_TYPE_CENTERED_DOWNDIP);
		floaterTypeParam = new StringParameter(FLOATER_TYPE_PARAM_NAME,floaterTypeOptions,FLOATER_TYPE_PARAM_DEFAULT);
		floaterTypeParam.setInfo(FLOATER_TYPE_PARAM_INFO);


		// add adjustable parameters to the list
		createParamList();


		// add the change listener to parameters
		backSeisParam.addParameterChangeListener(this);
		backSeisRupParam.addParameterChangeListener(this);
		lowerSeisDepthParam.addParameterChangeListener(this);
		magScalingRelBackgrParam.addParameterChangeListener(this);
		rupOffsetParam.addParameterChangeListener(this);
		faultDiscrParam.addParameterChangeListener(this);
		magScalingRelParam.addParameterChangeListener(this);
		sigmaParam.addParameterChangeListener(this);
		aspectRatioParam.addParameterChangeListener(this);
		floaterTypeParam.addParameterChangeListener(this);

	}


	/**
	 * This put parameters in the ParameterList (depending on settings).
	 * This could be smarter in terms of not showing parameters if certain
	 * GEMSourceData subclasses are not passed in.
	 */
	private void createParamList() {

		adjustableParams = new ParameterList();

		// add adjustable parameters to the list
		adjustableParams.addParameter(backSeisParam);
		if(!backSeisParam.getValue().equals(BACK_SEIS_EXCLUDE)) {
			adjustableParams.addParameter(backSeisRupParam);
			if(!backSeisRupParam.getValue().equals(BACK_SEIS_RUP_POINT)) {
				adjustableParams.addParameter(magScalingRelBackgrParam);
				MagScalingRelationship magScRel = getmagScalingRelationship(magScalingRelBackgrParam.getValue());
				if(backSeisRupParam.getValue().equals(BACK_SEIS_RUP_FINITE_SURF) || (magScRel instanceof MagAreaRelationship)) {
					adjustableParams.addParameter(lowerSeisDepthParam);
				}
			}
		}
		adjustableParams.addParameter(faultDiscrParam);
		adjustableParams.addParameter(rupOffsetParam);
		adjustableParams.addParameter(aspectRatioParam);
		adjustableParams.addParameter(floaterTypeParam);
		adjustableParams.addParameter(magScalingRelParam);
		adjustableParams.addParameter(sigmaParam);
	}



	private MagScalingRelationship getmagScalingRelationship(String magScName) {
		if (magScName.equals(WC1994_MagAreaRelationship.NAME))
			return new WC1994_MagAreaRelationship();
		else if (magScName.equals(WC1994_MagLengthRelationship.NAME))
			return new WC1994_MagLengthRelationship();
		else
			return new PEER_testsMagAreaRelationship();
	}


	protected ProbEqkSource mkFaultSource(GEMFaultSourceData gemFaultSourceData) {
		
		StirlingGriddedSurface faultSurface = new StirlingGriddedSurface(
				gemFaultSourceData.getTrace(),
				gemFaultSourceData.getDip(),
				gemFaultSourceData.getSeismDepthUpp(),
				gemFaultSourceData.getSeismDepthLow(),
                faultDiscrValue);
		
		return new FloatingPoissonFaultSource(
				gemFaultSourceData.getMfd(),	//IncrementalMagFreqDist
                faultSurface,					//EvenlyGriddedSurface			
                magScalingRel,					// MagScalingRelationship
                this.sigmaValue,				// sigma of the mag-scaling relationship
                this.aspectRatioValue,			// floating rupture aspect ration (length/width)
                this.rupOffsetValue,			// floating rupture offset
                gemFaultSourceData.getRake(),	// average rake of the ruptures
                duration,						// duration of forecast
                MINMAG,							// minimum mag considered (probs of those lower set to zero regardless of MFD)
                floaterTypeFlag,				// type of floater (0 for full DDW, 1 for floating both ways, and 2 for floating down center)
                12.0);  						// mags >= to this forced to be full fault ruptures (set as high value for now)
	}
	
	


	protected ProbEqkSource mkSubductionSource(GEMSubductionFaultSourceData gemSubductFaultSourceData) {
		
		ApproxEvenlyGriddedSurface faultSurface = new ApproxEvenlyGriddedSurface(
				gemSubductFaultSourceData.getTopTrace(),
				gemSubductFaultSourceData.getBottomTrace(),
                faultDiscrValue);
		
		
		return new FloatingPoissonFaultSource(
				gemSubductFaultSourceData.getMfd(),	//IncrementalMagFreqDist
                faultSurface,					//EvenlyGriddedSurface			
                magScalingRel,					// MagScalingRelationship
                this.sigmaValue,				// sigma of the mag-scaling relationship
                this.aspectRatioValue,			// floating rupture aspect ration (length/width)
                this.rupOffsetValue,			// floating rupture offset
                gemSubductFaultSourceData.getRake(),	// average rake of the ruptures
                duration,						// duration of forecast
                MINMAG,							// minimum mag considered (probs of those lower set to zero regardless of MFD)
                floaterTypeFlag,					// type of floater (0 for full DDW, 1 for floating both ways, and 2 for floating down center)
                12.0);  						// mags >= to this forced to be full fault ruptures (set as high value for now)
	}
	

	protected ProbEqkSource mkGridSource(GEMPointSourceData gridSourceData) {
		
		if(backSeisRupValue.equals(BACK_SEIS_RUP_POINT)) {
			return new PointEqkSource(gridSourceData.getHypoMagFreqDistAtLoc(),
					gridSourceData.getAveRupTopVsMag(), 
					gridSourceData.getAveHypoDepth(),
					duration, MINMAG);
		}
		else if(backSeisRupValue.equals(BACK_SEIS_RUP_LINE)) {
			return new PointToLineSource(gridSourceData.getHypoMagFreqDistAtLoc(),
					gridSourceData.getAveRupTopVsMag(), 
					gridSourceData.getAveHypoDepth(),
					magScalingRelBackgr,
					lowerSeisDepthValue, 
					duration, MINMAG);
		}
		else if(backSeisRupValue.equals(BACK_SEIS_RUP_CROSS_HAIR)) {
			return new PointToLineSource(gridSourceData.getHypoMagFreqDistAtLoc(),
					gridSourceData.getAveRupTopVsMag(), 
					gridSourceData.getAveHypoDepth(),
					magScalingRelBackgr,
					lowerSeisDepthValue, 
					duration, MINMAG,
					2, 0);
		}
		else if(backSeisRupValue.equals(BACK_SEIS_RUP_SPOKED)) {
			return new PointToLineSource(gridSourceData.getHypoMagFreqDistAtLoc(),
					gridSourceData.getAveRupTopVsMag(), 
					gridSourceData.getAveHypoDepth(),
					magScalingRelBackgr,
					lowerSeisDepthValue, 
					duration, MINMAG,
					16, 0);
		}
		else if(backSeisRupValue.equals(BACK_SEIS_RUP_FINITE_SURF)) {
			throw new RuntimeException(NAME+" - "+BACK_SEIS_RUP_FINITE_SURF+ " is not yet implemented");
		}
		else
			throw new RuntimeException(NAME+" - Unsupported background rupture type");
	}

	

	/**
	 * Returns the  ith earthquake source
	 *
	 * @param iSource : index of the source needed
	 */
	public ProbEqkSource getSource(int iSource) {

		GEMSourceData srcData = gemSourceDataList.get(iSource);
		if(srcData instanceof GEMFaultSourceData)
			return mkFaultSource((GEMFaultSourceData)srcData);
		else if (srcData instanceof GEMSubductionFaultSourceData)
			return mkSubductionSource((GEMSubductionFaultSourceData)srcData);
		else if (srcData instanceof GEMPointSourceData)
			return mkGridSource((GEMPointSourceData)srcData);
		else
			throw new RuntimeException(NAME+": "+srcData.getClass()+" not yet supported");
	}

	/**
	 * Get the number of earthquake sources
	 *
	 * @return integer
	 */
	public int getNumSources(){
		return gemSourceDataList.size();
	}

	/**
	 * Get the list of all earthquake sources.
	 *
	 * @return ArrayList of Prob Earthquake sources
	 */
	public ArrayList  getSourceList(){
		ArrayList list = new ArrayList();
		for(int s=0; s<this.getNumSources();s++)
			list.add(getSource(s));
		return list;
	}

	/**
	 * Return the name for this class
	 *
	 * @return : return the name for this class
	 */
	public String getName(){
		return NAME;
	}

	/**
	 * update the forecast
	 **/

	public void updateForecast() {

		// make sure something has changed
		if(parameterChangeFlag) {
			
			// set the primitive params here so it's not repeated many times in the source-creation methods
			// (this could alternatively be done in the parameterChange method)
			backSeisValue = backSeisParam.getValue();
			backSeisRupValue = backSeisRupParam.getValue();
			lowerSeisDepthValue = lowerSeisDepthParam.getValue();
			magScalingRelBackgr = getmagScalingRelationship(magScalingRelBackgrParam.getValue());
			
			rupOffsetValue = rupOffsetParam.getValue();
			faultDiscrValue = faultDiscrParam.getValue();
			magScalingRel = getmagScalingRelationship(magScalingRelParam.getValue());
			sigmaValue = sigmaParam.getValue();
			aspectRatioValue = aspectRatioParam.getValue();
			String floaterTypeName = floaterTypeParam.getValue();
			if(floaterTypeName.equals(this.FLOATER_TYPE_FULL_DDW)) 
				floaterTypeFlag = 0;
			else if(floaterTypeName.equals(this.FLOATER_TYPE_ALONG_STRIKE_AND_DOWNDIP)) 
				floaterTypeFlag = 1;
			else // (floaterTypeName.equals(this.FLOATER_TYPE_CENTERED_DOWNDIP)) 
				floaterTypeFlag = 2;
			duration = timeSpan.getDuration();

			parameterChangeFlag = false;
		}
	}

	/**
	 *  This acts on a parameter change event.
	 *
	 *  This sets the flag to indicate that the sources need to be updated
	 *
	 * @param  event
	 */
	public void parameterChange(ParameterChangeEvent event) {
		super.parameterChange(event);
		String paramName = event.getParameterName();

		// recreate the parameter list if any of the following were modified
		if(paramName.equals(BACK_SEIS_NAME) || paramName.equals(BACK_SEIS_RUP_NAME) || 
				paramName.equals(MAG_SCALING_REL_BACKGR_PARAM_NAME)){
			createParamList();
		}
		parameterChangeFlag = true;

	}


	// this is temporary for testing purposes
	public static void main(String[] args) {

	}

}
