package org.opensha.gem.condor.calc.components;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.data.Site;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.util.FileUtils;
import org.opensha.gem.condor.dagGen.HazardDataSetDAGCreator;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.imr.IntensityMeasureRelationship;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.util.TectonicRegionType;

public class CalculationInputsXMLFile implements XMLSaveable {
	
	private EqkRupForecastAPI erf;
	private List<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> imrMaps;
	private List<Site> sites;
	private CalculationSettings calcSettings;
	private CurveResultsArchiver archiver;
	
	private boolean erfSerialized = false;
	private String serializedERFFile;
	
	public CalculationInputsXMLFile(EqkRupForecastAPI erf,
		List<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> imrMaps,
		List<Site> sites,
		CalculationSettings calcSettings,
		CurveResultsArchiver archiver) {
		this.erf = erf;
		this.imrMaps = imrMaps;
		this.sites = sites;
		this.calcSettings = calcSettings;
		this.archiver = archiver;
	}
	
	public EqkRupForecastAPI getERF() {
		return erf;
	}
	
	public void serializeERF(String odir) throws IOException {
		erf.updateForecast();
		FileUtils.saveObjectInFileThrow(serializedERFFile, erf);
		String serializedERFFile = odir + HazardDataSetDAGCreator.ERF_SERIALIZED_FILE_NAME;
		setSerialized(serializedERFFile);
	}
	
	public void setSerialized(String serializedERFFile) {
		erfSerialized = serializedERFFile != null;
		this.serializedERFFile = serializedERFFile;
	}

	public List<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> getIMRMaps() {
		return imrMaps;
	}

	public List<Site> getSites() {
		return sites;
	}

	public CalculationSettings getCalcSettings() {
		return calcSettings;
	}

	public CurveResultsArchiver getArchiver() {
		return archiver;
	}

	public Element toXMLMetadata(Element root) {
		if (erf instanceof EqkRupForecast) {
			EqkRupForecast newERF = (EqkRupForecast)erf;
			root = newERF.toXMLMetadata(root);
			if (erfSerialized) {
				// load the erf element from metadata
				Element erfElement = root.element(EqkRupForecast.XML_METADATA_NAME);

				// rename the old erf to ERF_REF so that the params are preserved, but it is not used for calculation
				root.add(erfElement.createCopy("ERF_REF"));
				erfElement.detach();
				
				// create new ERF element and add to root
				Element newERFElement = root.addElement(EqkRupForecast.XML_METADATA_NAME);
				newERFElement.addAttribute("fileName", serializedERFFile);
			}
		} else {
			throw new ClassCastException("Currently only EqkRupForecast subclasses can be saved" +
			" to XML.");
		}
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs =
			new ArrayList<ScalarIntensityMeasureRelationshipAPI>();
		ArrayList<Map<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> newList =
			new ArrayList<Map<TectonicRegionType,ScalarIntensityMeasureRelationshipAPI>>();
		for (HashMap<TectonicRegionType,ScalarIntensityMeasureRelationshipAPI> map : imrMaps) {
			newList.add(map);
			for (TectonicRegionType tect : map.keySet()) {
				ScalarIntensityMeasureRelationshipAPI imr = map.get(tect);
				boolean add = true;
				for (ScalarIntensityMeasureRelationshipAPI newIMR : imrs) {
					if (newIMR.getShortName().equals(imr.getShortName())) {
						add = false;
						break;
					}
				}
				if (add)
					imrs.add(imr);
			}
		}
		imrsToXML(imrs, root);
		imrMapsToXML(newList, root);
		Site.writeSitesToXML(sites, root);
		calcSettings.toXMLMetadata(root);
		archiver.toXMLMetadata(root);
		return null;
	}
	
	public static CalculationInputsXMLFile loadXML(Document doc) throws InvocationTargetException, IOException {
		Element root = doc.getRootElement();
		
		/* Load the ERF 							*/
		EqkRupForecastAPI erf;
		Element erfElement = root.element(EqkRupForecast.XML_METADATA_NAME);
		Attribute className = erfElement.attribute("className");
		if (className == null) { // load it from a file
			String erfFileName = erfElement.attribute("fileName").getValue();
			erf = (EqkRupForecast)FileUtils.loadObject(erfFileName);
		} else {
			erf = EqkRupForecast.fromXMLMetadata(erfElement);
			System.out.println("Updating Forecast");
			erf.updateForecast();
		}
		
		/* Load the IMRs							*/
		Element imrsEl = root.element(XML_IMRS_NAME);
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs =imrsFromXML(imrsEl);
		ArrayList<ParameterAPI> paramsToAdd = new ArrayList<ParameterAPI>();
		for (ScalarIntensityMeasureRelationshipAPI imr : imrs) {
			ListIterator<ParameterAPI> it = imr.getSiteParamsIterator();
			while (it.hasNext()) {
				ParameterAPI param = it.next();
				boolean add = true;
				for (ParameterAPI prevParam : paramsToAdd) {
					if (param.getName().equals(prevParam.getName())) {
						add = false;
						break;
					}
				}
				if (add)
					paramsToAdd.add(param);
			}
		}
		
		/* Load the IMR Maps						*/
		Element imrMapsEl = root.element(XML_IMR_MAP_LIST_NAME);
		ArrayList<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> imrMaps =
			imrMapsFromXML(imrs, imrMapsEl);
		
		/* Load the sites 							*/
		Element sitesEl = root.element(Site.XML_METADATA_LIST_NAME);
		ArrayList<Site> sites = Site.loadSitesFromXML(sitesEl, paramsToAdd);
		
		/* Load Curve Archiver						*/
		Element archiverEl = root.element(AsciiFileCurveArchiver.XML_METADATA_NAME);
		CurveResultsArchiver archiver = AsciiFileCurveArchiver.fromXMLMetadata(archiverEl);
		
		/* Load calc settings						*/
		Element calcSettingsEl = root.element(CalculationSettings.XML_METADATA_NAME);
		CalculationSettings calcSettings = CalculationSettings.fromXMLMetadata(calcSettingsEl);
		
		return new CalculationInputsXMLFile(erf, imrMaps, sites, calcSettings, archiver);
	}
	
	public static final String XML_IMRS_NAME = "IMRs";
	
	public static Element imrsToXML(ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs,
			Element root) {
		Element imrsEl = root.addElement(XML_IMRS_NAME);
		
		for (ScalarIntensityMeasureRelationshipAPI imr : imrs) {
			if (imr instanceof IntensityMeasureRelationship) {
				IntensityMeasureRelationship attenRel = (IntensityMeasureRelationship)imr;
				attenRel.toXMLMetadata(imrsEl);
			} else {
				throw new ClassCastException("Currently only IntensityMeasureRelationship subclasses can be saved" +
						" to XML.");
			}
		}
		
		return root;
	}
	
	public static ArrayList<ScalarIntensityMeasureRelationshipAPI> imrsFromXML(Element imrsEl) throws InvocationTargetException {
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs =
			new ArrayList<ScalarIntensityMeasureRelationshipAPI>();
		
		Iterator<Element> it = imrsEl.elementIterator();
		while (it.hasNext()) {
			Element imrEl = it.next();
			
			ScalarIntensityMeasureRelationshipAPI imr = 
				(ScalarIntensityMeasureRelationshipAPI) IntensityMeasureRelationship.fromXMLMetadata(imrEl, null);
			imrs.add(imr);
		}
		
		return imrs;
	}
	
	public static final String XML_IMR_MAP_NAME = "IMR_Map";
	public static final String XML_IMR_MAPING_NAME = "IMR_Maping";
	
	public static Element imrMapToXML(Map<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> map,
			Element root) {
		Element mapEl = root.addElement(XML_IMR_MAP_NAME);
		
		for (TectonicRegionType tect : map.keySet()) {
			Element mapingEl = mapEl.addElement(XML_IMR_MAPING_NAME);
			ScalarIntensityMeasureRelationshipAPI imr = map.get(tect);
			mapingEl.addAttribute("tectonicRegionType", tect.toString());
			mapingEl.addAttribute("imr", imr.getShortName());
		}
		
		return root;
	}
	
	public static HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMapFromXML(
			ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs, Element imrMapEl) {
		HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> map =
			new HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>();
		
		Iterator<Element> it = imrMapEl.elementIterator(XML_IMR_MAPING_NAME);
		
		while (it.hasNext()) {
			Element mappingEl = it.next();
			
			String tectName = mappingEl.attributeValue("tectonicRegionType");
			String imrName = mappingEl.attributeValue("imr");
			
			TectonicRegionType tect = TectonicRegionType.getTypeForName(tectName);
			ScalarIntensityMeasureRelationshipAPI imr = null;
			for (ScalarIntensityMeasureRelationshipAPI testIMR : imrs) {
				if (imrName.equals(testIMR.getShortName())) {
					imr = testIMR;
					break;
				}
			}
			if (imr == null)
				throw new RuntimeException("IMR '" + imrName + "' not found in XML mapping lookup");
			map.put(tect, imr);
		}
		
		return map;
	}
	
	public static final String XML_IMR_MAP_LIST_NAME = "IMR_Maps";
	
	public static Element imrMapsToXML(
			ArrayList<Map<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> maps,
			Element root) {
		Element mapsEl = root.addElement(XML_IMR_MAP_LIST_NAME);
		
		for (Map<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> map : maps) {
			mapsEl = imrMapToXML(map, mapsEl);
		}
		
		return root;
	}
	
	public static ArrayList<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> imrMapsFromXML(
			ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs,
			Element imrMapsEl) {
		ArrayList<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> maps =
			new ArrayList<HashMap<TectonicRegionType,ScalarIntensityMeasureRelationshipAPI>>();
		
		Iterator<Element> it = imrMapsEl.elementIterator(XML_IMR_MAP_NAME);
		
		while (it.hasNext()) {
			Element imrMapEl = it.next();
			maps.add(imrMapFromXML(imrs, imrMapEl));
		}
		
		return maps;
	}

}
