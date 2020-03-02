package org.opensha.step.calc;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.sha.earthquake.griddedForecast.STEP_CombineForecastModels;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;

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
public class IsAftershockToMainshock_Calc {
  //private ObsEqkRupture newEvent;
  //private STEP_CombineForecastModels mainshockModel;
  //private boolean isInZone;
 // private boolean sameEvent;

  public IsAftershockToMainshock_Calc(ObsEqkRupture newEvent,
                                      STEP_CombineForecastModels
                                      mainshockModel) {
	//this.mainshockModel = mainshockModel;
	//this.newEvent = newEvent;
    //calc_IsAftershockToMainshock(mainshockModel,newEvent );
  }

//  /**
//   * get_isAftershock
//   */
//  public boolean get_isAftershock() {
//    return isInZone;
//  }
//  
//  
//
//  public boolean isSameEvent() {
//	return sameEvent;
//}

/**
   * Calc_IsAftershockToPrev
   */
  public  static boolean calc_IsAftershockToMainshock(STEP_CombineForecastModels mainshockModel, ObsEqkRupture newEvent) {	
	  boolean sameEvent = false;
	  if(mainshockModel.getMainShock().equalsObsEqkRupEvent(newEvent)){
		  sameEvent = true;
	  }
	  
    boolean isInZone = false;
    GriddedRegion aftershockZone;
    aftershockZone = mainshockModel.getAfterShockZone();
    ObsEqkRupture mainshock = mainshockModel.getMainShock();
    Location newEventLoc = newEvent.getHypocenterLocation(); 
    // if this event is not accepting aftershocks any more stop
    // here and return a false
    if (mainshockModel.get_isStatic())
      isInZone = false;
    // if it is accepting look and see if this event fits
    else {
      isInZone = aftershockZone.contains(newEventLoc);

      if (isInZone) {
        //if ( (double) mainshock.getMag() > (double) newEvent.getMag()) {
        //mainshockModel.addToAftershockList(newEvent);
      } else {
    	  //mainshockModel.set_isStatic(true);//TODO check if necessary, I have commented as this update the mainshockModel which could be used in subsequent calls, 
          //there is a similar call in STEP_main line357, baishan
      }
    }

    return isInZone;
  }
}

