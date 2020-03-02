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

package org.opensha.sha.earthquake.rupForecastImpl;

import java.util.ArrayList;
import java.util.Iterator;

import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.MagLengthRelationship;
import org.opensha.commons.calc.magScalingRelations.MagScalingRelationship;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.Site;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;


/**
 * <p>Title: FloatingPoissonFaultSource </p>
 * <p>Description: This implements a basic Poisson fault source for arbitrary: <p>
 * <UL>
 * <LI>magDist - any IncrementalMagFreqDist
 * <LI>faultSurface - any EvenlyDiscretizedSurface
 * <LI>magScalingRel- any magLenthRelationship or magAreaRelalationship
 * <LI>magScalingSigma - the standard deviation of log(Length) or log(Area)
 * <LI>rupAspectRatio - the ratio of rupture length to rupture width (down-dip)
 * <LI>rupOffset - the amount by which ruptures are offset on the fault.
 * <LI>rake - that rake (in degrees) assigned to all ruptures.
 * <LI>timeSpan - the duration of the forecast (in same units as in the magFreqDist)
 * </UL><p>
 * Note that none of these input objects are saved internally (after construction) in
 * order to conserve memory (this is why there are no associated get/set methods for each).<p>
 * The ruptures are placed uniformly across the fault surface (at rupOffset spacing), which
 * means there is a tapering of implied slip amounts at the ends of the fault.<p>
 * All magnitudes below 5.0 in the magDist are ignored in building the ruptures (unless
 * one uses the constructor that allows the "minMag" to be set by hand). <p>
 * Note that magScalingSigma can be either a MagAreaRelationship or a
 * MagLengthRelationship.  If a MagAreaRelationship is being used, and the rupture
 * width implied for a given magnitude exceeds the down-dip width of the faultSurface,
 * then the rupture length is increased accordingly and the rupture width is set as
 * the down-dip width.  If a MagLengthRelationship is being used, and the rupture
 * width implied by the rupAspectRatio exceeds the down-dip width, everything below
 * the bottom edge of the fault is simply cut off (ignored).  Thus, with a
 * MagLengthRelationship you can force rupture of the entire down-dip width by giving
 * rupAspecRatio a very small value.</p>
 * magScalingSigma is set by hand (rather than getting it from the magScalingRel) to
 * allow maximum flexibility (e.g., some relationships do not even give a sigma value).<p>
 * If magScalingSigma is non zero, then three branches are considered for the Area or Lengths
 * values: median and +/-1.64sigma, with relative weights of 0.6, 0.2, and 0.2, respectively.<p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Sept, 2003
 * @version 1.0
 */

public class OldFloatingPoissonFaultSource extends ProbEqkSource {

  //for Debug purposes
  private static String  C = new String("Old FloatingPoissonFaultSource");
  private boolean D = false;

  //name for this classs
  protected String  NAME = "Old Floating Poisson Fault Source";

  // private fields
  private int totNumRups;
  private ArrayList ruptureList;
  private ArrayList faultCornerLocations = new ArrayList();   // used for the getMinDistance(Site) method
  private double duration;
  private EvenlyGriddedSurface faultSurface;

  /* Note that none of the input objects are saved after the ruptureList is created
     by the constructor.  This is deliberate to save memory.  A setName() method was
     added here to enable users to give a unique identifier once created.
  */

  /**
   * This creates the Simple Poisson Fault Source.
   * @param magDist - any incremental mag. freq. dist. object
   * @param faultSurface - any EvenlyGriddedSurface representation of the fault
   * @param magScalingRel - any magAreaRelationship or magLengthRelationthip
   * @param magScalingSigma - uncertainty of the length(mag) or area(mag) relationship
   * @param rupAspectRatio - ratio of rupture length to rupture width
   * @param rupOffset - amount of offset for floating ruptures
   * @param rake - average rake of the ruptures
   * @param duration - the timeSpan of interest in years (this is a Poissonian source)
   * @param minMag - the minimum magnitude to be considered from magDist (lower mags are ignored)
   */
  public OldFloatingPoissonFaultSource(IncrementalMagFreqDist magDist,
                                  EvenlyGriddedSurface faultSurface,
                                  MagScalingRelationship magScalingRel,
                                  double magScalingSigma,
                                  double rupAspectRatio,
                                  double rupOffset,
                                  double rake,
                                  double duration,
                                  double minMag) {

      this.duration = duration;
      this.faultSurface = faultSurface;

      if (D) {
        System.out.println(magDist.getName());
        System.out.println("surface rows, cols: "+faultSurface.getNumCols()+", "+faultSurface.getNumRows());
        System.out.println("magScalingRelationship: "+magScalingRel.getName());
        System.out.println("magScalingSigma: "+magScalingSigma);
        System.out.println("rupAspectRatio: "+rupAspectRatio);
        System.out.println("rupOffset: "+rupOffset);
        System.out.println("rake: "+rake);
        System.out.println("timeSpan: "+duration);
        System.out.println("minMag: "+minMag);

      }
      // make a list of a subset of locations on the fault for use in the getMinDistance(site) method
      makeFaultCornerLocs(faultSurface);

      // make the rupture list
      ruptureList = new ArrayList();
      if(magScalingSigma == 0.0)
        addRupturesToList(magDist, faultSurface, magScalingRel, magScalingSigma, rupAspectRatio, rupOffset, rake, minMag, 0.0, 1.0);
      else {
    	  GaussianMagFreqDist gDist = new GaussianMagFreqDist(-3.0,3.0,25,0.0,1.0,1.0);
    	  gDist.scaleToCumRate(0, 1.0);
    	  for(int m=0; m<gDist.getNum(); m++) {
    		  addRupturesToList(magDist, faultSurface, magScalingRel, magScalingSigma,
                      rupAspectRatio, rupOffset, rake, minMag, gDist.getX(m), gDist.getY(m));
//    		  System.out.println(m+"\t"+gDist.getX(m)+"\t"+gDist.getY(m));
    	  }
    	  
//        The branch-tip weights (0.6, 0.2, and 0.2) for the mean, -1.64sigma, and +1.64sigma,
//       respectively, are from WG99's Table 1.1
    	  /*
        addRupturesToList(magDist, faultSurface, magScalingRel, magScalingSigma,
                          rupAspectRatio, rupOffset, rake, minMag, 0.0, 0.6);
        addRupturesToList(magDist, faultSurface, magScalingRel,
                          magScalingSigma, rupAspectRatio, rupOffset, rake, minMag, 1.64, 0.2);
        addRupturesToList(magDist, faultSurface, magScalingRel,
                          magScalingSigma, rupAspectRatio, rupOffset, rake, minMag, -1.64, 0.2);
       */
      }
  }


  /**
   * Same as other constuctor, but where minMag defaults to 5.0.
   */
  public OldFloatingPoissonFaultSource(IncrementalMagFreqDist magDist,
                                  EvenlyGriddedSurface faultSurface,
                                  MagScalingRelationship magScalingRel,
                                  double magScalingSigma,
                                  double rupAspectRatio,
                                  double rupOffset,
                                  double rake,
                                  double duration) {
    this( magDist, faultSurface, magScalingRel,magScalingSigma,rupAspectRatio,rupOffset,rake,duration,5.0);
  }

  /**
   * This computes the rupture length from the information supplied
   * @param magScalingRel - a MagLengthRelationship or a MagAreaRelationship
   * @param magScalingSigma - the standard deviation of the Area or Length estimate
   * @param numSigma - the number of sigmas from the mean for which the estimate is for
   * @param rupAspectRatio
   * @param mag
   * @return
   */
  private double getRupLength(MagScalingRelationship magScalingRel,
                              double magScalingSigma,
                              double numSigma,
                              double rupAspectRatio,
                              double mag) throws RuntimeException {

    // if it's a mag-area relationship
    if(magScalingRel instanceof MagAreaRelationship) {
      double area = magScalingRel.getMedianScale(mag) * Math.pow(10,numSigma*magScalingSigma);
      return Math.sqrt(area*rupAspectRatio);
    }
    else if (magScalingRel instanceof MagLengthRelationship) {
      return magScalingRel.getMedianScale(mag) * Math.pow(10,numSigma*magScalingSigma);
    }
    else throw new RuntimeException("bad type of MagScalingRelationship");
  }



  /**
   * This method makes and adds ruptures to the list
   */
  private void addRupturesToList(IncrementalMagFreqDist magDist,
                             EvenlyGriddedSurface faultSurface,
                             MagScalingRelationship magScalingRel,
                             double magScalingSigma,
                             double rupAspectRatio,
                             double rupOffset,
                             double rake,
                             double minMag,
                             double numSigma,
                             double weight) {

    int numMags = magDist.getNum();  // Note that some of these may have zero rates!

    double rupLen;
    double rupWidth;
    double numRup;
    double mag;
    double rate;
    double prob=Double.NaN;

    // get down-dip width of fault (from first column)
    Location loc1 = (Location) faultSurface.get(0,0);
    Location loc2 = (Location) faultSurface.get(faultSurface.getNumRows()-1,0);
    double ddw = RelativeLocation.getTotalDistance(loc1,loc2);

    if( D ) System.out.println(C+": ddw="+ddw);

    if( D ) System.out.println(C+": magScalingSigma="+magScalingSigma);
    for(int i=0;i<numMags;++i){
      mag = magDist.getX(i);
      // make sure it has a non-zero rate & the mag is >= minMag
      if(magDist.getY(i) > 0 && mag >= minMag) {

        rupLen = getRupLength(magScalingRel,magScalingSigma,numSigma,rupAspectRatio,mag);
        rupWidth= rupLen/rupAspectRatio;

        // if magScalingRel is a MagAreaRelationship, then rescale rupLen if rupWidth
        // exceeds the down-dip width (don't do anything for MagLengthRelationship)
        if(magScalingRel instanceof MagAreaRelationship  && rupWidth > ddw) {
          rupLen *= rupWidth/ddw;
          rupWidth = ddw;
        }
//System.out.println((float)mag+"\t"+(float)rupLen+"\t"+(float)rupWidth+"\t"+(float)(rupLen*rupWidth));

        numRup = faultSurface.getNumSubsetSurfaces(rupLen,rupWidth,rupOffset);
//if(mag == 6.445)  System.out.println(rupLen+"\t"+rupWidth+"\t"+rupOffset+"\t"+mag+"\t"+numRup);
        rate = magDist.getY(mag);
        // Create the ruptures and add to the list
        for(int r=0; r < numRup; ++r) {
          probEqkRupture = new ProbEqkRupture();
          probEqkRupture.setAveRake(rake);
          probEqkRupture.setRuptureSurface(faultSurface.getNthSubsetSurface(rupLen,rupWidth,rupOffset,r));
          probEqkRupture.setMag(mag);
          prob = weight*(1.0 - Math.exp(-duration*rate/numRup));
          probEqkRupture.setProbability(prob);
          ruptureList.add(probEqkRupture);
        }
          if( D ) System.out.println(C+": mag="+mag+"; rupLen="+rupLen+"; rupWidth="+rupWidth+
                                      "; rate="+rate+"; timeSpan="+duration+"; numRup="+numRup+
                                      "; weight="+weight+"; prob="+prob);
      }
    }
  }

  /**
   * It returns a list of all the locations which make up the surface for this
   * source.
   *
   * @return LocationList - List of all the locations which constitute the surface
   * of this source
   */
   public LocationList getAllSourceLocs() {
     return this.faultSurface.getLocationList();
   }
   
   public EvenlyGriddedSurfaceAPI getSourceSurface() { return this.faultSurface; }

  /**
   * @return the total num of rutures for all magnitudes
   */
  public int getNumRuptures() { return ruptureList.size(); }


  /**
   * This method returns the nth Rupture in the list
   */
  public ProbEqkRupture getRupture(int nthRupture){ return (ProbEqkRupture) ruptureList.get(nthRupture); }


   /**
   * This returns the shortest dist to either end of the fault trace, or to the
   * mid point of the fault trace (done also for the bottom edge of the fault).
   * @param site
   * @return minimum distance in km
   */
   public  double getMinDistance(Site site) {

      double min = Double.MAX_VALUE;
      double tempMin;

      Iterator it = faultCornerLocations.iterator();

      while(it.hasNext()) {
        tempMin = RelativeLocation.getHorzDistance(site.getLocation(),(Location)it.next());
        if(tempMin < min) min = tempMin;
      }

      return min;
    }


    /**
     * This makes the vector of fault corner location used by the getMinDistance(site)
     * method.
     * @param faultSurface
     */
    private void makeFaultCornerLocs(EvenlyGriddedSurface faultSurface) {

      int nRows = faultSurface.getNumRows();
      int nCols = faultSurface.getNumCols();
      faultCornerLocations.add(faultSurface.get(0,0));
      faultCornerLocations.add(faultSurface.get(0,(int)(nCols/2)));
      faultCornerLocations.add(faultSurface.get(0,nCols-1));
      faultCornerLocations.add(faultSurface.get(nRows-1,0));
      faultCornerLocations.add(faultSurface.get(nRows-1,(int)(nCols/2)));
      faultCornerLocations.add(faultSurface.get(nRows-1,nCols-1));

    }

    /**
     * set the name of this class
     *
     * @return
     */
    public void setName(String name) {
      NAME=name;
     }

 /**
  * get the name of this class
  *
  * @return
  */
 public String getName() {
   return NAME;
  }
}
