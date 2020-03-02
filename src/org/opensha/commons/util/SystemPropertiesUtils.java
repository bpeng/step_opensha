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

package org.opensha.commons.util;

/**
 * <p>Title: SystemPropertiesUtils</p>
 * <p>Description: Gets the System properties of the current user</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public final class SystemPropertiesUtils {

  private final static String SYSTEM_LINE_SEPERATOR = System.getProperty("line.separator");
  private final static String SYSTEM_FILE_SEPERATOR = System.getProperty("file.separator");
  private final static String SYSTEM_OS = System.getProperty("os.name");

  /**
   *
   * @returns the System specific line separtor
   */
  public static String getSystemLineSeparator(){
    return SYSTEM_LINE_SEPERATOR;
  }

  /**
   *
   * @returns the System specific file separator
   */
  public static String getSystemFileSeparator(){
    return SYSTEM_FILE_SEPERATOR;
  }

  /**
   *
   * @returns the System specific system name
   */
  public static String getSystemName(){
    return SYSTEM_OS;
  }

}
