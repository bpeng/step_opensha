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

package org.opensha.sha.gui.controls;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.param.DoubleDiscreteParameter;
import org.opensha.commons.param.IntegerParameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.editor.ParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.gui.HazardCurveServerModeApplication;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.gui.beans.EqkRupSelectorGuiBean;
import org.opensha.sha.gui.beans.EqkRupSelectorGuiBeanAPI;
import org.opensha.sha.gui.beans.EqkRuptureFromERFSelectorPanel;
import org.opensha.sha.gui.beans.IMT_GuiBean;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.gui.beans.TimeSpanGuiBean;
import org.opensha.sha.gui.servlets.CyberShakeHazardDataSelectorServlet;

/**
 * <p>Title: CyberShakeDeterministicPlotControlPanel </p>
 *
 * <p>Description: This allows to view the Deterministic Cybershake Curves with
 * that of OpenSHA using teh Empirical based AttenuationRelationships.
 * </p>
 *
 * @author Nitin Gupta
 * @since March 7,2006
 * @version 1.0
 */
public class CyberShakePlotControlPanel
    extends JFrame implements ParameterChangeListener{


  private static final boolean D = false;
  public static final String SITE_SELECTOR_PARAM = "CyberShake Site";
  public static final String SA_PERIOD_SELECTOR_PARAM = "SA Period";
  public static final String SRC_INDEX_PARAM = "Source Index";
  public static final String RUP_INDEX_PARAM = "Rupture Index";

  private static final String DETER_PROB_SELECTOR_PARAM = "Curve Type";
  private static final String PROB_CURVE = "Probabilistic Curve";
  private static final String DETER_CURVE = "Deterministic Curve";

  JPanel guiPanel = new JPanel();
  JButton submitButton = new JButton();
  JButton paramSettingButton = new JButton();
  JLabel controlPanelLabel = new JLabel();
  BorderLayout borderLayout1 = new BorderLayout();

  //Curve type selector param
  private StringParameter curveTypeSelectorParam;

  //Site selection param
  private StringParameter siteSelectionParam;
  //SA Period selection param
  private StringParameter saPeriodParam;

  //source index parameter
  private StringParameter srcIndexParam;

  //rupture index parameter
  private IntegerParameter rupIndexParam;

  //Editor to show the parameters in the panel
  private ParameterListEditor listEditor;
  //list to show the parameters
  private ParameterList paramList;

  //handle to the application using this control panel
  private CyberShakePlotControlPanelAPI application;

  //list for getting the SA Period for the selected Site
  private HashMap siteAndSA_PeriodList=null;
  //list for getting the sources for the selected site
  private HashMap siteAndSrcListMap = null;

  //if deterministic curve needs to be plotted
  private boolean isDeterministic ;
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public CyberShakePlotControlPanel(CyberShakePlotControlPanelAPI app) {
    application = app;
    try {
      getSiteInfoForCalc();
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    this.pack();
    Component parent = (Component)app;
    // show the window at center of the parent component
    this.setLocation(parent.getX()+parent.getWidth()/2,
                     parent.getY());
  }

  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);
    guiPanel.setLayout(gridBagLayout1);
    submitButton.setText("OK");
    paramSettingButton.setText("Set Pathway-1 params");
    paramSettingButton.setToolTipText("Sets the same parameters in the Pathway-1\n "+
        "application as in Cybershake calculations.");
    controlPanelLabel.setHorizontalAlignment(SwingConstants.CENTER);
    controlPanelLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    controlPanelLabel.setText("Cybershake Hazard Data Plot Control");
    //creating the Site and SA Period selection for the Cybershake control panel
    initCyberShakeControlPanel();
    this.getContentPane().add(guiPanel, java.awt.BorderLayout.CENTER);
    guiPanel.add(controlPanelLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(6, 10, 0, 12), 145, 23));
    guiPanel.add(listEditor, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 2, 0, 2), 386, 210));
    guiPanel.add(submitButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 17, 1, 114), 20, 0));
    guiPanel.add(paramSettingButton,
                 new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                        , GridBagConstraints.CENTER,
                                        GridBagConstraints.NONE,
                                        new Insets(0, 72, 1, 0), 5, 0));
    submitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        submitButton_actionPerformed(e);
      }
    });
    paramSettingButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        paramSettingButton_actionPerformed(e);
      }
    });
    this.setSize(80,300);
  }


  /**
   * Creates the Cybershake site and SA Period GUI elements.
   * Allows the user to select the Site and SA period value for which
   * hazard curve needs to be plotted.
   */
  private void initCyberShakeControlPanel(){
    ArrayList siteList  = new ArrayList();
    Set set = siteAndSA_PeriodList.keySet();
    Iterator it = set.iterator();
    while(it.hasNext())
      siteList.add((String)it.next());

    ArrayList supportedCurvesType = new ArrayList();
    supportedCurvesType.add(PROB_CURVE);
    supportedCurvesType.add(DETER_CURVE);

    curveTypeSelectorParam = new StringParameter(this.DETER_PROB_SELECTOR_PARAM,
                                                 supportedCurvesType,
                                                 (String)
                                                 supportedCurvesType.get(0));

    paramList = new ParameterList();

    siteSelectionParam = new StringParameter(SITE_SELECTOR_PARAM,
                                             siteList,(String)siteList.get(0));
    initSA_PeriodParam();
    initSrcIndexParam();
    rupIndexParam = new IntegerParameter(RUP_INDEX_PARAM, new Integer(0));
    paramList.addParameter(curveTypeSelectorParam);
    paramList.addParameter(siteSelectionParam);
    paramList.addParameter(saPeriodParam);
    paramList.addParameter(srcIndexParam);
    paramList.addParameter(rupIndexParam);
    siteSelectionParam.addParameterChangeListener(this);
    curveTypeSelectorParam.addParameterChangeListener(this);
    listEditor = new ParameterListEditor(paramList);
    listEditor.setTitle("Set Params for Cybershake Curve");
    makeParamVisible();
  }


  /**
   * Creates the SA Period Parameter which allows the user to select the
   * SA Period for a given site for which hazard data needs to be plotted.
   */
  private void initSA_PeriodParam(){
    String siteName = (String)siteSelectionParam.getValue();
    ArrayList saPeriods = (ArrayList)siteAndSA_PeriodList.get(siteName);
    saPeriodParam = new StringParameter(this.SA_PERIOD_SELECTOR_PARAM,
        saPeriods,(String)saPeriods.get(0));
  }


  /**
   * Makes the parameters visible or invisible based on if it is deterministic
   * or prob. curve.
   */
  private void makeParamVisible() {
    String curveType = (String)curveTypeSelectorParam.getValue();
    if(curveType.equals(PROB_CURVE)) {
      this.isDeterministic = false;
      application.setCurveType(HazardCurveServerModeApplication.PROBABILISTIC);
    }
    else {
      this.isDeterministic = true;
      application.setCurveType(HazardCurveServerModeApplication.DETERMINISTIC);
    }
    
    listEditor.getParameterEditor(SRC_INDEX_PARAM).setVisible(isDeterministic);
    listEditor.getParameterEditor(RUP_INDEX_PARAM).setVisible(isDeterministic);
  }

  /**
   * Creates the parameters displaying all the src index for a given Cybershake
   * site for which deterministic calculations can be done.
   */
  private void initSrcIndexParam(){
    String siteName = (String)siteSelectionParam.getValue();
    TreeSet srcIndexSet = (TreeSet)siteAndSrcListMap.get(siteName);
    Iterator it =srcIndexSet.iterator();
    ArrayList srcIndexList = new ArrayList();
    while(it.hasNext())
      srcIndexList.add(((Integer)it.next()).toString());
    srcIndexParam = new StringParameter(SRC_INDEX_PARAM,srcIndexList,(String)srcIndexList.get(0));
  }


  /**
   * Updates the list editor when user changes the Cybershake site
   * @param e ParameterChangeEvent
   */
  public void parameterChange (ParameterChangeEvent e){
    String paramName = e.getParameterName();
    if(paramName.equals(SITE_SELECTOR_PARAM)){
      getSiteInfoForCalc();
      initSA_PeriodParam();
      initSrcIndexParam();
      listEditor.replaceParameterForEditor(SA_PERIOD_SELECTOR_PARAM,saPeriodParam );
      listEditor.replaceParameterForEditor(SRC_INDEX_PARAM,srcIndexParam);
    }
    if(paramName.equals(DETER_PROB_SELECTOR_PARAM)){
      this.makeParamVisible();
    }
    listEditor.refreshParamEditor();
  }


  /**
   * Gets the hazard data from the Cybershake site for the given SA period.
   * @param cybershakeSite String Cybershake Site
   * @param saPeriod String SA period for which hazard file needs to be read.
   * @return ArrayList Hazard Data
   * @throws RuntimeException
   */
  private DiscretizedFuncAPI getHazardData(String cybershakeSite, String saPeriod) throws RuntimeException{
    DiscretizedFuncAPI cyberShakeHazardData=null;
    try{

      if(D) System.out.println("starting to make connection with servlet");
      URL cybershakeDataServlet = new
                             URL("http://gravity.usc.edu/OpenSHA/servlet/CyberShakeHazardDataSelectorServlet");


      URLConnection servletConnection = cybershakeDataServlet.openConnection();
      if(D) System.out.println("connection established");

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());


      //sending the input parameters to the servlet
      outputToServlet.writeObject(CyberShakeHazardDataSelectorServlet.GET_HAZARD_DATA);
      //sending the cybershake site
      outputToServlet.writeObject(cybershakeSite);
      //sending the sa period
      outputToServlet.writeObject(saPeriod);


      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      cyberShakeHazardData = (DiscretizedFuncAPI) inputToServlet.readObject();
      inputToServlet.close();
    }catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
    return cyberShakeHazardData;
  }



  /**
   *
   * @return HashMap Returns the Hashmap of Cybershake sites and SA period values
   * for each site with keys being the Site name and values being the Arraylist
   * of SA periods for which hazard has been computed.
   * @throws RuntimeException
   */
  private void getSiteInfoForCalc() throws RuntimeException{

    try{

      if(D) System.out.println("starting to make connection with servlet");
      URL cybershakeDataServlet = new
                             URL("http://gravity.usc.edu/OpenSHA/servlet/CyberShakeHazardDataSelectorServlet");


      URLConnection servletConnection = cybershakeDataServlet.openConnection();
      if(D) System.out.println("connection established");

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());


      //sending the ArrayList of the gmt Script Lines
      outputToServlet.writeObject(CyberShakeHazardDataSelectorServlet.GET_CYBERSHAKE_INFO_DETER_CURVE);


      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream outputFromServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      siteAndSA_PeriodList = (HashMap)outputFromServlet.readObject();
      siteAndSrcListMap  = (HashMap)outputFromServlet.readObject();
      outputFromServlet.close();
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
  }


  /**
   * Gets the hazard data from the Cybershake site for the given SA period.
   * @param cybershakeSite String Cybershake Site
   * @param saPeriod String SA period for which hazard file needs to be read.
   * @return ArbitrarilyDiscretizedFunc Determinitic curve data
   * @throws RuntimeException
   */
  private DiscretizedFuncAPI getDeterministicData(String cybershakeSite,
                                                  String saPeriod,
                                                  String srcIndex,
                                                  Integer rupIndex,
                                                  ArrayList imlVals) throws
      RuntimeException {
    DiscretizedFuncAPI cyberShakeDeterminicticHazardCurve = null;
    try {

      if (D) System.out.println("starting to make connection with servlet");
      URL cybershakeDataServlet = new
          URL(
          "http://gravity.usc.edu/OpenSHA/servlet/CyberShakeHazardDataSelectorServlet");

      URLConnection servletConnection = cybershakeDataServlet.openConnection();
      if (D) System.out.println("connection established");

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches(false);
      servletConnection.setDefaultUseCaches(false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty("Content-Type",
                                           "application/octet-stream");

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());

      //sending the input parameters to the servlet
      outputToServlet.writeObject(CyberShakeHazardDataSelectorServlet.
                                  GET_DETERMINISTIC_DATA);
      //sending the cybershake site
      outputToServlet.writeObject(cybershakeSite);
      //sending the sa period
      outputToServlet.writeObject(saPeriod);

      //sending the src Index
      outputToServlet.writeObject(srcIndex);

      //sending the rupture index
      outputToServlet.writeObject(rupIndex);

      //sending the rupture index
      outputToServlet.writeObject(imlVals);

      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
      // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      cyberShakeDeterminicticHazardCurve = (DiscretizedFuncAPI) inputToServlet.
          readObject();
      inputToServlet.close();
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
    return cyberShakeDeterminicticHazardCurve;
  }


  /**
   * Sets the parameters in the OpenSHA application similar to what
   * is required  by the Cybershake.
   * @param actionEvent ActionEvent
   */
  private void paramSettingButton_actionPerformed(ActionEvent actionEvent) {
    setSiteParams();
    boolean imtSet = setIMT_Params();

    if(!imtSet)
      return;
    application.setX_ValuesForHazardCurve(createUSGS_PGA_Function());

    if(isDeterministic)
      setEqkSrcRupSelectorParams();
    else
      setEqkRupForecastParams();
  }

  /**
   * Retreives the Cybershake data and plots it in the application.
   * @param actionEvent ActionEvent
   */
  private void submitButton_actionPerformed(ActionEvent actionEvent) {
    String cyberShakeSite = (String)siteSelectionParam.getValue();
    String saPeriod = (String)saPeriodParam.getValue();
    String srcIndex = (String)srcIndexParam.getValue();
    Integer rupIndex = (Integer)rupIndexParam.getValue();
    ArrayList imlVals = application.getIML_Values();
    DiscretizedFuncAPI curveData = null;

    if(isDeterministic){
      curveData = getDeterministicData(cyberShakeSite,
                                       saPeriod, srcIndex, rupIndex,
                                       imlVals);
      String name = "Cybershake deterministic curve";
      String infoString = "Site = "+ (String)siteSelectionParam.getValue()+
          "; SA-Period = "+(String)saPeriodParam.getValue()+"; SourceIndex = "+(String)srcIndexParam.getValue()+
          "; RuptureIndex = "+((Integer)rupIndexParam.getValue()).intValue();
      curveData.setName(name);
      curveData.setInfo(infoString);
      application.addCybershakeCurveData(curveData);
    }
    else{
      curveData = this.getHazardData(cyberShakeSite, saPeriod);
      String name = "Cybershake hazard curve";
      String infoString = "Site = "+ (String)siteSelectionParam.getValue()+
          "; SA-Period = "+(String)saPeriodParam.getValue();
      curveData.setName(name);
      curveData.setInfo(infoString);
      application.addCybershakeCurveData(curveData);

    }
  }


  /**
   * This sets the site parameters in the OpenSHA application
   * based on the chosen Cybershake site
   */
  private void setSiteParams(){
    Site_GuiBean site = application.getSiteGuiBeanInstance();
    String cyberShakeSite = (String)siteSelectionParam.getValue();
    if(cyberShakeSite.equalsIgnoreCase("USC")){// Setting the Site for USC
      site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(34.019200));
      site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(-118.28600));
    }
    else if(cyberShakeSite.equalsIgnoreCase("PAS")){ //Setting the site for PAS
      site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(34.148427));
      site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(-118.17119));
    }else if(cyberShakeSite.equalsIgnoreCase("CCP")){ // Setting the site for CCP
      site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(34.054884));
      site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(-118.41302));
    }else if(cyberShakeSite.equalsIgnoreCase("FFI")){ //Setting the site for FFI
      site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(34.336030));
      site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(-118.50862));
    }else if(cyberShakeSite.equalsIgnoreCase("LADT")){ //Setting the site for LADT
      site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(34.052041));
      site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(-118.25713));
    }else if(cyberShakeSite.equalsIgnoreCase("LBP")){ //Setting the site for LBP
      site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(33.754944));
      site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(-118.22300));
    }else if(cyberShakeSite.equalsIgnoreCase("WNGC")){ //setting the site for WNGC
      site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(34.041823));
      site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(-118.06530));
    }else if(cyberShakeSite.equalsIgnoreCase("SBSM")){ //setting the site for SBSM
      site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(34.064986));
      site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(-117.29201));
    }else if(cyberShakeSite.equalsIgnoreCase("SABD")){ //setting the site for SABD
      site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(33.754111));
      site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(-117.86778));
    }else if(cyberShakeSite.equalsIgnoreCase("SMCA")){ //setting the site for SMCA
      site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(34.009092));
      site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(-118.48939));
    }

    site.getParameterListEditor().refreshParamEditor();
  }

  /**
   * Set the Eqk Rup Forecast in the OpenSHA application similar to eqk forecast
   * params used to do the cybershake calculations.
   */
  private void setEqkRupForecastParams(){
    ERF_GuiBean gui = application.getEqkRupForecastGuiBeanInstance();
    gui.getERFParameterListEditor().getParameterEditor(gui.ERF_PARAM_NAME).setValue(Frankel02_AdjustableEqkRupForecast.NAME);
    gui.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        FAULT_MODEL_NAME).setValue(Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_STIRLING);
    gui.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_EXCLUDE);
    gui.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        RUP_OFFSET_PARAM_NAME).setValue(new Double(5.0));

    TimeSpanGuiBean timespan = gui.getSelectedERFTimespanGuiBean();
    timespan.getTimeSpan().setDuration(1.0);
    gui.getERFParameterListEditor().refreshParamEditor();
    timespan.getParameterListEditor().refreshParamEditor();
  }

  /**
   * Select the same source and rupture in the OpenSHA application for deterministic calculations,
   * similar to eqk forecast params used to do the cybershake calculations.
   */
  private void setEqkSrcRupSelectorParams(){
    EqkRupSelectorGuiBean erfRupSelectorGuiBean = application.getEqkSrcRupSelectorGuiBeanInstance();
    erfRupSelectorGuiBean.getParameterEditor(erfRupSelectorGuiBean.RUPTURE_SELECTOR_PARAM_NAME).
        setValue(erfRupSelectorGuiBean.RUPTURE_FROM_EXISTING_ERF);

    EqkRupSelectorGuiBeanAPI erfRupSelGuiBean = erfRupSelectorGuiBean.getEqkRuptureSelectorPanel();
    EqkRuptureFromERFSelectorPanel rupGuiBean = (EqkRuptureFromERFSelectorPanel)erfRupSelGuiBean;
    rupGuiBean.showAllParamsForForecast(false);
    rupGuiBean.getParameterListEditor().getParameterEditor(rupGuiBean.ERF_PARAM_NAME).
        setValue(Frankel02_AdjustableEqkRupForecast.NAME);
    ERF_GuiBean erfGuiBean = rupGuiBean.getERF_ParamEditor();

    erfGuiBean.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        FAULT_MODEL_NAME).setValue(Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_STIRLING);
    erfGuiBean.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_EXCLUDE);
    erfGuiBean.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        RUP_OFFSET_PARAM_NAME).setValue(new Double(5.0));

    TimeSpanGuiBean timespan = erfGuiBean.getSelectedERFTimespanGuiBean();
    timespan.getTimeSpan().setDuration(1.0);
    erfGuiBean.getERFParameterListEditor().refreshParamEditor();
    timespan.getParameterListEditor().refreshParamEditor();
    //rupGuiBean.updateERFAndSourceRupList();
    //rupGuiBean.getParameterListEditor().refreshParamEditor();
    try {
      EqkRupForecastAPI erf = (EqkRupForecastAPI) erfGuiBean.getSelectedERF();
      rupGuiBean.setEqkRupForecast(erf);
    }
    catch (InvocationTargetException ex) {
    }
    String srcIndex = (String)srcIndexParam.getValue();
    int srcNum = Integer.parseInt(srcIndex.trim());
    Integer rupIndex = (Integer)rupIndexParam.getValue();
    rupGuiBean.setSourceFromSelectedERF(srcNum);
    rupGuiBean.setRuptureForSelectedSource(rupIndex.intValue());
    rupGuiBean.showAllParamsForForecast(true);
  }

  /**
   * Select the IMT and SA Period in the OpenSHA application similar to that chosen
   * in Cybershake control panel.
   */
  private boolean setIMT_Params(){
    IMT_GuiBean imtGui = application.getIMTGuiBeanInstance();
    DecimalFormat format = new DecimalFormat("0.00");
    imtGui.getParameterEditor(imtGui.IMT_PARAM_NAME).setValue("SA");
    String saPeriodString = (String)saPeriodParam.getValue();
    double saPeriod = Double.parseDouble(format.format(Double.parseDouble(saPeriodString.trim())));
    DoubleDiscreteParameter saPeriodParam = (DoubleDiscreteParameter)imtGui.getParameterEditor("SA Period").getParameter();
    ArrayList allowedVals = saPeriodParam.getAllowedDoubles();
    int size = allowedVals.size();
    double minSaVal = ((Double)allowedVals.get(0)).doubleValue();
    double maxSaVal = ((Double)allowedVals.get(size -1)).doubleValue();
    if ( (saPeriod < minSaVal) || (saPeriod > maxSaVal)) {
      JOptionPane.showMessageDialog(this,
                                    "This attenuation does not support the SA Period\n " +
                                    "selected in cybershake control panel. Either choose a \n different Attenuation " +
                                    "Relationship or a different SA Period");
      return false;
    }
    else {
      for (int i = 0; i < size-1; ++i) {
        double saVal_first = Double.parseDouble(format.format(((Double)allowedVals.get(i)).doubleValue()));
        double saVal_second = Double.parseDouble(format.format(((Double)allowedVals.get(i+1)).doubleValue()));
        if(saPeriod >= saVal_first && saPeriod <= saVal_second){
          if((saPeriod - saVal_first) <= (saVal_second - saPeriod))
            imtGui.getParameterEditor("SA Period").setValue((Double)allowedVals.get(i));
          else
            imtGui.getParameterEditor("SA Period").setValue((Double)allowedVals.get(i+1));
          break;
        }
      }
    }
    imtGui.refreshParamEditor();
    return true;
  }

  /**
   * initialises the function with the x and y values if the user has chosen the USGS-PGA X Vals
   * the y values are modified with the values entered by the user
   */
  private ArbitrarilyDiscretizedFunc createUSGS_PGA_Function() {
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    function.set(.005, 1);
    function.set(.007, 1);
    function.set(.0098, 1);
    function.set(.0137, 1);
    function.set(.0192, 1);
    function.set(.0269, 1);
    function.set(.0376, 1);
    function.set(.0527, 1);
    function.set(.0738, 1);
    function.set(.103, 1);
    function.set(.145, 1);
    function.set(.203, 1);
    function.set(.284, 1);
    function.set(.397, 1);
    function.set(.556, 1);
    function.set(.778, 1);
    function.set(1.09, 1);
    function.set(1.52, 1);
    function.set(2.13, 1);
    return function;
  }

}
