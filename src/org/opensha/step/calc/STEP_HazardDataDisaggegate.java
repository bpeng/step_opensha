package org.opensha.step.calc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.region.SitesInGriddedRegion;
import org.opensha.commons.exceptions.RegionConstraintException;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.calc.disaggregation.DisaggregationSourceRuptureInfo;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.imr.attenRelImpl.McVerryetal_2000_AttenRel;
import org.opensha.sha.util.SiteTranslator;

public class STEP_HazardDataDisaggegate extends STEP_HazardDataSet{

	public STEP_HazardDataDisaggegate(boolean includeWillsSiteClass) {
		super(includeWillsSiteClass);			
	}
	
	public static void main(String[] args) {
		STEP_HazardDataDisaggegate step = new STEP_HazardDataDisaggegate(false);
		step.runSTEP();	
		System.exit(0);
	}

	
	/**
	 * run step code to calculate forecast for specified time
	 */
	public void runSTEP(){
		//1. create step main
		
		if(stepMain == null){
		   stepMain = new STEP_main();
		}
		stepMain.loadBgGrid();
		
		
		//2. 
		createShakeMapAttenRelInstance();

		//3.get default region
		//SitesInGriddedRegion regionSites = getDefaultRegion();//
		ArrayList <PointEqkSource>  sourceList = this.createStepSourcesFromFile();
		
		String scripts = runAggregate(sourceList);

		//5. output
		if(scripts != null)
		saveScript2File(scripts.toString());
		
	}

	/**
	 * disaggregate the eqk sources and create gmt script
	 * @param sourceList
	 */
	protected String runAggregate(ArrayList<PointEqkSource> sourceList) {
		double MAX_DISTANCE = 500;
		SitesInGriddedRegion sites = getDefaultRegion();//
		
		//PointEqkSourceERF forecast = new PointEqkSourceERF(sourceList);
		try {
			sites.addSiteParams(attenRel.getSiteParamsIterator());
			Site site = sites.getSite(0); //173,43
			site.setLocation(new Location(-43.5,172.75));
			log("site " + site);
			site.setValue(McVerryetal_2000_AttenRel.SITE_TYPE_NAME, McVerryetal_2000_AttenRel.SITE_TYPE_D);
			StepDisaggregationCalculator calculator = new StepDisaggregationCalculator();
			calculator.setNumSourcestoShow(sourceList.size());
			List <DisaggregationSourceRuptureInfo> disaggregatedSourceList = calculator.disaggregate(IML_VALUE, site, attenRel, sourceList,
													MAX_DISTANCE, null);
			
			log("disaggregatedSourceList" + disaggregatedSourceList.size());
			for(DisaggregationSourceRuptureInfo info : disaggregatedSourceList){
				log(".. " + info.getName() + " " + sourceList.get(info.getId()).getLocation() + " " + info.getRate());
			}
			//createGMTScriptForDisaggregationPlot
			List <String> gmtScriptList =  calculator.createGMTScriptForDisaggregationPlot(".");
			log("GMT script >>>>>> ");
			StringBuffer scripts = new StringBuffer();
			for(String script: gmtScriptList){
				//log(script);
				scripts.append(script).append("\n");
			}
			log(scripts.toString());	
			return scripts.toString();
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RegionConstraintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * craetes the output xyz files
	 * @param probVals : Probablity values ArrayList for each Lat and Lon
	 * @param fileName : File to create
	 */
	private void saveScript2File( String contents){
		//int size = probVals.length;		
		File outoutFile = new File(RegionDefaults.OUTPUT_DIR + "/disaggregate.gmt" );
		if( outoutFile.exists()){
			outoutFile.delete();
		}        
		try{
			FileWriter fr = new FileWriter(outoutFile);			
			fr.write(contents);			
			fr.close();
		}catch(IOException ee){
			ee.printStackTrace();
		}
	}
	

	private void log(String string) {
		System.out.println(string);
	}
	
}
