package org.opensha.step.calc;

import org.apache.log4j.Logger;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;

import java.util.ListIterator;



/**
 * <p>Title: CompletenessMagCalc</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CompletenessMagCalc {
	private static Logger logger = Logger.getLogger(CompletenessMagCalc.class);
	
//TODO suggestions: change variables to non static, and let methods to return the result
  public  double mcBest;
  public  double mcSynth;
  public  double mcMaxCurv;
  public  int numInBin;
  private  double deltaBin = 0.1;
  private  double GR_MMax = 10.0; // max mag to use in GR calc

  public CompletenessMagCalc() {

  }

  public CompletenessMagCalc(ObsEqkRupList afterShocks) {
	  setMcMaxCurv(afterShocks);
	  setMcBest(afterShocks);
}

/**
   * set_McBest
   * Calculate the best Mc estimate based on the synthetic and max curvature
   * methods
   */
  public  void setMcBest(ObsEqkRupList eventList) {
    ListIterator eventIt = eventList.listIterator();
    ObsEqkRupture event;
    int numEvents = eventList.size();
    int ind = 0;
    double[] magList = new double[numEvents];
    while (eventIt.hasNext())  {
      event = (ObsEqkRupture)eventIt.next();
      magList[ind++] = event.getMag();
    }
	//setMcMaxCurv(eventList);

    calcMcSynth(magList);
  }

  /**
   * setMcMaxCurv
   */
  public void setMcMaxCurv(ObsEqkRupList eventList) {
    ListIterator eventIt = eventList.listIterator();
    ObsEqkRupture event;
    int numEvents = eventList.size();
    int ind = 0;
    double[] magList = new double[numEvents];
    while (eventIt.hasNext())  {
      event = (ObsEqkRupture)eventIt.next();
      magList[ind++] = event.getMag();
    }
    calcMcMaxCurv(magList);
  }

  /**
   *
   * @return mcBest double
   */
  public  double getMcBest(){
    if (mcSynth != Double.NaN)
        mcBest = mcSynth;
      else
        mcBest = mcMaxCurv;
    mcBest = 4.0;
    return mcBest;
    }

    /**
     * Calculate Mc based on the max curvature method
     * @return mcMaxCurv double
     */
    public  double getMcMaxCurv(){
      return mcMaxCurv;
    }

    /**
     * Calculate Mc based on synthetic GR distributions.  Return the value
     * estimated at 95% probability, if not return the 90%.  If this is not
     * possible to estimate, return a -99
     * @return double
     */
    public  double getMcSynth(){

      //HOW TO BETTER HANDLE THE -99?!?!?!?!?!
      return mcSynth;
    }

    private  void calcMcMaxCurv(double[] magList){

      double minMag = ListSortingTools.getMinVal(magList);
      double maxMag = ListSortingTools.getMaxVal(magList);
      if(minMag>0){
        minMag=0;
      }

      //number of mag bins
      int numMags = (int)(maxMag*10)+1;

      //create the histogram of the mag bins
      MagHist hist = new MagHist(magList,minMag,maxMag,deltaBin);
      int[] numInBin = hist.getNumInBins();
      // find the value of max curvature and the bin it corresponds to
      double maxCurv = ListSortingTools.getMaxVal(numInBin);
      //System.out.println("Min Max Mag = "+minMag+"," +maxMag);
      double[] magRange = ListSortingTools.getEvenlyDiscrVals(minMag,maxMag,deltaBin);
      int maxCurvInd = ListSortingTools.findIndex((int)maxCurv,numInBin);
      //set mc to the M value at the maximum curvature
      mcMaxCurv = magRange[maxCurvInd];
    }

    private  void calcMcSynth(double[] magList){
      // make a first guess at Mc using max curvature
      mcMaxCurv = getMcMaxCurv();       
         
      int aSize = (int)((((mcMaxCurv+1.5)-(mcMaxCurv-0.9))/deltaBin)+1.0);
      double[] fitProb = new double[aSize];
      //int ct = 0;
      // loop over a range of completeness guesses
      //double topVal = mcMaxCurv + 1.5;      
      //for(double mcLoop = mcMaxCurv-0.9; mcLoop < mcMaxCurv + 1.5;
      //    mcLoop += deltaBin){
      double mcLoop = mcMaxCurv-0.9;
      for (int ct = 0; ct < aSize; ct++) {
          mcLoop = mcLoop + deltaBin;
        //double[] magBins = new double[numEvents];
      // get all events above the completeness guess (mcLoop)
        double[] cutCat = null;
        int sizeCutCat = 0;
        try{//TODO check, the could be no value found in the magList for the provided top value mcMaxCurv + 1.5
        	// in this case, simply ignore the error, correct??
        	 cutCat = ListSortingTools.getValsAbove(magList,mcLoop);
             //
             sizeCutCat = cutCat.length;
          }
          catch (NoValsFoundException err1){
        	 // logger.error("-- calcMcSynth error " + err1 );
        	  //err1.printStackTrace();
        	  //continue;
          }       

        // if > 25 events calculate the b value and estimate Mc
        if(sizeCutCat >= 25){
          MaxLikeGR_Calc maxLikeGR_Calc = new MaxLikeGR_Calc();
          maxLikeGR_Calc.setMags(cutCat);         
          double bvalMaxLike = maxLikeGR_Calc.get_bValueMaxLike();
          int numBins = (int)Math.round((GR_MMax-mcLoop)/deltaBin)+1;

          // create the GR distribution of synthetic events
          GutenbergRichterMagFreqDist GR_FMD =
              new GutenbergRichterMagFreqDist(mcLoop,GR_MMax,numBins);
          GR_FMD.setAllButTotMoRate(mcLoop,GR_MMax,sizeCutCat,bvalMaxLike);
          // loop over all bins and get the # of synthetic  events in each bin
          int mIndex = 0;
          double[] mbinRates = new double[numBins];
          for(double mbinLoop = mcLoop; mbinLoop <= GR_MMax; mbinLoop += deltaBin){
            mbinRates[mIndex] = GR_FMD.getIncrRate(mIndex++);
          }

          //create the histogram of the observed events (in mag bins)
          boolean flip = true;
          MagHist hist = new MagHist(magList,mcLoop,GR_MMax,deltaBin);
          int[] numObsInBin = hist.getNumInBins();
          double[] obsCumSum = ListSortingTools.calcCumSum(numObsInBin,flip);
          double sumObs = ListSortingTools.getListSum(obsCumSum);
          double numer = 0;

          // calculate the fit of the synthetic to the real
          for(int sLoop = 0; sLoop < obsCumSum.length; ++ sLoop){
            numer = numer + Math.abs(obsCumSum[sLoop]-mbinRates[sLoop]);
          }
          fitProb[ct] = ((sumObs-numer)/sumObs)*100.0;
        }
       //++ct;  // increment the mcLoop counter
      }
      try{
        double[] mc95List = ListSortingTools.getValsAbove(fitProb,95.0);
        //
         mcSynth = ListSortingTools.getMinVal(mc95List); //????? change to class level variable
      }   catch (NoValsFoundException err1){
        try {
          double[] mc90List = ListSortingTools.getValsAbove(fitProb, 90.0);
           mcSynth = ListSortingTools.getMinVal(mc90List); //???change to class level variable
        }  catch (NoValsFoundException err2){
           mcSynth = Double.NaN; //????change to class level variable
           //err2.printStackTrace();
        }
      }
      // double[] mc95List = ListSortingTools.getValsAbove(fitProb,95.0);
      //double mc95 = ListSortingTools.getMinVal(mc95List);

    }


}
