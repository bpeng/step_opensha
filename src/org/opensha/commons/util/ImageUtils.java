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

import java.awt.Image;
import java.awt.Toolkit;
/**
 * <p>Title: ImageUtils.java </p>
 * <p>Description: This class will be used to load the image from the jar file.
 *   The image will be used to be shown in frames.  </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class ImageUtils {

  /**
   * this is the path where images will be put into
   */
  private static final String imagePath = "/resources/images/";


  /**
   * this method accepts the filename and loads the image from the jar file
   * @param fileName
   * @return
   */
  public static Image loadImage(String fileName) {
    String imageFileName = imagePath+fileName;
    java.net.URL url = ImageUtils.class.getResource(imageFileName);
    Image img=Toolkit.getDefaultToolkit().getImage(url);
    return img;
  }


}

