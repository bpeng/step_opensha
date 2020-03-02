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

package org.opensha.sha.gui;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;

import org.opensha.commons.util.FileUtils;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.disaggregation.DisaggregationCalculator;
import org.opensha.sha.earthquake.EqkRupForecastBaseAPI;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.gui.beans.EqkRupSelectorGuiBean;
import org.opensha.sha.gui.infoTools.ApplicationVersionInfoWindow;
import org.opensha.sha.gui.infoTools.ExceptionWindow;



/**
 * <p>Title: HazardCurveLocalModeApplication</p>
 * <p>Description: This application is extension of HazardCurveApplication, where
 * everything take place on the user's own machine. This version of application
 * does not require any internet connection, the only difference between this
 * application and its parent class that it uses user's system memory for doing
 * any computation. Whereas , in the HazardCurve application all calculations
 * take place on the users machine.</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class HazardCurveLocalModeApplication extends HazardCurveServerModeApplication {

	//public final static String NSHMP08_CEUS_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.NSHMP_CEUS08.NSHMP08_CEUS_ERF";
	public final static String GEM1_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.GEM1.GEM1ERF";
	public final static String FRANKEL_ADJ_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast";
	public final static String FRANKEL_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_EqkRupForecast";
	public final static String FRANKEL02_ADJ_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast";

	public final static String WG02_ERF_LIST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.WG02.WG02_ERF_Epistemic_List";
	public final static String WG02_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.WG02.WG02_EqkRupForecast";
	public final static String WGCEP_UCERF1_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF1.WGCEP_UCERF1_EqkRupForecast";
	public final static String WGCEP_UCERF_2_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2";
	public final static String WGCEP_UCERF_2_EPISTEMIC_LIST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList";
	public final static String WGCEP_AVG_UCERF_2_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2";

	public final static String PEER_AREA_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_AreaForecast";
	public final static String PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_NonPlanarFaultForecast";
	public final static String PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_MultiSourceForecast";
	public final static String PEER_LOGIC_TREE_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_LogicTreeERF_List";
	//public final static String STEP_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast";
	public final static String STEP_ALASKA_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.step.STEP_AlaskanPipeForecast";
	public final static String OLD_POISSON_FAULT_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.OldFloatingPoissonFaultERF";
	public final static String POISSON_FAULT_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.FloatingPoissonFaultERF";
	public final static String SIMPLE_FAULT_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.PoissonFaultERF";
	public final static String POINT_SRC_FORECAST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.PointSourceERF";
	public final static String POINT_SRC_TO_LINE_ERF_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.PointToLineSourceERF";
	public final static String POINT_SRC_TO_LINE_ERF_LIST_TEST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.PointToLineSourceERF_ListTest";
	public final static String POINT2MULT_VSS_FORECAST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.Point2MultVertSS_Fault.Point2MultVertSS_FaultERF";
	public final static String POINT2MULT_VSS_ERF_LIST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.Point2MultVertSS_Fault.Point2MultVertSS_FaultERF_List";
	public final static String YUCCA_MOUNTAIN_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.YuccaMountain.YuccaMountainERF";
	public final static String YUCCA_MOUNTAIN_ERF_LIST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.YuccaMountain.YuccaMountainERF_List";
	//public final static String CYBERSHAKE_ERF_LIST_CLASS_NAME="org.opensha.sha.cybershake.openshaAPIs.CyberShakeERF";
	//public final static String CYBERSHAKE_ERF_WRAPPER_LIST_CLASS_NAME="org.opensha.sha.cybershake.openshaAPIs.CyberShakeUCERFWrapper_ERF";
	public final static String NZ_ERF0909_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.NewZealand.NewZealandERF0909";
	public final static String GEM_TEST_ERF_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.GEM.TestGEM_ERF";
	
	public final static String URS_AVG_UCERF_2_CLASS_NAME="scratch.ned.URS.URS_MeanUCERF2";


	protected final static String appURL = "http://www.opensha.org/applications/hazCurvApp/HazardCurveApp.jar";

	/**
	 * Returns the Application version
	 * @return String
	 */

	public static String getAppVersion(){
		return version;
	}



	/**
	 * Checks if the current version of the application is latest else direct the
	 * user to the latest version on the website.
	 */
	protected void checkAppVersion(){
		ArrayList hazCurveVersion = null;
		try {
			hazCurveVersion = FileUtils.loadFile(new URL(versionURL));
		}
		catch (Exception ex1) {
			return;
		}
		String appVersionOnWebsite = (String)hazCurveVersion.get(0);
		if(!appVersionOnWebsite.trim().equals(version.trim())){
			try{
				ApplicationVersionInfoWindow messageWindow =
					new ApplicationVersionInfoWindow(appURL,
							this.versionUpdateInfoURL,
							"App Version Update", this);
				Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				messageWindow.setLocation( (dim.width -
						messageWindow.getSize().width) / 2,
						(dim.height -
								messageWindow.getSize().height) / 2);
				messageWindow.setVisible(true);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		return;

	}

	public static ArrayList<String> getLocalERFClasses() {
		ArrayList<String> erf_Classes = new ArrayList<String>();

		//adding the client based ERF's to the application
		erf_Classes.add(GEM1_FORECAST_CLASS_NAME);
		erf_Classes.add(POINT_SRC_TO_LINE_ERF_CLASS_NAME);
		erf_Classes.add(POINT_SRC_TO_LINE_ERF_LIST_TEST_CLASS_NAME);
		erf_Classes.add(URS_AVG_UCERF_2_CLASS_NAME);
		erf_Classes.add(FRANKEL_ADJ_FORECAST_CLASS_NAME);
		erf_Classes.add(FRANKEL_FORECAST_CLASS_NAME);
		erf_Classes.add(FRANKEL02_ADJ_FORECAST_CLASS_NAME);
		//      erf_Classes.add(NSHMP08_CEUS_ERF_CLASS_NAME);
		erf_Classes.add(WG02_ERF_LIST_CLASS_NAME);
		erf_Classes.add(WGCEP_UCERF1_CLASS_NAME);
		erf_Classes.add(PEER_AREA_FORECAST_CLASS_NAME);
		erf_Classes.add(PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
		erf_Classes.add(PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
		erf_Classes.add(PEER_LOGIC_TREE_FORECAST_CLASS_NAME);
		//erf_Classes.add(STEP_FORECAST_CLASS_NAME);
		erf_Classes.add(STEP_ALASKA_ERF_CLASS_NAME);
		erf_Classes.add(OLD_POISSON_FAULT_ERF_CLASS_NAME);
		erf_Classes.add(POISSON_FAULT_ERF_CLASS_NAME);
		erf_Classes.add(SIMPLE_FAULT_ERF_CLASS_NAME);
		erf_Classes.add(POINT_SRC_FORECAST_CLASS_NAME);
		erf_Classes.add(POINT2MULT_VSS_FORECAST_CLASS_NAME);
		erf_Classes.add(POINT2MULT_VSS_ERF_LIST_CLASS_NAME);
		
		erf_Classes.add(WGCEP_UCERF_2_CLASS_NAME);
		erf_Classes.add(WGCEP_UCERF_2_EPISTEMIC_LIST_CLASS_NAME);
		erf_Classes.add(WGCEP_AVG_UCERF_2_CLASS_NAME);
		
		erf_Classes.add(YUCCA_MOUNTAIN_CLASS_NAME);
		erf_Classes.add(YUCCA_MOUNTAIN_ERF_LIST_CLASS_NAME);
		
		erf_Classes.add(GEM_TEST_ERF_CLASS_NAME);
		//      erf_Classes.add(CYBERSHAKE_ERF_LIST_CLASS_NAME);
		//      erf_Classes.add(CYBERSHAKE_ERF_WRAPPER_LIST_CLASS_NAME);
		erf_Classes.add(NZ_ERF0909_CLASS_NAME);
		
		return erf_Classes;
	}


	/**
	 * Initialize the ERF Gui Bean
	 */
	protected void initERF_GuiBean() {

		if(erfGuiBean == null){
			// create the ERF Gui Bean object
			ArrayList<String> erf_Classes = getLocalERFClasses();

			try {
				erfGuiBean = new ERF_GuiBean(erf_Classes);
				erfGuiBean.getParameter(erfGuiBean.ERF_PARAM_NAME).
				addParameterChangeListener(this);
			}
			catch (InvocationTargetException e) {

				ExceptionWindow bugWindow = new ExceptionWindow(this, e,
						"Problem occured " +
				"during initialization the ERF's. All parameters are set to default.");
				bugWindow.setVisible(true);
				bugWindow.pack();
				//e.printStackTrace();
				//throw new RuntimeException("Connection to ERF's failed");
			}
		}
		else{
			boolean isCustomRupture = erfRupSelectorGuiBean.isCustomRuptureSelected();
			if(!isCustomRupture){
				EqkRupForecastBaseAPI eqkRupForecast = erfRupSelectorGuiBean.getSelectedEqkRupForecastModel();
				erfGuiBean.setERF(eqkRupForecast);
			}
		}
//		erfPanel.removeAll(); TODO clean
//		erfPanel.add(erfGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
//				GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0 ));
//		erfPanel.updateUI();
	}


	/**
	 * Initialize the ERF Rup Selector Gui Bean
	 */
	protected void initERFSelector_GuiBean() {

		EqkRupForecastBaseAPI erf = null;
		try {
			erf = erfGuiBean.getSelectedERF();
		}
		catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}
		if(erfRupSelectorGuiBean == null){
			// create the ERF Gui Bean object
			ArrayList erf_Classes = new ArrayList();

			/**
			 *  The object class names for all the supported Eqk Rup Forecasts
			 */
			erf_Classes.add(POISSON_FAULT_ERF_CLASS_NAME);
			erf_Classes.add(FRANKEL_ADJ_FORECAST_CLASS_NAME);
			erf_Classes.add(WGCEP_UCERF_2_CLASS_NAME);
			erf_Classes.add(WGCEP_UCERF_2_EPISTEMIC_LIST_CLASS_NAME);
			erf_Classes.add(WGCEP_AVG_UCERF_2_CLASS_NAME);
			//erf_Classes.add(STEP_FORECAST_CLASS_NAME);
			erf_Classes.add(STEP_ALASKA_ERF_CLASS_NAME);
			erf_Classes.add(POISSON_FAULT_ERF_CLASS_NAME);
			erf_Classes.add(FRANKEL02_ADJ_FORECAST_CLASS_NAME);
			erf_Classes.add(PEER_AREA_FORECAST_CLASS_NAME);
			erf_Classes.add(PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
			erf_Classes.add(PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
			erf_Classes.add(WG02_ERF_CLASS_NAME);


			try {

				erfRupSelectorGuiBean = new EqkRupSelectorGuiBean(erf,erf_Classes);
			}
			catch (InvocationTargetException e) {
				throw new RuntimeException("Connection to ERF's failed");
			}
		}
		else
			erfRupSelectorGuiBean.setEqkRupForecastModel(erf);
//		erfPanel.removeAll(); TODO clean
//		//erfGuiBean = null;
//		erfPanel.add(erfRupSelectorGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
//				GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
//		erfPanel.updateUI();
	}

	/**
	 * This method creates the HazardCurveCalc and Disaggregation Calc(if selected) instances.
	 * Calculations are performed on the user's own machine, no internet connection
	 * is required for it.
	 */
	protected void createCalcInstance(){
		try{
			if(calc == null) {
				calc = new HazardCurveCalculator();
				if(this.calcParamsControl != null)
					calc.setAdjustableParams(calcParamsControl.getAdjustableCalcParams());
//System.out.println("Created new calc from LocalModeApp");
			}
			if(disaggregationFlag)
				if(disaggCalc == null)
					disaggCalc = new DisaggregationCalculator();
		}catch(Exception e){

			ExceptionWindow bugWindow = new ExceptionWindow(this,e,this.getParametersInfoAsString());
			bugWindow.setVisible(true);
			bugWindow.pack();
			//     e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		HazardCurveLocalModeApplication applet = new HazardCurveLocalModeApplication();
		applet.checkAppVersion();
		applet.init();
		applet.setTitle("Hazard Curve Local mode Application "+"("+getAppVersion()+")" );
		applet.setVisible(true);
		applet.createCalcInstance();
	}
}
