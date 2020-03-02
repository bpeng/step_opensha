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
 * <p>Title: XY_ValuesControlPanelAPI</p>
 *
 * <p>Description: This interface provides interface to the XY_ValuesControlPanel.
 * Any application using XY_ValuesControlPanel needs to implement this interface</p>
 * @author : Nitin Gupta
 * @version 1.0
 */
public interface XY_ValuesControlPanelAPI {


    /**
     * Sets ArbitraryDiscretizedFunc inside list containing all the functions.
     * @param function ArbitrarilyDiscretizedFunc
     */
    public void setArbitraryDiscretizedFuncInList(ArbitrarilyDiscretizedFunc function);

}
