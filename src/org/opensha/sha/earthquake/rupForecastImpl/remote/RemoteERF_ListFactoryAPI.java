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

package org.opensha.sha.earthquake.rupForecastImpl.remote;

import java.rmi.Remote;

/**
 * <p>Title: RemoteERF_ListFactoryAPI </p>
 * <p>Description: This will act as the factory for giving the ERF List references to
 * the client. After getting the ERF List reference, client can interact with the
 * ERF List as usual.</p>
 * @author Nitin Gupta, Vipin Gupta
 * @date April 28, 2004
 * @version 1.0
 */

public interface RemoteERF_ListFactoryAPI extends Remote {

  /**
   * Get the reference to the remote ERF_List
   * @param className
   * @return
   * @throws java.rmi.RemoteException
   */
  public RemoteERF_ListAPI getRemoteERF_List(String className) throws java.rmi.RemoteException;

}
