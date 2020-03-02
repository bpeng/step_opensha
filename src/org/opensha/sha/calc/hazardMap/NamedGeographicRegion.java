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

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.NamedObjectAPI;
import org.opensha.commons.data.region.BorderType;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.region.Region;
import org.opensha.commons.util.XMLUtils;

// TODO this is in an odd location and shouldn't necessarily exits.
// Region or GriddedRegion should implement Named interface
public class NamedGeographicRegion extends Region implements NamedObjectAPI {

	private String name;

	public NamedGeographicRegion(LocationList outline, String name) {
		super(outline, BorderType.MERCATOR_LINEAR);

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Element toXMLMetadata(Element root) {
		Region region = new Region(this.getBorder(), BorderType.MERCATOR_LINEAR);
		
		root = region.toXMLMetadata(root);
		
		Element regionEl = root.element(XML_METADATA_NAME);
		
		regionEl.addAttribute("name", name);

		return root;
	}

	public static NamedGeographicRegion fromXMLMetadata(Element geographicElement) {
		Region region = Region.fromXMLMetadata(geographicElement);
		
		String name = geographicElement.attributeValue("name");
		
		return new NamedGeographicRegion(region.getBorder(), name);
	}
	
	public static void main(String args[]) {
		NamedGeographicRegion region = new NamedGeographicRegion(
				new CaliforniaRegions.RELM_TESTING().getBorder(), "Relm!");
		Document doc = XMLUtils.createDocumentWithRoot();
		Element root = doc.getRootElement();
		region.toXMLMetadata(root);
		try {
		XMLUtils.writeDocumentToFile("region.xml", doc);
		} catch( IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
