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

package org.opensha.sha.gui.infoTools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;

/**
 * <p>Title: AtteuationRelationshipsInstance </p>
 * <p>Description: Creates the list of the AttenuationRelationship Objects from
 * their classnames.</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created March 05,2004
 * @version 1.0
 */

public class AttenuationRelationshipsInstance {

	private static final String C= "AtteuationRelationshipsInstance";

	/**
	 *  The object class names for all the supported attenuation ralations (IMRs)
	 *  Temp until figure out way to dynamically load classes during runtime
	 */
	public final static String BJF_1997_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.BJF_1997_AttenRel";
	public final static String BA_2008_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel";
	public final static String AS_1997_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.AS_1997_AttenRel";
	public final static String C_1997_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.Campbell_1997_AttenRel";
	public final static String SCEMY_1997_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.SadighEtAl_1997_AttenRel";
	public final static String F_2000_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.Field_2000_AttenRel";
	public final static String A_2000_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.Abrahamson_2000_AttenRel";
	public final static String CB_2003_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.CB_2003_AttenRel";
	public final static String SM_2003_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel";
	public final static String USGS_2004_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel";
	public final static String AS_2005_PRELIM_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.depricated.AS_2005_prelim_AttenRel";
	public final static String CB_2006_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.depricated.CB_2006_AttenRel";
	public final static String CB_2008_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel";
	public final static String CY_2006_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.depricated.CY_2006_AttenRel";
	public final static String Boore_2006_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.depricated.BA_2006_AttenRel";
	public final static String CS_2005_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.CS_2005_AttenRel";
	//public final static String SS_2006_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.SiteSpecific_2006_AttenRel";
	public final static String BS_2003_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.BS_2003_AttenRel";
	public final static String BC_2004_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.BC_2004_AttenRel";
	public final static String GouletEtAl_2006_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.GouletEtAl_2006_AttenRel";
	public final static String AS_2008_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.AS_2008_AttenRel";
	public final static String CY_2008_CLASS_NAME = "org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel";
	public final static String CyberShake_CLASS_NAME = "org.opensha.sha.cybershake.openshaAPIs.CyberShakeIMR";
	public final static String McVerryetal_2000_CLASS_NAME="org.opensha.sha.imr.attenRelImpl.McVerryetal_2000_AttenRel";
    
	//arrayList to store the supported AttenRel Class Names with their full package structure.
	private ArrayList<String> supportedAttenRelClasses;
	
	public static ArrayList<String> getDefaultIMRClassNames() {
		ArrayList<String> supportedAttenRelClasses = new ArrayList<String>();
		
		//adds all the AttenRel classes to the ArrayList
		// ******** ORDER THEM BY YEAR, NEWEST FIRST ********
		// 2009
		
		// 2008
		supportedAttenRelClasses.add(CB_2008_CLASS_NAME);
		supportedAttenRelClasses.add(BA_2008_CLASS_NAME);
		supportedAttenRelClasses.add(AS_2008_CLASS_NAME);
		supportedAttenRelClasses.add(CY_2008_CLASS_NAME);
		// 2007
		
		// 2006
		supportedAttenRelClasses.add(Boore_2006_CLASS_NAME);
		supportedAttenRelClasses.add(CB_2006_CLASS_NAME);
		supportedAttenRelClasses.add(CY_2006_CLASS_NAME);
		supportedAttenRelClasses.add(GouletEtAl_2006_CLASS_NAME);
		// 2005
		supportedAttenRelClasses.add(CS_2005_CLASS_NAME);
		// 2004
		supportedAttenRelClasses.add(BC_2004_CLASS_NAME);
		supportedAttenRelClasses.add(USGS_2004_CLASS_NAME);
		// 2003
		supportedAttenRelClasses.add(BS_2003_CLASS_NAME);
		supportedAttenRelClasses.add(CB_2003_CLASS_NAME);
		supportedAttenRelClasses.add(SM_2003_CLASS_NAME);
		// 2002
		
		// 2001
		
		// 2000
		supportedAttenRelClasses.add(F_2000_CLASS_NAME);
		supportedAttenRelClasses.add(A_2000_CLASS_NAME);
		supportedAttenRelClasses.add(McVerryetal_2000_CLASS_NAME);
		// 1999
		
		// 1998
		
		// 1997
		supportedAttenRelClasses.add(AS_1997_CLASS_NAME);
		supportedAttenRelClasses.add(BJF_1997_CLASS_NAME);
		supportedAttenRelClasses.add(C_1997_CLASS_NAME);
		supportedAttenRelClasses.add(SCEMY_1997_CLASS_NAME);
		
		// OTHER
//		supportedAttenRelClasses.add(CyberShake_CLASS_NAME);
		return supportedAttenRelClasses;
	}

	/**
	 * class default constructor
	 */
	public AttenuationRelationshipsInstance(){
		this(getDefaultIMRClassNames());
	}
	
	/**
	 * constructor for giving your own custom class names
	 */
	public AttenuationRelationshipsInstance(ArrayList<String> classNames){
		setIMR_ClassNames(classNames);
	}

	/**
	 * This method takes in a custom list of IMR class names that are used when
	 * createIMRClassInstance is called.
	 * 
	 * @param classNames an ArrayList of IMR class names to be included.
	 */

	public void setIMR_ClassNames(ArrayList<String> classNames) {
		supportedAttenRelClasses = classNames;
	}

	/**
	 * Creates a class instance from a string of the full class name including packages.
	 * This is how you dynamically make objects at runtime if you don't know which\
	 * class beforehand. For example, if you wanted to create a BJF_1997_AttenRel you can do
	 * it the normal way:<P>
	 *
	 * <code>BJF_1997_AttenRel imr = new BJF_1997_AttenRel()</code><p>
	 *
	 * If your not sure the user wants this one or AS_1997_AttenRel you can use this function
	 * instead to create the same class by:<P>
	 *
	 * <code>BJF_1997_AttenRel imr =
	 * (BJF_1997_AttenRel)ClassUtils.createNoArgConstructorClassInstance("org.opensha.sha.imt.attenRelImpl.BJF_1997_AttenRel");
	 * </code><p>
	 *
	 */

	public ArrayList<ScalarIntensityMeasureRelationshipAPI> 
			createIMRClassInstance(ParameterChangeWarningListener listener){
		
		ArrayList<ScalarIntensityMeasureRelationshipAPI> AttenRelObjects = 
			new ArrayList<ScalarIntensityMeasureRelationshipAPI>();
		String S = C + ": createIMRClassInstance(): ";
		int size = supportedAttenRelClasses.size();
		
		for(int i=0;i< size;++i){
			Object obj = createIMRClassInstance(listener, supportedAttenRelClasses.get(i));
			AttenRelObjects.add((AttenuationRelationship)obj);
		}
		
		Collections.sort(AttenRelObjects, new ImrComparator());
		return AttenRelObjects;
	}

	/**
	 * Creates a class instance from a string of the full class name including packages.
	 * This is how you dynamically make objects at runtime if you don't know which\
	 * class beforehand. For example, if you wanted to create a BJF_1997_AttenRel you can do
	 * it the normal way:<P>
	 *
	 * <code>BJF_1997_AttenRel imr = new BJF_1997_AttenRel()</code><p>
	 *
	 * If your not sure the user wants this one or AS_1997_AttenRel you can use this function
	 * instead to create the same class by:<P>
	 *
	 * <code>BJF_1997_AttenRel imr =
	 * (BJF_1997_AttenRel)ClassUtils.createNoArgConstructorClassInstance("org.opensha.sha.imt.attenRelImpl.BJF_1997_AttenRel");
	 * </code><p>
	 *
	 */

	public AttenuationRelationship createIMRClassInstance( org.opensha.commons.param.event.ParameterChangeWarningListener listener, String className){
		String S = C + ": createIMRClassInstance(): ";
		try {
		    // KLUDGY why is this class hardcoded and dynamically loaded
			Class listenerClass = Class.forName( "org.opensha.commons.param.event.ParameterChangeWarningListener" );
			Object[] paramObjects = new Object[]{ listener };
			Class[] params = new Class[]{ listenerClass };
			Class imrClass = Class.forName(className);
			Constructor con = imrClass.getConstructor( params );
			Object obj = con.newInstance( paramObjects );
			return (AttenuationRelationship)obj;
		} catch ( ClassCastException e ) {
			System.out.println(S + e.toString());
			throw new RuntimeException( S + e.toString() );
		} catch ( ClassNotFoundException e ) {
			System.out.println(S + e.toString());
			throw new RuntimeException( S + e.toString() );
		} catch ( NoSuchMethodException e ) {
			System.out.println(S + e.toString());
			throw new RuntimeException( S + e.toString() );
		} catch ( InvocationTargetException e ) {
			e.printStackTrace();
			System.out.println(S + e.toString());
			throw new RuntimeException( S + e.toString() );
		} catch ( IllegalAccessException e ) {
			System.out.println(S + e.toString());
			throw new RuntimeException( S + e.toString() );
		} catch ( InstantiationException e ) {
			System.out.println(S + e.toString());
			throw new RuntimeException( S + e.toString() );
		}
	}

	private static class ImrComparator implements 
			Comparator<ScalarIntensityMeasureRelationshipAPI> {

		public int compare(
				ScalarIntensityMeasureRelationshipAPI imr1,
				ScalarIntensityMeasureRelationshipAPI imr2) {
			return imr1.getName().compareToIgnoreCase(imr2.getName());
		}
	}

}
