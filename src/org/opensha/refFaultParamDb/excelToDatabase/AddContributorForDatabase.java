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

package org.opensha.refFaultParamDb.excelToDatabase;

import org.opensha.refFaultParamDb.dao.db.ContributorDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;

/**
 * <p>Title: AddContributorForDatabase.java </p>
 * <p>Description: Add a new contributor to the database </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddContributorForDatabase {
  private final static DB_AccessAPI dbConn= new DB_ConnectionPool();

  public static void main(String[] args) {
    ContributorDB_DAO contributorDAO = new ContributorDB_DAO(dbConn);
    // add fault _sandbox
    /*Contributor contributor = new Contributor();
    contributor.setEmail("fault_sandbox@usc.edu");
    contributor.setFirstName("Fault");
    contributor.setLastName("Sandbox");
    contributor.setName("fault_sandbox");
    String password = "perry";
    contributorDAO.addContributor(contributor,password);
    // add vgupta
    contributor = new Contributor();
    contributor.setEmail("vgupta@usc.edu");
    contributor.setFirstName("Vipin");
    contributor.setLastName("Gupta");
    contributor.setName("vgupta");
    password = "vgupta";
    contributorDAO.addContributor(contributor,password);
    //System.exit(0);
    contributor = new Contributor();
    contributor.setEmail("perry@gps.caltech.edu");
    contributor.setFirstName("Sue");
    contributor.setLastName("Perry");
    contributor.setName("perry");
    password = "perry";
    contributorDAO.addContributor(contributor,password);
    Contributor contributor = new Contributor();
    contributor.setEmail("Chris.Wills@conservation.ca.gov");
    contributor.setFirstName("Chris");
    contributor.setLastName("Wills");
    contributor.setName("chris.wills");
    String password = "uxy4tuz";
    contributorDAO.addContributor(contributor,password);*/
    System.exit(0);
  }

}
