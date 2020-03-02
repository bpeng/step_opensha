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

package org.opensha.sha.calc.hazardMap;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.opensha.commons.gridComputing.GridJob;
import org.opensha.commons.gridComputing.GridResources;

public class HazardMapJob extends GridJob {

	public HazardMapJob(GridResources resources, HazardMapCalculationParameters calcParams, String jobID, String jobName,
			String email, String configFileName) {
		super(resources, calcParams, jobID, jobName, email, configFileName);
	}
	
	@Override
	public HazardMapCalculationParameters getCalcParams() {
		return (HazardMapCalculationParameters)calcParams;
	}

	public static HazardMapJob fromXMLMetadata(Element jobElem) {
		GridResources resources = GridResources.fromXMLMetadata(jobElem.element(GridResources.XML_METADATA_NAME));
		HazardMapCalculationParameters resourceProvider = new HazardMapCalculationParameters(jobElem);
		
		String jobID = jobElem.attributeValue("jobID");
		Attribute jobNameAtt = jobElem.attribute("jobName");
		String jobName;
		if (jobNameAtt == null)
			jobName = jobID;
		else
			jobName = jobNameAtt.getValue();
		String email = jobElem.attributeValue("email");
		String configFileName = jobElem.attributeValue("configFileName");
		
		return new HazardMapJob(resources, resourceProvider, jobID, jobName, email, configFileName);
	}
}
