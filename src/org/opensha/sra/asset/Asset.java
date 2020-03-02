package org.opensha.sra.asset;
import java.util.Currency;
import java.util.HashMap;

import org.opensha.commons.param.ParameterList;
import org.opensha.sra.vulnerability.Vulnerability;

/**
 * A <code>PointAsset</code> represents a particular <code>AssetType</code> at 
 * a particular site. Potentially could reflect detailed building-specific
 * attributes such as cladding type, setbacks, shape, etc. via an
 * arbitrary parameter list. Flexible enough for earthquake, wind, or
 * flood risk.
 * 
 * NOTE: Concrete implementations of various asset types may be 
 * necessary if more detail/functionality is required than can be provided by
 * an Asset and internal AssetType alone.
 *
 *
 * NOTE: Placeholder
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class Asset {

	private int id;
	private String name;
	private AssetType type;
	private Value value;
	private double valueYear;
	private Currency valueCurrency;
	private ParameterList params;
	
	private HashMap<Vulnerability, Double> vulnWeightMap;
	private HashMap<Vulnerability, Double> fragWeightMap;
	
}
