package org.opensha.step.calc;

import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;

/**
 * <p>Title: MaxLikeGR_Calc </p>
 * <p>Description: Calculate maximum likelihood a and b values from the
* Gutenberg Richter distribution</p>
 * @author Matt Gerstenberger
 * @version 1.0
 */

public class MaxLikeGR_Calc {
  private  double aVal;
  private  double bVal;
  private  double[] grid_aVal;
  private  double[] grid_bVal;
  private  double binning = 0.1;

  /**
   * default constuctor
   */
  public MaxLikeGR_Calc() {

  }
  /**
   * get the a value from the Gutenberg Richter Distribution
   * @return double
   */
  public  double get_aValueMaxLike(){
   return aVal;
  }
  /**
   * get the b value from the Gutenberg Richter distribution
   * @return double
   */

  public  double get_bValueMaxLike(){
    return bVal;
  }

  /**
   * getGridded_aValMaxLike
   * returns the a value for each location in the evenly gridded region
   */
  public  double[] getGridded_aValMaxLike() {
    return grid_aVal;
  }

  /**
   * getGridded_bValMaxLike
   * returns the b value for each location in the evenly gridded region
   */
  public  double[] getGridded_bValMaxLike() {
    return grid_bVal;
  }


  /**
   * Get the magnitudes of the earthquakes to be used in the GR calculation
   * @param magList double[]
   */
  public  void setMags(double[] magList){
    calcGR_MaxLike(magList);
  }
  /**
   * Get the magnitudes of the earthquakes to be used in the GR calculation
   * @param magList ArrayList array of Doubles
   */

  public  void setMags(ObsEqkRupList obsEventList){
    ListIterator eventIt = obsEventList.listIterator();
    ObsEqkRupture event;
    int numEvents = obsEventList.size();
    int ind = 0;
    double[] magList = new double[numEvents];
    while (eventIt.hasNext())  {
      event = (ObsEqkRupture)eventIt.next();
      magList[ind++] = event.getMag();
    }
    calcGR_MaxLike(magList);

  }

  /**
   * setGriddedMags
   * calculate the a and b values on the evenly gridded region grid.
   *
   */
  public  void setGriddedMags(GriddedRegion gridNodes, ObsEqkRupList eventList) {
    calc_GROnGrid(gridNodes, eventList);
  }

  private  double getMinMag(double[] magList){
    // find the minimum magnitude of the given catalogue
   double minMag = magList[0];

   int size = magList.length;
   for(int magLoop = 1;magLoop < size; ++magLoop)
     if(magList[magLoop] < minMag){
       minMag = magList[magLoop];
     }
   return minMag;
    }
  private  double getMeanMag(double[] magList){
    // find the mean magnitude for use in the GR calculation
    double sum = 0;
    int size = magList.length;
   for(int magLoop = 0;magLoop < size; ++magLoop)
     sum += magList[magLoop];
    return sum/size;
  }

  private  void calcGR_MaxLike(double[] magList){
  /*  fMinMag = min(mCatalog(:,6));
   fMeanMag = mean(mCatalog(:,6));
   % Calculate the b-value (maximum likelihood)
   fBValue = (1/(fMeanMag-(fMinMag-(fBinning/2))))*log10(exp(1));
   fAValue = log10(nLen) + fBValue * fMinMag;
*/
   double minMag = getMinMag(magList);
   double meanMag = getMeanMag(magList);
   int size = magList.length;
   bVal = (1/(meanMag-(minMag-(binning/2.0))))*0.43429;
   aVal = Math.log(size)*.43429 + bVal * minMag;
  }



  /**
   * calc_GROnGrid
   */
  private  void calc_GROnGrid(GriddedRegion gridNodes, ObsEqkRupList eventList){
	  Iterator<Location> gridIt = gridNodes.getNodeList().iterator();
    int numNodes = gridNodes.getNodeCount();
    //I DO THIS TWICE - ABOVE PUBLIC W/O THE SIZE DEC?!?!?!
    double[] grid_aVal = new double[numNodes];
    double[] grid_bVal = new double[numNodes];

    ListIterator eventIt = eventList.listIterator();
    int numEvents = eventList.size();
    double[] eventDist = new double[numEvents];
    double searchRadius;
    double completenessMag;
    int ind = 0;

    if (numEvents < 1000) searchRadius = 15;
    else if (numEvents < 1500) searchRadius = 12;
    else if (numEvents < 2000) searchRadius = 10;
    else searchRadius = 7.5;
    /**
     * This is defined here and in CalcGROnGRid
     * need to sort that out.
    while (gridIt.hasNext()) {
      CircularGeographicRegion gridRegion = new CircularGeographicRegion((Location)gridIt.next(),searchRadius);
      ObsEqkRupList regionEventList = eventList.getObsEqkRupsInside(gridRegion);

      ObsEqkRupList regionEventList = new ObsEqkRupList();

      CompletenessMagCalc.setMcBest(regionEventList);
      completenessMag = CompletenessMagCalc.getMcBest();
      ObsEqkRupList completeRegionList = regionEventList.getObsEqkRupsAboveMag(completenessMag);
      setMags(completeRegionList);
      grid_aVal[ind] = get_aValueMaxLike();
      grid_bVal[ind++] = get_bValueMaxLike();
    }
   */
  }

  public static void main(String[] args) {
    double[] magList = new double[10];
    double startMag = 3;
    for(int synMag = 0;synMag<10;++synMag){
      magList[synMag] = startMag;
      ++startMag;
    }
    MaxLikeGR_Calc calc = new MaxLikeGR_Calc();
    calc.setMags(magList);
    System.out.println("aVal is: "+ calc.get_aValueMaxLike());
    System.out.println("bVal is: "+ calc.get_bValueMaxLike());

  }

}
