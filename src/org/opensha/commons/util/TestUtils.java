package org.opensha.commons.util;

public class TestUtils {
	
	public static double getPercentDiff(double testVal, double targetVal){
		//comparing each value we obtained after doing the IMR calc with the target result
		//and making sure that values lies with the .01% range of the target values.
		//comparing if the values lies within the actual tolerence range of the target result
		double result = 0;
		if(targetVal!=0)
			result =(StrictMath.abs(testVal-targetVal)/targetVal)*100d;

		return result;
	}

}
