package org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData;

import java.util.ArrayList;

//import org.geotools.geometry.jts.JTSFactoryFinder;
//import org.opengis.geometry.coordinate.GeometryFactory;
//import org.opengis.geometry.coordinate.Polygon;
//import org.opengis.geometry.primitive.SurfaceBoundary;
//import com.vividsolutions.jts.geom.Coordinate;

import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.BorderType;
import org.opensha.commons.data.region.Region;
import org.opensha.sha.earthquake.griddedForecast.MagFreqDistsForFocalMechs;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;
import org.opensha.sha.util.TectonicRegionType;


public class GEMAreaSourceData extends GEMSourceData {
 
	// this holds pairs of focal mechanism and mag-freq distributions
	// if stike is NaN then uniform distribution is assumed.
	private MagFreqDistsForFocalMechs magFreqDistsForFocalMechs;
	private Region reg;
	// the following specifies the average depth to top of rupture as a function of
	// magnitude, which will be obtained using the getInterpolatedY(mag) method.
	private ArbitrarilyDiscretizedFunc aveRupTopVsMag;
	// the following will be used to locate point sources (i.e., for all mags lower than the minimum mag in aveRupTopVsMag)
	private double aveHypoDepth;

	/**
	 * 
	 */
	public GEMAreaSourceData(String id, String name, TectonicRegionType tectReg, 
			Region reg, MagFreqDistsForFocalMechs magFreqDistsForFocalMechs,
			ArbitrarilyDiscretizedFunc aveRupTopVsMag, double aveHypoDepth) {
		this.id = id;
		this.name = name;
		this.tectReg = tectReg;
		this.reg = reg;
		this.magFreqDistsForFocalMechs = magFreqDistsForFocalMechs;
		this.aveRupTopVsMag = aveRupTopVsMag;
		this.aveHypoDepth = aveHypoDepth;
	} 
	
	/**
	 * 
	 * @return
	 */
	public MagFreqDistsForFocalMechs getMagFreqDistsForFocalMechs(){
		return this.magFreqDistsForFocalMechs;
	}
	
	/**
	 * 
	 * @return
	 */
	public Region getRegion(){
		return this.reg;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArbitrarilyDiscretizedFunc getAveRupTopVsMag(){
		return this.aveRupTopVsMag;
	}

	
	
	/**
	 * 
	 * @return
	 */
	public double getAveHypoDepth(){
		return this.aveHypoDepth;
	}

	
//	/**
//	 * 
//	 */
//	public void writeToShpFile() {
//		
//		// Geometry builder
//		GeometryFactory geometryFactory = (GeometryFactory) JTSFactoryFinder.getGeometryFactory(null);
//		
//		// Create a simple geometry using the vertexes of the polygon
//		LocationList border = this.reg.getBorder();
//		List<Coordinate> coords = new ArrayList<Coordinate>();
//		for (int i=0; i<border.size(); i++){
//			coords.add(new Coordinate(border.getLocationAt(i).getLongitude(), border.getLocationAt(i).getLatitude()));
//		}
//	    coords.add(new Coordinate(border.getLocationAt(0).getLongitude(), border.getLocationAt(0).getLatitude()));
//
//	    // Create the polygon
//	    Polygon poly = geometryFactory.createPolygon((SurfaceBoundary) coords );
//	    
//	    // Write shapefile 
//		
//	}
	
}
