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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.data.estimate.Estimate;
import org.opensha.commons.data.estimate.MinMaxPrefEstimate;
import org.opensha.refFaultParamDb.dao.db.CombinedEventsInfoDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PaleoSiteDB_DAO;
import org.opensha.refFaultParamDb.dao.db.ReferenceDB_DAO;
import org.opensha.refFaultParamDb.data.ExactTime;
import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.refFaultParamDb.data.TimeEstimate;
import org.opensha.refFaultParamDb.gui.addEdit.paleoSite.AddEditCumDisplacement;
import org.opensha.refFaultParamDb.gui.addEdit.paleoSite.AddEditNumEvents;
import org.opensha.refFaultParamDb.gui.addEdit.paleoSite.AddEditSlipRate;
import org.opensha.refFaultParamDb.vo.CombinedDisplacementInfo;
import org.opensha.refFaultParamDb.vo.CombinedEventsInfo;
import org.opensha.refFaultParamDb.vo.CombinedNumEventsInfo;
import org.opensha.refFaultParamDb.vo.CombinedSlipRateInfo;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.vo.PaleoSitePublication;
import org.opensha.refFaultParamDb.vo.Reference;

/**
 * 
 * @author vipingupta
 *
 */
public class PutPetrrizzoBirdDataIntoDatabase {
	 private final static String FILE_NAME = "org/opensha/refFaultParamDb/excelToDatabase/rev_Petrizzo_ingest_4_Vipin.xls";
	  // rows (number of records) in this excel file. First 3 rows are neglected as they have header info
	  private final static int MIN_ROW = 3;
	  private final static int MAX_ROW = 194;
	  //private final static int MIN_ROW = 74;
	  //private final static int MAX_ROW = 74;
	  // columns in this excel file
	  private final static int MIN_COL = 0;
	  private final static int MAX_COL = 61;
	  private PaleoSiteDB_DAO paleoSiteDAO = new PaleoSiteDB_DAO(DB_AccessAPI.dbConnection);
	  private ReferenceDB_DAO referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);
	  private FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	  private CombinedEventsInfoDB_DAO combinedEventsInfoDAO = new CombinedEventsInfoDB_DAO(DB_AccessAPI.dbConnection);
	  private final static String UNKNOWN = "Unknown";
	  private final static String MA = "MA";
	  private final static String KA = "ka";
	  private final static int ZERO_YEAR=1950;
	  private String measuredComponent, senseOfMotion;
	  private CombinedDisplacementInfo combinedDispInfo;
	  private CombinedSlipRateInfo combinedSlipRateInfo;
	  private CombinedNumEventsInfo combinedNumEventsInfo;
	  private boolean isDisp, isSlipRate, isNumEvents;
	  private double min, max, pref;
	  private String refShortCitation, refFullCitation;
	  private TimeAPI startTime, endTime;
	  private String startTimeUnits, endTimeUnits;
	  private final static String NO = "no";
	  private final static String BETWEEN_LOCATIONS_SITE_TYPE = "Between Locations";
	  
	  
	  /*
	   * This hashmap is needed to keep track of already done sites. The excel spreadsheet has multiple rows where each 
	   * row has a combined event info. 
	   * 
	   */
	  private HashMap doneSitesMap = new HashMap(); 

	  public PutPetrrizzoBirdDataIntoDatabase() {
	    try {
	      // read the excel file
	      POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(FILE_NAME));
	      HSSFWorkbook wb = new HSSFWorkbook(fs);
	      HSSFSheet sheet = wb.getSheetAt(0);
	      // read data for each row
	      for(int r = MIN_ROW; r<=MAX_ROW; ++r) {
	        System.out.println("Processing Row:"+(r+1));
	        HSSFRow row = sheet.getRow(r);
	        
	        // in case new paleo site needs to be entered into database
	        PaleoSite paleoSite = new PaleoSite();
	        // combined event info for this site
	        CombinedEventsInfo combinedEventsInfo = new CombinedEventsInfo();
	        // paleo site publication
	        PaleoSitePublication paleoSitePub = new PaleoSitePublication();
	        combinedEventsInfo.setPaleoSitePublication(paleoSitePub);
	        // set publication in paleo site
	        ArrayList pubList = new ArrayList();
	        pubList.add(paleoSitePub);
	        paleoSite.setPaleoSitePubList(pubList);
	        // site types
	        ArrayList siteTypeNames = new ArrayList();
	        siteTypeNames.add(UNKNOWN);
	        paleoSitePub.setSiteTypeNames(siteTypeNames);


	        // make objects of displacement, slip rate as well as num events
	        combinedDispInfo = new CombinedDisplacementInfo();
	        combinedSlipRateInfo = new CombinedSlipRateInfo();
	        combinedNumEventsInfo = new CombinedNumEventsInfo();
	        isDisp=false;
	        isSlipRate=false;
	        isNumEvents=false;

	        // start time and end time
	        startTime = new TimeEstimate();
	        endTime = new TimeEstimate();
	        try {
	          // get value of each column in the row
	          for (int c = MIN_COL; c <= MAX_COL; ++c) {
	            HSSFCell cell = row.getCell( (short) c);
	            String value = null;
	            if (cell != null &&
	                ! (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)) {
	              if(cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
	                value = cell.getStringCellValue().trim();
	              else value = ""+cell.getNumericCellValue();
	            }
	            process(c, value, paleoSite, combinedEventsInfo, paleoSitePub);
	          }
	        }catch(InvalidRowException e) {
	          System.out.println("Row "+(r+1)+":"+e.getMessage());
	          continue;
	        }catch(RuntimeException ex) {
	          ex.printStackTrace();
	          //continue;
	          System.exit(0);
	        }

	        // set the start and end time
	        combinedEventsInfo.setStartTime(this.startTime);
	        combinedEventsInfo.setEndTime(this.endTime);
	        ArrayList refList = new ArrayList();
	        refList.add(paleoSitePub.getReference());
	        combinedEventsInfo.setReferenceList(refList);

	        // set displacement in combined events info
	        if(isDisp) {
	          combinedDispInfo.setSenseOfMotionQual(this.senseOfMotion);
	          combinedDispInfo.setMeasuredComponentQual(this.measuredComponent);
	          combinedEventsInfo.setCombinedDisplacementInfo(combinedDispInfo);
	        }

	        // set slip rate in combined events info
	        if(isSlipRate) {
	          combinedSlipRateInfo.setSenseOfMotionQual(this.senseOfMotion);
	          combinedSlipRateInfo.setMeasuredComponentQual(this.measuredComponent);
	          combinedEventsInfo.setCombinedSlipRateInfo(combinedSlipRateInfo);
	        }

	        // set num events in combined events info
	        if(isNumEvents) combinedEventsInfo.setCombinedNumEventsInfo(combinedNumEventsInfo);

	        // site in DB
	        PaleoSite siteInDB = isSiteInCache(paleoSite.getSiteName(), paleoSite.getOldSiteId(), combinedEventsInfo.getNeokinemaFaultNumber());
	      
	        //boolean isSiteInDB
	        if(siteInDB==null) { // site does not exist in database
	          paleoSiteDAO.addPaleoSite(paleoSite);
	          putSiteInCache(paleoSite, combinedEventsInfo.getNeokinemaFaultNumber());
	          siteInDB = paleoSite;
	          Thread.sleep(1000);
	          //siteInDB = paleoSiteDAO.getPaleoSite(paleoSite.getSiteName());
	        }
	        paleoSitePub.setSiteEntryDate(siteInDB.getEntryDate());
	        paleoSitePub.setSiteId(siteInDB.getSiteId());
	        combinedEventsInfo.setIsExpertOpinion(false);
	        combinedEventsInfo.setSiteId(siteInDB.getSiteId());
	        combinedEventsInfo.setSiteEntryDate(siteInDB.getEntryDate());

	        // add combined events info to database
	         combinedEventsInfoDAO.addCombinedEventsInfo(combinedEventsInfo);
	         Thread.sleep(500);
	         
	      }
	    }catch(Exception e) {
	      e.printStackTrace();
	    }
	  }
	  
	  /**
	   * Check whether we have already put the PaleoSite data fr this site into PaleoSite table
	   * @param paleoSite
	   * @return
	   */
	  private PaleoSite isSiteInCache(String siteName, String qFaultSiteIdtSiteId,  String neoKinemaFaultNumber) {
		  String key=null;
		  if(qFaultSiteIdtSiteId!=null) key = qFaultSiteIdtSiteId;
		  else if(siteName!=null) key = siteName;
		  else key = neoKinemaFaultNumber;
		  PaleoSite paleoSite1  = (PaleoSite)this.doneSitesMap.get(key);
		  return paleoSite1;
	  }
	  
	  private void putSiteInCache(PaleoSite paleoSite, String neoKinemaFaultNumber) {
		  String qFaultSiteIdtSiteId = paleoSite.getOldSiteId();
		  String siteName = paleoSite.getSiteName();
		  String key = null;
		  if(qFaultSiteIdtSiteId!=null) key = qFaultSiteIdtSiteId;
		  else if(siteName!=null) key = siteName;
		  else key = neoKinemaFaultNumber;
		  doneSitesMap.put(key, paleoSite);
	  }
	  

	  /**
	   * Process the excel sheet according to the specific column number
	   *
	   * @param columnNumber
	   * @param value
	   * @param paleoSite
	   * @param combinedEventsInfo
	   */
	  private void process(int columnNumber, String value, PaleoSite paleoSite,
	                       CombinedEventsInfo combinedEventsInfo,
	                       PaleoSitePublication paleoSitePub) {
	    switch (columnNumber) {
	      case 0:
	    	  if(value!=null && value.equalsIgnoreCase(NO)) throw new InvalidRowException("No need to put into database as ingest=no");
	    	  break;
	      case 3: //  NEO-KINEMA FAULT ID
	    	  
	    	  combinedEventsInfo.setNeokinemaFaultNumber(value);
	    	  combinedEventsInfo.setDataSource("Peter Bird"); 
	    	  break;
	      case 5:
	    	  // combine the neo-kinema fault Id and reference type
	    	  combinedEventsInfo.setNeokinemaFaultNumber(combinedEventsInfo.getNeokinemaFaultNumber()+"-"+value);
	    	  break;
	      case 6: // WG Fault section Id
	    	  	//int faultSectionId = (int)Double.parseDouble(value);
	      	//FaultSectionSummary faultSectionSummary= faultSectionDAO.getFaultSectionSummary(faultSectionId);
	        //System.out.println(faultSectionId);
	      	//paleoSite.setFaultSectionNameId(faultSectionSummary.getSectionName(), faultSectionSummary.getSectionId());
	        break;
	      case 9: // qfault Site-Id
	        paleoSite.setOldSiteId(value);
	        break;
	      case 12: // site name
	        // if site name starts with "per", then we will set its name as lat,lon
	        if(value==null || value.startsWith("per")) value="";
	        paleoSite.setSiteName(value);
	        break;
	      case 14: // Site longitude 1
	        if(value==null) throw new InvalidRowException("Site Longitude is missing");
	        paleoSite.setSiteLon1(Float.parseFloat(value));
	        paleoSite.setSiteLon2(Float.NaN);
	        break;
	      case 15: // Site latitude 1
	        if(value==null) throw new InvalidRowException("Site latitude is missing");
	        paleoSite.setSiteLat1(Float.parseFloat(value));
	        paleoSite.setSiteLat2(Float.NaN);
	        break;
	      case 16: // site Elevation 1
	    	  if(value!=null && !value.equals("")) paleoSite.setSiteElevation1(Float.parseFloat(value)); 
	    	  break;
	      case 17: // Site longitude 2
	    	  if(value!=null && !value.equals(""))  paleoSite.setSiteLon2(Float.parseFloat(value));
	          break;
	          
	      case 18:
	    	  if(value!=null && !value.equals(""))   { // Site Lat2
	    		  paleoSite.setSiteLat2(Float.parseFloat(value));
	    		  ArrayList siteTypeNames = paleoSitePub.getSiteTypeNames();
	    		  siteTypeNames.clear();
	    		  siteTypeNames.add(BETWEEN_LOCATIONS_SITE_TYPE);
	    		  /*if(paleoSite.getSiteName().equalsIgnoreCase(""))
	    	          paleoSite.setSiteName(GUI_Utils.latFormat.format(paleoSite.getSiteLat1())+","+
	    	        		  GUI_Utils.lonFormat.format(paleoSite.getSiteLon1())+";"+
	    	        		  GUI_Utils.latFormat.format(paleoSite.getSiteLat2())+","+
	    	        		  GUI_Utils.lonFormat.format(paleoSite.getSiteLon2()));*/
	    	  }
	    	 /* else {
	    		  if(paleoSite.getSiteName().equalsIgnoreCase(""))
	    	          paleoSite.setSiteName(GUI_Utils.latFormat.format(paleoSite.getSiteLat1())+","+
	    	        		  GUI_Utils.lonFormat.format(paleoSite.getSiteLon1()));
	    	  }*/
	          break;
	      case 19: 
	    	   // site elevation 2
	    	  if(value!=null && !value.equals("")) {
	    		  int index = value.indexOf("+");
	    		  if(index>0) value = value.substring(0, index);
	    		  paleoSite.setSiteElevation2(Float.parseFloat(value));
	    	  }
	    	  break;
	      case 24: // site notes
	    	  String generalComments = paleoSite.getGeneralComments();
	    	  if(generalComments==null) generalComments="";
	    	  if(value!=null) generalComments+=value+"\n";
	    	  paleoSite.setGeneralComments(generalComments);
	    	  
	      case 25: // reference summary
	        this.refShortCitation = value;
	        if(paleoSite.getSiteName().equalsIgnoreCase(""));
	        	paleoSite.setSiteName("Per "+refShortCitation);
	        break;
	      case 26: // reference full citation
	    	  this.refFullCitation = value;
	    	  break;
	      case 27: // reference Id in qfaults
	        if(value!=null && !value.equals("")) paleoSitePub.setReference(referenceDAO.getReferenceByQfaultId((int)Double.parseDouble(value)));
	        else {
	        	// get reference from database.
	        	Reference ref = this.getReference(refShortCitation, refFullCitation);
	        	Reference refFromDB = this.referenceDAO.getReference(ref.getRefAuth(), ref.getRefYear());
	        	if(refFromDB==null) paleoSitePub.setReference(addReferenceToDatabase(refShortCitation, refFullCitation));
	        	else paleoSitePub.setReference(refFromDB);
	        }
	        break;
	      case 28: // combined info comments
	        if(value==null) value="";
	        combinedEventsInfo.setDatedFeatureComments(value);
	        break;
	      case 30: // representative strand name 	  
	        if(value==null) value = UNKNOWN;
	        else {
	        	int repStrandIndex = Integer.parseInt(value);
	        	if(repStrandIndex==4) value = UNKNOWN;
	        	else if(repStrandIndex==3) value = "One of Several Strands";
	        }
	        paleoSitePub.setRepresentativeStrandName(value);
	        break;
	      case 31: // measured component
	        if(value==null || value.equals("")) value=UNKNOWN;
	        if(value.equalsIgnoreCase("A")) measuredComponent="Total";
			else if(value.equalsIgnoreCase("B")) measuredComponent="Vertical";
			else if(value.equalsIgnoreCase("C")) measuredComponent="Horizontal,Trace-Parallel";
			else if(value.equalsIgnoreCase("D")) measuredComponent="Horizontal,Trace-NORMAL";
	        break;
	      case 32: // sense of motion
	        //if(value==null) value=UNKNOWN;
	        this.senseOfMotion = value;
	        break;
	     /* case 17: //aseismic slip factor for displacement
	         if(value!=null) {
	           Estimate estimate = new MinMaxPrefEstimate(Double.NaN,Double.NaN,Double.parseDouble(value),Double.NaN, Double.NaN, Double.NaN);
	           combinedDispInfo.setASeismicSlipFactorEstimateForDisp(new EstimateInstances(estimate, AddEditCumDisplacement.ASEISMIC_SLIP_FACTOR_UNITS));
	         }
	         break;*/
	      case 33: // preferred displacement
	        if(value==null || value.equals("")) this.pref = Double.NaN;
	        else {
	          this.isDisp = true;
	          this.pref = Double.parseDouble(value);
	        }
	        break;
	      case 34: // No need to migrate (offset error)
	        break;
	      case 35: // min displacement
	        if(value==null || value.equals("")) this.min = Double.NaN;
	        else {
	          this.isDisp = true;
	          this.min = Double.parseDouble(value);
	        }
	        break;
	      case 36: // max displacement
	        if(value==null || value.equals("")) this.max = Double.NaN;
	        else {
	          this.isDisp = true;
	          this.max = Double.parseDouble(value);
	        }
	        if(isDisp) {
	          Estimate estimate = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
	          combinedDispInfo.setDisplacementEstimate(new EstimateInstances(estimate, AddEditCumDisplacement.CUMULATIVE_DISPLACEMENT_UNITS));
	        }
	        break;
	      case 37: // diplacement comments
	        if(value==null || value.equals("")) value="";
	        combinedDispInfo.setDisplacementComments(value);
	        break;
	      case 38 : // preferred num events
	        if(value==null || value.equals("")) this.pref = Double.NaN;
	        else {
	          this.isNumEvents = true;
	          this.pref = Double.parseDouble(value);
	        }
	        break;
	      case 39 : //min num events
	        if(value==null || value.equals("")) this.min = Double.NaN;
	        else {
	          this.isNumEvents = true;
	          this.min = Double.parseDouble(value);
	        }
	        break;
	      case 40: // max num events
	        if(value==null || value.equals("")) this.max = Double.NaN;
	        else {
	          this.isNumEvents = true;
	          this.max = Double.parseDouble(value);
	        }
	        if(isNumEvents) {
	          Estimate estimate = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
	          this.combinedNumEventsInfo.setNumEventsEstimate(new EstimateInstances(estimate, AddEditNumEvents.NUM_EVENTS_UNITS));
	        }
	        break;
	      case 41: // num events comments
	        if(value==null || value.equals("")) value="";
	        this.combinedNumEventsInfo.setNumEventsComments(value);
	        break;
	      case 42: // timespan comments
	        if(value==null || value.equals("")) value="";
	        combinedEventsInfo.setDatedFeatureComments(combinedEventsInfo.getDatedFeatureComments()+"\n"+value);
	        break;
	      case 43: // preferred start time
	        if(value==null || value.equals("")) this.pref = Double.NaN;
	        else pref = Double.parseDouble(value);
	        break;
	      case 44:  // start time units
	        if(value!=null && !value.equals("")) startTimeUnits = value;
	        else startTimeUnits="";
	        break;
	      case 45: // No need to migrate (start time error)
	        break;
	      case 46: // max start time
	        if(value==null || value.equals("")) this.max = Double.NaN;
	        else max = Double.parseDouble(value);
	        break;
	      case 47: // min start time
	        if(value==null || value.equals("")) this.min = Double.NaN;
	        else min = Double.parseDouble(value);
	        if(Double.isNaN(min) && Double.isNaN(max) && Double.isNaN(pref)) {
	        	startTime = null;
	        	break;
	        }
	        if(startTimeUnits.equalsIgnoreCase("")) throw new InvalidRowException("Start Time units are missing");
	         // if units are MA
	        if(startTimeUnits.equalsIgnoreCase(MA)) {
	          min = min*1000;
	          max=max*1000;
	          pref=pref*1000;
	          startTimeUnits = KA;
	        }
	        // swap min/max in case of AD/BC
	       /* if(!startTimeUnits.equalsIgnoreCase(KA)) {
	          double temp = min;
	          min=max;
	          max=temp;
	        }*/

	        // set the start time
	        Estimate est = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
	        if(!startTimeUnits.equalsIgnoreCase(TimeAPI.BC) && !startTimeUnits.equalsIgnoreCase(KA))  startTimeUnits = TimeAPI.AD;
	        if(startTimeUnits.equalsIgnoreCase(KA))
	          ((TimeEstimate)startTime).setForKaUnits(est, ZERO_YEAR);
	        else ((TimeEstimate)startTime).setForCalendarYear(est, startTimeUnits);

	        // set reference in start time
	        ArrayList refList = new ArrayList();
	        refList.add(paleoSitePub.getReference());
	        startTime.setReferencesList(refList);
	        break;
	      case 48: // max end time
	        if(value==null || value.equals("")) this.max = Double.NaN;
	        else max = Double.parseDouble(value);
	        break;
	      case 49: // pref end time
	        if(value==null || value.equals("")) this.pref = Double.NaN;
	        else pref = Double.parseDouble(value);
	        break;
	      case 50: // min end time
	        if(value==null || value.equals("")) this.min = Double.NaN;
	        else min  = Double.parseDouble(value);
	        break;
	      case 51: // end time units
	        //if(value!=null) endTimeUnits = value;
	        /*else*/   endTimeUnits=this.startTimeUnits;
	        if(startTime==null) {
	        	endTime = null;
	        	break;
	        }
	        if(Double.isNaN(min) && Double.isNaN(max) && Double.isNaN(pref))
	          endTime = new ExactTime(Integer.parseInt(paleoSitePub.getReference().getRefYear()), 0, 0, 0, 0, 0, TimeAPI.AD, true);
	        else {
	          if(endTimeUnits.equalsIgnoreCase("")) throw new InvalidRowException("End Time units are missing");
	          // if units are MA
	          if(endTimeUnits.equalsIgnoreCase(MA)) {
	            min = min*1000;
	            max=max*1000;
	            pref=pref*1000;
	            endTimeUnits = KA;
	          }
	          //System.out.println(min+","+max+","+pref+","+endTimeUnits+","+this.startTimeUnits);
	          // swap min/max in case of AD/BC
	          /*if(!endTimeUnits.equalsIgnoreCase(KA)) {
	            double temp = min;
	            min=max;
	            max=temp;
	          }*/
	          //System.out.println(min+","+max+","+pref);

	          // set the end time
	          Estimate endTimeEst = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
	          if(!endTimeUnits.equalsIgnoreCase(TimeAPI.BC))  endTimeUnits = TimeAPI.AD;
	          if(endTimeUnits.equalsIgnoreCase(KA))
	            ((TimeEstimate)endTime).setForKaUnits(endTimeEst, ZERO_YEAR);
	          else ((TimeEstimate)endTime).setForCalendarYear(endTimeEst, endTimeUnits);
	        }
	        // set reference in start time
	        ArrayList refList1 = new ArrayList();
	        refList1.add(paleoSitePub.getReference());
	        endTime.setReferencesList(refList1);
	        break;
	      case 52: // dated feature comments
	        if(value==null || value.equals("")) value ="";
	        combinedEventsInfo.setDatedFeatureComments(combinedEventsInfo.getDatedFeatureComments()+"\n"+value);
	        break;
	      case 53:   // MIN aseismic slip factor for Slip Rate
	    	  if(value==null || value.equals("")) this.min = Double.NaN;
	    	  else this.min = Double.parseDouble(value);
	    	  break;
	      case 54:   // MAX aseismic slip factor for Slip Rate
	    	  if(value==null || value.equals("")) this.max = Double.NaN;
	    	  else this.max = Double.parseDouble(value);
	    	  break;
	      case 55:   // PREF aseismic slip factor for Slip Rate
	    	  if(value==null || value.equals("")) this.pref = Double.NaN;
	    	  else this.pref = Double.parseDouble(value);
	    	  if(!Double.isNaN(min) || !Double.isNaN(max) || !Double.isNaN(pref)) {
	    		  Estimate estimate = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
	    		  combinedSlipRateInfo.setASeismicSlipFactorEstimateForSlip(new EstimateInstances(estimate, AddEditSlipRate.ASEISMIC_SLIP_FACTOR_UNITS));
	    	  }
	    	  break;
	      case 56: // preferred slip rate
	        if(value==null || value.equals("")) this.pref = Double.NaN;
	        else {
	          this.isSlipRate = true;
	          this.pref = Double.parseDouble(value);
	        }
	        break;
	      case 57: // no need to migrate (slip rate error)
	        break;
	      case 58: // min slip rate
	        if(value==null || value.equals("")) this.min = Double.NaN;
	        else {
	          this.isSlipRate = true;
	          this.min = Double.parseDouble(value);
	        }
	        break;
	      case 59: // max slip rate
	        if(value==null || value.equals("")) this.max = Double.NaN;
	        else {
	          this.isSlipRate = true;
	          this.max = Double.parseDouble(value);
	        }
	        if(isSlipRate) {
	         Estimate estimate = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
	         this.combinedSlipRateInfo.setSlipRateEstimate(new EstimateInstances(estimate, AddEditSlipRate.SLIP_RATE_UNITS));
	       }
	        break;
	      case 60: // slip rate comments
	        if(value==null || value.equals("")) value="";
	        this.combinedSlipRateInfo.setSlipRateComments(value);
	        break;
	      case 61:
	    	 if(value!=null)
	    		 paleoSite.setGeneralComments(paleoSite.getGeneralComments()+"\n"+value);
	    	   
	    }
	  }

	  /**
	   * Add reference to the database
	   *
	   * @param referenceSummary
	   * @return
	   */
	  private Reference addReferenceToDatabase(String referenceSummary, String refFullCitation) {
	    Reference ref = getReference(referenceSummary, refFullCitation);
	    int id = this.referenceDAO.addReference(ref);
	    //int id=-1;
	    ref.setReferenceId(id);
	    return ref;
	  }

	private Reference getReference(String referenceSummary, String refFullCitation) {
		Reference ref = new Reference();
	    ref.setFullBiblioReference("");
	    int index = referenceSummary.indexOf("(");
	    ref.setRefAuth(referenceSummary.substring(0,index));
	    ref.setRefYear(referenceSummary.substring(index+1,referenceSummary.indexOf(")")));
	    ref.setFullBiblioReference(refFullCitation);
		return ref;
	}
	
	
}

 