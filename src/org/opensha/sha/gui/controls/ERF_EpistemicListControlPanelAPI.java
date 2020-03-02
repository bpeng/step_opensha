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

/**
 * <p>Title:ERF_EpistemicListControlPanelAPI </p>
 * <p>Description: Any applet which uses the ERF_EpistemicListControlPanel needs
 * to implement this API</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface ERF_EpistemicListControlPanelAPI {


  /**
   * This function sets whether all curves are to drawn or only fractiles are to drawn
   * @param drawAllCurves :True if all curves are to be drawn else false
   */
  public void setPlotAllCurves(boolean drawAllCurves);


  /**
   * This function sets the percentils option chosen by the user.
   * User can choose "No Fractiles", "5th, 50th and 95th Fractile" or
   * "Plot Fractile"
   *
   * @param fractileOption : Oprion selected by the user. It can be set by
   * various constant String values in ERF_EpistemicListControlPanel
   */
  public void setFractileOption(String fractileOption);


  /**
   * This function is needed to tell the applet whether avg is selected or not
   *
   * @param isAvgSelected : true if avg is selected else false
   */
  public void setAverageSelected(boolean isAvgSelected);


}
