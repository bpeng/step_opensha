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
 * <p>Title: ByteSwapUtil</p>
 *
 * <p>Description: This program swaps the byte ordering and read the data value.
 * This progarm can be used when a binary data file created with "Little Endian" byte
 * ordering technique is being read by a java program . </p>
 * <p>Note: Java always reads binary files in "Big Endian" format.</p>
 * <p>This program will swap the byte ordering of the bytes read by java program,
 * so as to get the correct value data.</p>
 * @author not attributable
 * @version 1.0
 */
@Deprecated // TODO replace with commons-io EndianUtils
public final class ByteSwapUtil {

  /**
   * Swaps the byte ordering for the integer value
   * @param value int
   * @return int
   */
  public static int swap(int value) {
    int b1 = (value >> 0) & 0xff;
    int b2 = (value >> 8) & 0xff;
    int b3 = (value >> 16) & 0xff;
    int b4 = (value >> 24) & 0xff;

    return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
  }

  /**
   * Swaps the byte prdering for the float value
   * @param value float
   * @return float
   */
  public static float swap(float value) {
    int intValue = Float.floatToIntBits(value);
    intValue = swap(intValue);
    return Float.intBitsToFloat(intValue);
  }

  /**
   * Swaps the bits of an int and returns the
   * corresponding float value. This method resolves
   * issues with big vs. little endian datastorage where
   * a valid little endian float, when read by a big
   * endian cpu becomes NaN.  This will preserve the true
   * little endian value. (And vice-versa).
   *
   * @param value The value to swap
   * @return The corresponding float value
   */
  public static float swapIntToFloat(int value) {
      int b1 = (value >> 0) & 0xff;
      int b2 = (value >> 8) & 0xff;
      int b3 = (value >> 16) & 0xff;
      int b4 = (value >> 24) & 0xff;

      return Float.intBitsToFloat(b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0);
  }

  
  
  
  
  /**
   * Byte swap a single short value.
   *
   * @param value  Value to byte swap.
   * @return       Byte swapped representation.
   */
  public static short swap(short value) {

    int b1 = value & 0xff;
    int b2 = (value >> 8) & 0xff;

    return (short) (b1 << 8 | b2 << 0);
  }

  /**
   * Byte swap a single long value.
   *
   * @param value  Value to byte swap.
   * @return       Byte swapped representation.
   */
  public static long swap(long value) {
    long b1 = (value >> 0) & 0xff;
    long b2 = (value >> 8) & 0xff;
    long b3 = (value >> 16) & 0xff;
    long b4 = (value >> 24) & 0xff;
    long b5 = (value >> 32) & 0xff;
    long b6 = (value >> 40) & 0xff;
    long b7 = (value >> 48) & 0xff;
    long b8 = (value >> 56) & 0xff;

    return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 |
        b5 << 24 | b6 << 16 | b7 << 8 | b8 << 0;
  }

  public static void main(String[] args) {
	  
//	  int tmp_i = 1234567890;
//	  System.out.println(tmp_i);
//	  System.out.println(swap(tmp_i));
//	  System.out.println(EndianUtils.swapInteger(tmp_i));
//	  
//	  float tmp_f = 123.456789f;
//	  System.out.println(tmp_f);
//	  System.out.println(swap(tmp_f));
//	  System.out.println(EndianUtils.swapFloat(tmp_f));
//	  
//	  short tmp_s = 1234;
//	  System.out.println(tmp_s);
//	  System.out.println(swap(tmp_s));
//	  System.out.println(EndianUtils.swapShort(tmp_s));
//	  
//	  long tmp_l = 1234467890123456789L;
//	  System.out.println(tmp_l);
//	  System.out.println(swap(tmp_l));
//	  System.out.println(EndianUtils.swapLong(tmp_l));
	  
	  
	  
	  
  }
}
