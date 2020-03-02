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

package org.opensha.sha.gui.controls;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: X_ValuesInCurveControlPanelAPI</p>
 * <p>Description: Interface to Application and XValueControlPanel. It gets the
 * IMT value from the application based on which it selects the default X Values</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface X_ValuesInCurveControlPanelAPI {

  /**
   * Get the selected IMT from the application, based on which it shows the
   * default X Values for the chosen IMT.
   * @return
   */
  public String getSelectedIMT();

  /**
   * Set the X Values from the ArbitrarilyDiscretizedFunc passed as the parameter
   * @param func
   */
  public void setX_ValuesForHazardCurve(ArbitrarilyDiscretizedFunc func);

  /**
   *Set the default X Values for the Hazard Curve for the selected IMT.
   */
  public void setX_ValuesForHazardCurve();
}
