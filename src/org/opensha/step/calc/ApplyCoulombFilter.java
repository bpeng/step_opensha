/**
 * 
 */
package org.opensha.step.calc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.text.DecimalFormat;



import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.earthquake.griddedForecast.STEP_CombineForecastModels;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * @author matthew
 *
 */
public class ApplyCoulombFilter {

	public ApplyCoulombFilter(){
		//this.applyFilter(bggrid);
	}

	public void applyFilter(BackGroundRatesGrid bggrid, UpdateSTEP_Forecast updateModel, ArrayList <STEP_CombineForecastModels>STEP_AftershockForecastList){
		DecimalFormat locFormat = new DecimalFormat("0.0000");
		DecimalFormat magFormat = new DecimalFormat("0.00");

		Location bgLoc;
		HypoMagFreqDistAtLoc bgDistAtLoc;
		double lat;
		double lon;
		double cfScaler;
		double DEPTH = 0;
		double sumRatesBlue = 0;
		double sumRatesRed = 0;
		double totalR = 0;

		int bgRegionSize;

		boolean isLoc;
		boolean isAS_Loc;
		ArrayList<String> coulombFileLines = null;
		LocationList bgLocList = bggrid.getRegion().getNodeList();	
		bgRegionSize = bgLocList.size();
		double[] cumBGList = new double[bgRegionSize];
		int[] cfs = new int[bgRegionSize];
		int numAftershockModels = STEP_AftershockForecastList.size();
		STEP_CombineForecastModels forecastModel;

		for (int modelLoop = 0; modelLoop < numAftershockModels; ++modelLoop){
			forecastModel = (STEP_CombineForecastModels)STEP_AftershockForecastList.get(modelLoop);
			if(forecastModel.getMainShock().getMag() > RegionDefaults.minCoulombMS_Mag){
				// get the list of locations for the aszone of the main mainshock
				LocationList gridLocs = forecastModel.getAfterShockZone().getNodeList();
				// get the distances of these locations from the fault
				double[] distanceFromFault = forecastModel.getGridDistanceFromFault();
				// number of grid nodes in aftershock zone
				double numAS_Locs = forecastModel.getAfterShockZone().getNodeCount();


				for(int k=0;k <  bgRegionSize ;++k){
					bgDistAtLoc = bggrid.getHypoMagFreqDistAtLoc(k);
					cumBGList[k] = bgDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minForecastMag);
					//totalR = totalR + cumBGList[k];
				}


				try {
					System.out.println("load Coulomb fileName " + new File(RegionDefaults.coulombFilterPath).getAbsolutePath());
					coulombFileLines = FileUtils.loadFile( RegionDefaults.coulombFilterPath );
				} catch(Exception e) {

					throw new RuntimeException("Coulomb filter file could not be loaded");
				}

				// load the coulomb file and iterate thru it to find the match in the rate file
				ListIterator<String> it = coulombFileLines.listIterator();
				StringTokenizer st;	
				while( it.hasNext() ) {
					// get next line
					String line = it.next().toString();
					//logger.info(" loadBackGroundGridFromFile line=" + line);
					st = new StringTokenizer(line);

					// get lat and lon
					lon =  Double.parseDouble(st.nextToken());
					lat =  Double.parseDouble(st.nextToken());
					cfScaler = Double.parseDouble(st.nextToken());
					Location cfLoc = new Location(lat,lon,DEPTH);
					//int dumc = 0;
					// assign the coulomb scaler(0,1) to the appropriate bg index
					for(int k=0;k<bgRegionSize;++k){
						bgDistAtLoc = bggrid.getHypoMagFreqDistAtLoc(k);
						isLoc = cfLoc.equalsLocation(bgLocList.getLocationAt(k));
						if(isLoc){
							
							if((int)cfScaler==0) cfs[k]=1;
							if((int)cfScaler==1) cfs[k]=2;
							//cfs[k] = (int)cfScaler;

							// if the location is with XX radius from the fault, set the cfs flag
							// to 2, so that the STEP only forecast will be used for these locs
							// these locations will be ignored in the total sum
							for(int as=0;as<numAS_Locs;++as){
								isAS_Loc = cfLoc.equalsLocation(gridLocs.getLocationAt(as));
								if(isAS_Loc) {
									if(distanceFromFault[as]<=RegionDefaults.coulombFaultRadius){
										cfs[k]=0;
										System.out.println("Coul in Aftershock Zone");
									}
								}
							}

							if(cfs[k]==1){
								sumRatesBlue = sumRatesBlue+ bgDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minForecastMag);
								//dumc++;
								//System.out.println("Blue "+lat+"   "+lon);
							}
							else if(cfs[k]==2){
								sumRatesRed = sumRatesRed+ bgDistAtLoc.getFirstMagFreqDist().getCumRate(RegionDefaults.minForecastMag);
								//dumc++;
								//System.out.println("Red "+lat+"   "+lon);
							}
						}
					}

				}
			}
		}
		totalR = sumRatesBlue+sumRatesRed;
		// these determine how much you need to scale the rates up or down
		// in the red and blue regions to match the a priori percents
		double scalerBlue = (totalR*RegionDefaults.bluePercent)/sumRatesBlue;
		double scalerRed = (totalR*RegionDefaults.redPercent)/sumRatesRed;
		double grid_scaler;
		double scaled_rate;
		double wrate;
		EvenlyDiscretizedFunc rates;
		// now apply the scaler to the rates.
		for(int k=0;k<bgRegionSize;++k){

			grid_scaler = 1.0;
			if(cfs[k]==1){
				grid_scaler = scalerBlue;
			}
			else if(cfs[k]==2){
				grid_scaler = scalerRed;
			}
			//System.out.println("Scaler "+grid_scaler);
			HypoMagFreqDistAtLoc fmd = bggrid.getHypoMagFreqDistAtLoc(k);
			int numForecastMags = 1 + (int) ( (RegionDefaults.maxForecastMag - RegionDefaults.minForecastMag) / RegionDefaults.deltaForecastMag);
			IncrementalMagFreqDist magFreqDist = new IncrementalMagFreqDist(RegionDefaults.minForecastMag, RegionDefaults.maxForecastMag, numForecastMags);
			IncrementalMagFreqDist scaled_fmd = new IncrementalMagFreqDist(RegionDefaults.minForecastMag, RegionDefaults.maxForecastMag, numForecastMags);

			magFreqDist = fmd.getFirstMagFreqDist();
			rates = magFreqDist.getCumRateDist();
			
			int nr = rates.getNum();
			//scaled_rate = new double[nr];
			for(int i=0;i<nr;i++){
				if(i<nr-1){
					scaled_rate =(rates.getY(i)-rates.getY(i+1))*grid_scaler; 
				}
				else{
					scaled_rate =rates.getY(i)*grid_scaler; 
				}
				//System.out.println(scaled_rate);
				scaled_fmd.add(i, scaled_rate);
			}
			bggrid.setMagFreqDistAtLoc(scaled_fmd, k);	

		}

		try{
			//File existingFile = new File(RegionDefaults.outputCoulombRatePath);
			FileWriter fr = new FileWriter(RegionDefaults.outputCoulombRatePath);
			bgRegionSize = bgLocList.size();

			for(int k=0;k <  bgRegionSize ;++k){
				bgLoc = bgLocList.getLocationAt(k);
				//get the hypoMag for the location				
				bgDistAtLoc = bggrid.getHypoMagFreqDistAtLoc(k); //k

				if(bgDistAtLoc != null){	
					//if(RegionDefaults.MODEL_FORMAT == RegionDefaults.MODEL_FORMAT_CSEP){
						int numForecastMags = 1 + (int) ( (RegionDefaults.maxForecastMag - RegionDefaults.minForecastMag) / RegionDefaults.deltaForecastMag);
						//log("numForecastMags=" + numForecastMags  );						
						for(int index = 0; index < numForecastMags; index++){
							double mag = RegionDefaults.minForecastMag  + index*RegionDefaults.deltaForecastMag;
							//log("1 mag=" + mag  );
							//double rate = bgDistAtLoc.getFirstMagFreqDist().getCumRate(mag);
							wrate = bgDistAtLoc.getFirstMagFreqDist().getIncrRate(mag);
							// make sure there are no ZERO rate bins
							if(wrate == 0) wrate = 1E-11;
							//if(rate > 0) log("2 mag=" + mag + " rate=" + rate);
							fr.write(locFormat.format(bgLoc.getLongitude() - RegionDefaults.gridSpacing/2)//minLon
									+ "    " + locFormat.format(bgLoc.getLongitude() + RegionDefaults.gridSpacing/2) //maxLon 
									+ "    " + locFormat.format(bgLoc.getLatitude() - RegionDefaults.gridSpacing/2) //minLat
									+ "    " + locFormat.format(bgLoc.getLatitude() + RegionDefaults.gridSpacing/2)  //maxLat
									+ "    " +  RegionDefaults.MIN_Z //minZ
									+ "    " + RegionDefaults.MAX_Z  //maxZ							
									+ "    " + magFormat.format(mag - RegionDefaults.deltaForecastMag /2) //minMag
									+ "    " + magFormat.format(mag + RegionDefaults.deltaForecastMag /2)  //maxMag						
									+ "    " +  wrate
									+ "     1\n");  // masking bit 1=valid forecast; 0=not valid 
						}
					}
				//}
			}
			fr.close();			

		}catch(IOException ee){
			ee.printStackTrace();

		}
	}
}


