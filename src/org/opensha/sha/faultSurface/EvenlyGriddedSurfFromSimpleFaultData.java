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

package org.opensha.sha.faultSurface;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.data.Location;
import org.opensha.commons.exceptions.FaultException;
import org.opensha.commons.util.FaultUtils;


/**
 * <p>Title:  EvenlyGriddedSurfFromSimpleFaultData </p>
 *
 * <p>Description: This creates and EvenlyGriddedSurface from SimpleFaultData</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public abstract class EvenlyGriddedSurfFromSimpleFaultData
    extends EvenlyGriddedSurface{

    // *********************
    /** @todo  Variables */
    // *********************

    /* Debbuging variables */
    protected final static String C = "EvenlyGriddedSurfFromSimpleFaultData";
    protected final static boolean D = false;

    protected FaultTrace faultTrace;
    protected double upperSeismogenicDepth = Double.NaN;
    protected double lowerSeismogenicDepth = Double.NaN;



    protected EvenlyGriddedSurfFromSimpleFaultData(SimpleFaultData simpleFaultData,
    		double gridSpacing) throws
    		FaultException {
 
      this(simpleFaultData.getFaultTrace(),
           simpleFaultData.getAveDip(),
           simpleFaultData.getUpperSeismogenicDepth(),
           simpleFaultData.getLowerSeismogenicDepth(),
           gridSpacing);

    }
    
    protected EvenlyGriddedSurfFromSimpleFaultData(FaultTrace faultTrace,
                                                double aveDip,
                                                double upperSeismogenicDepth,
                                                double lowerSeismogenicDepth,
                                                double gridSpacing) throws
        FaultException {
    	set(faultTrace, aveDip, upperSeismogenicDepth, lowerSeismogenicDepth, gridSpacing);
    }
    
    private void set(FaultTrace faultTrace,
    		double aveDip,
    		double upperSeismogenicDepth,
    		double lowerSeismogenicDepth,
    		double gridSpacing)	{
    	this.faultTrace =faultTrace;
    	this.aveDip =aveDip;
    	this.upperSeismogenicDepth = upperSeismogenicDepth;
    	this.lowerSeismogenicDepth =lowerSeismogenicDepth;
    	this.gridSpacing = gridSpacing;
    }
    
    
    /**
     * Stitch Together the fault sections. It assumes:
     * 1. Sections are in correct order (in how they are to be stitched together)
     * 2. Distance between adjacent points on neighboring sections (in correct order) 
     * is less than distance to opposite ends of the sections.  In other words no sections
     * overlap by more than half the section length.
     * Each of the following are average over the sections (weight averaged by area): 
     * upper and lower seismogenic depth, slip.  Total area of surface is maintained, 
     * plus an addition area implied by gaps between neighboring sections.
     * 
     * @param simpleFaultData
     * @param gridSpacing
     * @throws FaultException
     */
    protected EvenlyGriddedSurfFromSimpleFaultData(ArrayList<SimpleFaultData> simpleFaultDataList, double gridSpacing) {
    	SimpleFaultData simpleFaultData = SimpleFaultData.getCombinedSimpleFaultData(simpleFaultDataList);
    	set(simpleFaultData.getFaultTrace(), 
    			simpleFaultData.getAveDip(), 
    			simpleFaultData.getUpperSeismogenicDepth(),
    			simpleFaultData.getLowerSeismogenicDepth(),
    			gridSpacing);
    	
    }

    // ***************************************************************
    /** @todo  Serializing Helpers - overide to increase performance */
    // ***************************************************************

    protected void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }
    protected void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }


    public FaultTrace getFaultTrace() { return faultTrace; }


    public double getUpperSeismogenicDepth() { return upperSeismogenicDepth; }

    public double getLowerSeismogenicDepth() { return lowerSeismogenicDepth; }


    /**
     * This method checks the simple-fault data to make sure it's all OK.
     * @throws FaultException
     */
    protected void assertValidData() throws FaultException {

        if( faultTrace == null ) throw new FaultException(C + "Fault Trace is null");

        FaultUtils.assertValidDip(aveDip);
        FaultUtils.assertValidSeisUpperAndLower(upperSeismogenicDepth, lowerSeismogenicDepth);

        if( gridSpacing == Double.NaN ) throw new FaultException(C + "invalid gridSpacing");

        double depth = faultTrace.getLocationAt(0).getDepth();
        if(depth > upperSeismogenicDepth)
                throw new FaultException(C + "depth on faultTrace locations must be < upperSeisDepth");

        Iterator<Location> it = faultTrace.iterator();
        while(it.hasNext()) {
          if(it.next().getDepth() != depth){
            throw new FaultException(C + ":All depth on faultTrace locations must be equal");
          }
        }
    }
}
