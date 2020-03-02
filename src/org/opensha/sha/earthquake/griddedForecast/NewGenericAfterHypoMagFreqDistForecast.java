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
public class NewGenericAfterHypoMagFreqDistForecast
    extends GenericAfterHypoMagFreqDistForecast {

 
	  
	// TODO Auto-generated constructor stub
public NewGenericAfterHypoMagFreqDistForecast(ObsEqkRupture mainshock,
			org.opensha.commons.data.region.GriddedRegion aftershockZone,
			double[] scaler) {
		super(mainshock, aftershockZone, scaler);
	}




/**
   * set_Gridded_kValue
   * This will taper the generic k value.  Each grid node will be assigned
   * a k value based on the distance from the fault.
   */

  public void set_Gridded_Gen_kValue() {
	   grid_Gen_kVal = new double[numGridLocs];
    //double rightSide = a_valueGeneric + b_valueGeneric *
    //    (this.mainShock.getMag() - this.genNodeCompletenessMag);
	   double generic_k = Math.pow(10,1.25 * (this.mainShock.getMag() - 5.4017))
	   	   * Math.pow(10, 1.03*(4-this.genNodeCompletenessMag))/5.1355;
    //double generic_k = Math.pow(10, rightSide);
    int numInd = kScaler.length;
    //double totK = 0;
    for (int indLoop = 0; indLoop < numInd - 1; ++indLoop) {
      grid_Gen_kVal[indLoop] = generic_k * this.kScaler[indLoop];
      // test to see if kScaler is correct
      //totK += this.kScaler[indLoop];
    }
    //System.out.println("new generic_k " +generic_k);
  }



}
