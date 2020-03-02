package org.opensha.gem.condor.calc.components;

import java.io.IOException;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.metadata.XMLSaveable;

/**
 * This interface defines a mechanism for storing hazard curve results. Initially this will probably
 * just have one implementation, storing hazard curves to files. In the future, you may want to have
 * this write the values to a database
 * 
 * @author kevin
 *
 */
public interface CurveResultsArchiver extends XMLSaveable {
	
	/**
	 * This stores the curve for the given site.
	 * 
	 * @param curve - the curve itself
	 * @param meta - curve metadata
	 * @throws IOException 
	 */
	public void archiveCurve(ArbitrarilyDiscretizedFunc curve, CurveMetadata meta) throws IOException;
}
