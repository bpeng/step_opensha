package org.opensha.sra.riskmaps;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;

public class BinaryHazardCurveReader {
	private DataInputStream reader = null;
	private ArrayList<Double> imlvals = new ArrayList<Double>();
	private double latitude, longitude;
	
	public BinaryHazardCurveReader(String filename) throws Exception {
		// Set up the reader
		reader = new DataInputStream(new FileInputStream(filename));
		
		// Pre-populate the IML values
		int imlcount = reader.readInt();
		for ( int i = 0; i < imlcount; ++i ) {
			imlvals.add(reader.readDouble());
		}
	}
	
	public ArbitrarilyDiscretizedFunc nextCurve() throws Exception {
		ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
		try {
			latitude = reader.readDouble();
			longitude = reader.readDouble();
			for ( int i = 0; i < imlvals.size(); ++i ) {
				function.set((double) imlvals.get(i), reader.readDouble());
			}
		} catch (EOFException eof) {
			return null;
		}
		return function;
	}
	
	public double[] currentLocation() {
		double [] loc = new double[2];
		loc[0] = latitude;
		loc[1] = longitude;
		return loc;
	}
	
	public int getNumVals() {
		return imlvals.size();
	}
}
