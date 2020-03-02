package org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.util.TectonicRegionType;

public class GEMSubductionFaultSourceData extends GEMSourceData{
	
	private FaultTrace topTrace;
	private FaultTrace bottomTrace;
	private double rake;
	private IncrementalMagFreqDist mfd;	
	private boolean floatRuptureFlag;

	
	// constructor.  TectonicRegionType defaults to SUBDUCTION_INTERFACE here.
	public GEMSubductionFaultSourceData(FaultTrace TopTrace, FaultTrace BottomTrace, 
			double rake, IncrementalMagFreqDist mfd, boolean floatRuptureFlag){
		
		this.topTrace = TopTrace;
		this.bottomTrace = BottomTrace;
		this.rake = rake;
		this.mfd = mfd;
		this.floatRuptureFlag = floatRuptureFlag;
		
		this.tectReg = TectonicRegionType.SUBDUCTION_INTERFACE;
		
	}

	public FaultTrace getTopTrace() {
		return topTrace;
	}

	public FaultTrace getBottomTrace() {
		return bottomTrace;
	}

	public double getRake() {
		return rake;
	}

	public IncrementalMagFreqDist getMfd() {
		return mfd;
	}
	
	public boolean getFloatRuptureFlag() {
		return floatRuptureFlag;
	}



}
