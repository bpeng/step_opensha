/**
 * 
 */
package org.opensha.step.calc;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.opensha.sha.earthquake.griddedForecast.GenericAfterHypoMagFreqDistForecast;
import org.opensha.sha.earthquake.griddedForecast.STEP_CombineForecastModels;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;

/**
 * @author matt
 *
 */
public class PurgeMainshockList {
	
	//private ArrayList <STEP_CombineForecastModels> finalModels ;
	//private static double daysToForget;
	//private static STEP_CombineForecastModels mainshockModel;
	//private static double msAge;
	
	
	/**
	 * @param finalModels
	 *
	 */
	public static void removeModels(ArrayList<STEP_CombineForecastModels> finalModels){
		int numMs = finalModels.size();
		int msLoop = 0;
		while (msLoop < numMs){
			STEP_CombineForecastModels mainshockModel = finalModels.get(msLoop);	           
			double msAge = mainshockModel.getDaysSinceMainshockStart();
			//First check if the MS is too recent
			if (msAge <= RegionDefaults.daysFromQDM_Cat){
				finalModels.remove(msLoop);
				--numMs;
			}
			// then make sure it is actually being used in a forecast
			else if
				(!mainshockModel.get_UsedInForecast()){
				finalModels.remove(msLoop);
				--numMs;
			}
			else{
				//removeAftershocks(mainshockModel);//TODO check complete
				++msLoop;
			}
		}	
				
	}

	/**
	 * remove after shocks that are within the uncertain period
	 * TODO: when aftershock are removed and the number of aftershocks become less
	 *       than 100, shall the aftershock zone be re defined to a circle?
	 * 
	 * @param mainshockModel
	 */
	private static void removeAftershocks(
			STEP_CombineForecastModels mainshockModel) {
		ObsEqkRupList newAfterShocks = new ObsEqkRupList();
		GregorianCalendar cuurentTime = mainshockModel.getCurrentTime();
		
		for(int index = 0; index < mainshockModel.getAfterShocks().size(); index++){						
			ObsEqkRupture shock = mainshockModel.getAfterShocks().getObsEqkRuptureAt(index);
			long timeDiff = cuurentTime.getTimeInMillis() - shock.getOriginTime().getTimeInMillis();
			if(timeDiff/24d/60/60/1000 <= RegionDefaults.daysFromQDM_Cat ){
			//this shock is within the uncertain period				
			}else{
				newAfterShocks.addObsEqkEvent(shock);
			}
		}
		mainshockModel.getAfterShocks().clear();
		mainshockModel.setAfterShocks(newAfterShocks);
		//redefine aftershock zone?
		if(newAfterShocks.size() < 100){
		  //checktypeIAftershockZoneDef(mainshockModel);
		}
	}
	
	/**
	 * when aftershocks are removed from a step model 
	 * check that if the number of aftershocks is < 100,
	 * and change the aftershock zone from type II to typeI. 
	 * @param sTEPAftershockForecastList
	 */
	private static void checktypeIAftershockZoneDef(
			STEP_CombineForecastModels model) {
		if(model.isUseSausageRegion()){//update only those sausage zone
			model.calcTypeI_AftershockZone();
			
//			double[] kScaler = DistDecayFromRupCalc.getDensity(this.mainShock,aftershockZone);
//			GenericAfterHypoMagFreqDistForecast genElement =
//				new GenericAfterHypoMagFreqDistForecast(this.mainShock,aftershockZone,kScaler);
//			this.genElement = genElement;
//			this.setChildParms(genElement);
		}
		
	}
}
