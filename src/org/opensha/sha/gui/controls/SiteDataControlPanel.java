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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.data.siteData.gui.beans.OrderedSiteDataGUIBean;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.event.AttenuationRelationshipChangeEvent;
import org.opensha.sha.imr.event.AttenuationRelationshipChangeListener;
import org.opensha.sha.util.SiteTranslator;

public class SiteDataControlPanel extends JFrame implements AttenuationRelationshipChangeListener,
					ActionListener, ChangeListener {
	
	private IMR_GuiBean imrGuiBean;
	private Site_GuiBean siteGuiBean;
	private OrderedSiteDataGUIBean dataGuiBean;
	
	private ScalarIntensityMeasureRelationshipAPI attenRel;
	
	private JPanel mainPanel = new JPanel(new BorderLayout());
	
	private JButton setButton = new JButton("Set IMR Params");
	private JButton viewButton = new JButton("View Data");
	
	private SiteTranslator trans = null;
	
	public SiteDataControlPanel(Component parent, IMR_GuiBean imrGuiBean,
            Site_GuiBean siteGuiBean) {
		this.imrGuiBean = imrGuiBean;
		this.siteGuiBean = siteGuiBean;
		
		attenRel = imrGuiBean.getSelectedIMR_Instance();
		imrGuiBean.addAttenuationRelationshipChangeListener(this);
		
		dataGuiBean = new OrderedSiteDataGUIBean(OrderedSiteDataProviderList.createCachedSiteDataProviderDefaults(), attenRel);
		
		viewButton.addActionListener(this);
		setButton.addActionListener(this);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		
		bottomPanel.add(setButton);
		bottomPanel.add(viewButton);
		
		mainPanel.add(dataGuiBean, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		enableButtons();
		dataGuiBean.getProviderList().addChangeListener(this);
		
		this.setContentPane(mainPanel);
		this.setSize(OrderedSiteDataGUIBean.width, 600);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void attenuationRelationshipChange(AttenuationRelationshipChangeEvent event) {
		dataGuiBean.setAttenuationRelationship(event.getNewAttenRel());
		enableButtons();
	}
	
	public void setSiteParams() {
		ArrayList<SiteDataValue<?>> data = loadData();
		
		if (data == null || data.size() == 0)
			return;
		
		for (SiteDataValue<?> val : data) {
			System.out.println(val);
		}
		
		if (trans == null) {
			trans = new SiteTranslator();
		}
		
		trans.setAllSiteParams(imrGuiBean.getSelectedIMR_Instance(), data);
		
		this.siteGuiBean.getParameterListEditor().refreshParamEditor();
		this.dispose();
	}
	
	public void displayData(ArrayList<SiteDataValue<?>> datas) {
		OrderedSiteDataGUIBean.showDataDisplayDialog(datas, this);
	}
	
	private ArrayList<SiteDataValue<?>> loadData() {
		return loadData(false);
	}
	
	private ArrayList<SiteDataValue<?>> loadData(boolean all) {
		OrderedSiteDataProviderList list = dataGuiBean.getProviderList();
		Location loc = siteGuiBean.getSite().getLocation();
		
		if (all)
			return list.getAllAvailableData(loc);
		else
			return list.getBestAvailableData(loc);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == setButton) {
			this.setSiteParams();
		} else if (e.getSource() == viewButton) {
			ArrayList<SiteDataValue<?>> data = loadData(true);
			this.displayData(data);
		}
	}
	
	private void enableButtons() {
		boolean enable = dataGuiBean.getProviderList().isAtLeastOneEnabled();
		setButton.setEnabled(enable);
		viewButton.setEnabled(enable);
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == dataGuiBean.getProviderList())
			enableButtons();
	}
}
