package org.opensha.step.calc;

/**
 * <p>Title: MagHist</p>
 * <p>Description: counts the number of events within each bin as defined
*  by minMag, maxMag and deltaBin </p>\
 * @author Matt Gerstenberger 2004
 * @version 1.0
 */



public class MagHist {

  int[] bins;
  int numBins;
  private  double[] binEdges;
  private  double minMag,maxMag;
  private  double deltaBin;

  private  int overFlows=0,underFlows=0;

  /**
   * default constructor
   */
  public MagHist (){
  } 

  public MagHist(double[] magList, double minMag, double maxMag, double deltaBin) {	
	  setMags(magList, minMag,maxMag,deltaBin);
  }


 /**
   *  get the number of events in each bin
   * @return int[] bins
   */
  public  int[] getNumInBins(){
    return bins;
  }

  public  double[] getBinEdges(){
    return binEdges;
  }

  /**
   * Define the magnitudes of interest and make the call to sort into mag bins
   * @param magList double[]
   */
  public  void setMags(double[] magList, double _minMag, double _maxMag,
                             double _deltaBin){
	//!!!set field values
    minMag = _minMag;
    maxMag = _maxMag;
    deltaBin = _deltaBin;
	  
	  
    numBins = (int)java.lang.Math.round((maxMag-minMag)/deltaBin); //???
   // System.out.println(" numBins " + numBins);    
    bins = new int[numBins];
    binEdges = new double[numBins];
    for(int bLoop=0;bLoop<=numBins-1;++bLoop){
      binEdges[bLoop]=minMag+bLoop*deltaBin;
    }
    calcHist(magList);
  }

  /**
   * sort into magnitude bins
   * !! there is an issue in this method, the maxMag is excluded
   * @param magList double[]
   */
  private  void calcHist(double[] magList){

    int size = magList.length;
   
 // TODO check the maxMag is included, refer to above comments
   for(int magLoop = 0;magLoop < size; ++magLoop){ 
	   
     if( magList[magLoop] < minMag)
       underFlows++;
     else if ( magList[magLoop] > maxMag) ///??? why ignore the max value??? 
       overFlows++;
     else{
       int bin = (int)((magList[magLoop]-minMag)/deltaBin);

       if(bin >=0 && bin < numBins) bins[bin]++;
     }
   }
  }

  /**
   * main method for testing
   * @param args String[]
   */
  public static void main(String[] args) {
    double deltaBin = 0.1;
    double minMag = 3.0;
    double maxMag = 8.0;
    double[] magList = new double[10];
    double startMag = 3;
    for(int synMag = 0;synMag<10;++synMag){
      magList[synMag] = startMag;
      ++startMag;
    }
    MagHist hist = new MagHist();
    hist.setMags(magList,minMag,maxMag,deltaBin);
    int[] numInBins = hist.getNumInBins();
    int length = numInBins.length;
    for(int lLoop = 1; lLoop<length; ++lLoop){
        System.out.println("Number in bins is: " + numInBins[lLoop-1] +
                           " Lower Bin Edge is: " + hist.getBinEdges()[lLoop-1]);
    }
  }

}

