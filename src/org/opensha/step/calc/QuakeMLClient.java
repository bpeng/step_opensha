package org.opensha.step.calc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.opensha.commons.data.Location;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import nz.org.geonet.quakeml.v1_0_1.client.QuakemlFactory;
import nz.org.geonet.quakeml.v1_0_1.client.QuakemlUtils;
import nz.org.geonet.quakeml.v1_0_1.domain.Event;
import nz.org.geonet.quakeml.v1_0_1.domain.Magnitude;
import nz.org.geonet.quakeml.v1_0_1.domain.Origin;
import nz.org.geonet.quakeml.v1_0_1.domain.Quakeml;

/**
 * this class is used to query quakes from the GeoNet Quake category
 * using the quakeMl lib
 * query params: http://www.geonet.org.nz/resources/earthquake/quake-web-services.html
 * url example:
 *  http://app-dev.geonet.org.nz/services/quake/quakeml/1.0.1/query?startDate=2009-05-30T01:11:11&endDate=2009-05-30T11:11:11
 *  
 * @author baishan
 *
 */
public class QuakeMLClient {
	private static final String QUAKE_DATA_SOURCE = "GEONET";
	private static final char QUAKE_EVENT_VERSION = '0';  //is eventVersion stored in the database?? may be similar to the evaluation status?
	//public static final String EVENT_EVALUATION_STATUS_COMFIRMED = "confirmed";
	//public static final String EVENT_EVALUATION_STATUS_PRE = "preliminary";
	//public static final String EVENT_EVALUATION_STATUS_REJECTED = "rejected";

	public  QuakemlFactory qkmlFactory = new QuakemlFactory();
	private static Pattern idPattern =  Pattern.compile("[0-9]{1,10}");
	public  static SimpleDateFormat dateformater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//"2007-05-12T07:41:04.874Z"
	private static Logger logger = Logger.getLogger(QuakeMLClient.class);
	static { dateformater.setTimeZone(TimeZone.getTimeZone("UTC"));  }

	/**
	 * @param startDate
	 * @param endDate
	 *  retrieve quakes from geonet geoserver wfs 
	 *  e.g.
	 *  http://wfs-beta.geonet.org.nz/geoserver/geonet/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=geonet:quake
	 *  &outputFormat=json&cql_filter=origintime>='2011-11-01T16:35:00'+AND+origintime<='2012-06-01T16:35:00'
	 *  
	 *    
	 * {"type":"Feature","id":"quake.3393025","geometry":{"type":"Point","coordinates":[172.34186,-43.59861]},
	 * "geometry_name":"origin_geom","properties":{"publicid":"3393025",
	 * "origintime":"2010-09-05T11:06:16Z","longitude":172.34186,
	 * "latitude":-43.59861,"depth":5,"magnitude":2.97,"magnitudetype":"Ml","status":"reviewed","phases":9,"type":"earthquake",
	 * "agency":"WEL(GNS_Primary)","updatetime":"2012-04-26T09:02:00Z","bbox":[172.34186,-43.59861,172.34186,-43.59861]}}				   
	 */
	public ObsEqkRupList retrieveEvents(Calendar startDate, Calendar endDate){	
		if(startDate == null || endDate == null){
			logger.error("startDate and endDate must be specified for QuakeMl client!!");
			return null;
		}
		ObsEqkRupList eqkRupList = new ObsEqkRupList();
		//Calendar endDatePlus1 = STEP_main.getCurrentGregorianTime();
		//endDatePlus1.setTimeInMillis(endDate.getTimeInMillis() + 24*60*60*1000l);//extend one day to include that day, make the time range wider
		String request = RegionDefaults.GEONET_QUAKEML_URL + "&cql_filter=origintime%3E='" + dateformater.format(startDate.getTime()) 
		+ "'+AND+origintime%3C'" + dateformater.format(endDate.getTime()) + "'";
		logger.info("quakeML request " + request);
	
		HttpClient client = new HttpClient();		
		GetMethod method = new GetMethod(request);	
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("request failed: " + request + " " + method.getStatusLine());
			}else{
				ObjectMapper mapper = new ObjectMapper();
				JsonNode     jsonRoot   = mapper.readTree(method.getResponseBodyAsStream());				
				//logger.info("0 #### features " + jsonRoot.get("features").size());
				JsonNode jsonFeatures =  jsonRoot.get("features");
				logger.info("1 #### features " + jsonFeatures.size());
				for(int i = 0; i < jsonFeatures.size(); i++){
					JsonNode feature = jsonFeatures.get(i);
					JsonNode featureProperties = feature.get("properties");				 

					Date originDate = dateformater.parse(featureProperties.get("origintime").getTextValue());
					GregorianCalendar originTime =   new GregorianCalendar(TimeZone.getTimeZone("UTC"));//
					originTime.setTimeInMillis(originDate.getTime());
					
					double lat = featureProperties.get("latitude").getDoubleValue();
					double lon = featureProperties.get("longitude").getDoubleValue();
					double depth =  featureProperties.get("depth").getDoubleValue();
					Location hypoLoc = new Location(lat, lon, depth);

					String magType = featureProperties.get("magnitudetype").getTextValue();
					double magval =  featureProperties.get("magnitude").getDoubleValue();
					double horzErr =0, vertErr=0, magErr=0;
					magval = Math.round(magval*10)/10d; //round mag
					ObsEqkRupture rupture = new ObsEqkRupture(featureProperties.get("publicid").getTextValue(),
							QUAKE_DATA_SOURCE, QUAKE_EVENT_VERSION,
							originTime,horzErr,vertErr,magErr,
							magType,hypoLoc,magval);
					if(rupture != null){
						//logger.info("rupture " + rupture.getInfo());
						rupture = checkTime(rupture,startDate,endDate);
						if(rupture != null) eqkRupList.addObsEqkEvent(rupture);
					}	
					//if(i < 100 ) logger.info("1 #### rupture " + rupture.getMag());

				}
			}

		} catch (HttpException e) {
			logger.error("Fatal protocol violation: " + e.getMessage());
			//e.printStackTrace();
		} catch (IOException e) {
			logger.error("Fatal transport error: " + e.getMessage());
			//e.printStackTrace();
		} catch (ParseException e) {
			logger.error("Fatal transport error: " + e.getMessage());
		} finally {
			// Release the connection.			
			method.releaseConnection();			
		}
		return eqkRupList;
	}

	/**
	 * @param startDate
	 * @param endDate
	 * TODO the current quakeml can only query to scale of day, and the end time is chopped to 00:00:00,
	 * so one day is added to the end date to make a wider range and a check on the results is added
	 * which is not necessary when a fine grained query in available in quakeml
	 */
	public ObsEqkRupList retrieveEvents_old(Calendar startDate, Calendar endDate){
		if(startDate == null || endDate == null){
			logger.error("startDate and endDate must be specified for QuakeMl client!!");
			return null;
		}
		//Calendar endDatePlus1 = STEP_main.getCurrentGregorianTime();
		//endDatePlus1.setTimeInMillis(endDate.getTimeInMillis() + 24*60*60*1000l);//extend one day to include that day, make the time range wider
		String request = RegionDefaults.GEONET_QUAKEML_URL + "?startDate=" + dateformater.format(startDate.getTime()) 
		+ "&endDate=" + dateformater.format(endDate.getTime());
		logger.info("quakeML request " + request);
		//http://app-dev.geonet.org.nz/services/quake/quakeml/1.0.1/query?startDate=2007-05-17&endDate=2007-05-18
		Quakeml qkml = qkmlFactory.getQuakeml(request, null, null);//request, username, password	    

		ObsEqkRupList eqkRupList = new ObsEqkRupList();
		List <Event> events = qkml.getEventParameters().getEvent();
		logger.info("quakeML events=" + events.size());
		for(Event event:events){
			ObsEqkRupture rupture = event2Rupture(event);
			if(rupture != null){
				//logger.info("rupture " + rupture.getInfo());
				rupture = checkTime(rupture,startDate,endDate);
				if(rupture != null) eqkRupList.addObsEqkEvent(rupture);
			}
		}
		logger.info("eqkRupList=" + eqkRupList.size());
		return eqkRupList;
	}

	/**
	 * check a quake is within specified time range
	 * @param rupture
	 * @param start: the start time, inclusive
	 * @param end: the end time, not inclusive
	 * @return
	 */
	private ObsEqkRupture checkTime(ObsEqkRupture rupture, Calendar start,
			Calendar end) {
		if(rupture.getOriginTime().before(start)){
			return null;
		}
		if(rupture.getOriginTime().after(end)||rupture.getOriginTime().equals(end)){//not inclusive
			return null;
		}
		return rupture;
	}

	/**
	 * transfer quakeml event to ObsEqkRupture
	 * @param event
	 * @return
	 */
	public ObsEqkRupture event2Rupture(Event event){
		try{
			String publicID = event.getPublicID();	//e.g.smi:geonet.org.nz/ori/431978/GROPE		
			Matcher m = idPattern.matcher(publicID);
			String eventID = publicID;		
			if( m.find()){					
				eventID = publicID.substring(m.start(), m.end());//extract the number:431978	
			}

			char eventVersion = QUAKE_EVENT_VERSION;
			//logger.info("1 eventID " + eventID  );
			Origin origin = QuakemlUtils.getPreferredOrigin(event);//event.getOrigin().get(0);
			String evalStatus = origin.getEvaluationStatus().value();
			//logger.info("2 eventID " + eventID + " evalStatus " + evalStatus  );
			if(evalStatus != null && evalStatus.length() > 0){//TODO check, it seems the only equavlent property to the event version is the evaluation status
				eventVersion = evalStatus.charAt(0);
			}
			XMLGregorianCalendar oriTime = origin.getTime().getValue();

			GregorianCalendar originTime = oriTime.toGregorianCalendar(TimeZone.getTimeZone("UTC"), null, null);//new GregorianCalendar(TimeZone.getTimeZone("GMT"));			

			double lat = origin.getLatitude().getValue();
			double lon = origin.getLongitude().getValue();
			double depth =  origin.getDepth().getValue();
			Location hypoLoc = new Location(lat, lon, depth);

			Magnitude mag = QuakemlUtils.getPreferredMagnitude(event);//event.getMagnitude().get(0);
			String magType = mag.getType();
			double magval =   0;
			if(mag != null && mag.getMag() != null) {
				magval =  mag.getMag().getValue();
			} else {//a quake event must have a magnitude
				return null;
			}
			//logger.info("oriTime=" + oriTime + " lat " + lat + " lon " + lon + " depth " + depth + " mag " + magval  + " magType " + magType);

			double horzErr =0, vertErr=0, magErr=0;

			if(mag != null && mag.getMag() != null && mag.getMag().getUncertainty() != null) {
				magErr = mag.getMag().getUncertainty();
			}

			if(origin != null ){
				double latErr = 0, lonErr = 0;	  
				if(origin.getLatitude() != null && origin.getLatitude().getUncertainty() != null){
					latErr = origin.getLatitude().getUncertainty();
				}
				if(origin.getLongitude() != null && origin.getLongitude().getUncertainty() != null){
					lonErr = origin.getLongitude().getUncertainty();	
				}				   		  
				horzErr = lonErr > latErr? lonErr:latErr;		    	
				if( origin.getDepth() != null && origin.getDepth().getUncertainty() != null ){		    		
					vertErr = origin.getDepth().getUncertainty();	
				}		    	
			}			    

			//logger.info("horzErr=" + horzErr + " vertErr " + vertErr   );		    		
			return  new ObsEqkRupture(eventID,QUAKE_DATA_SOURCE, eventVersion,
					originTime,horzErr,vertErr,magErr,
					magType,hypoLoc,magval);

		}catch(Exception e){
			logger.error("event2Rupture parse event error " + e);
			//e.printStackTrace();
			return null;
		}		
	}
}
