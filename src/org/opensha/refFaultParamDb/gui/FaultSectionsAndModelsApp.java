/**
 * 
 */
package org.opensha.refFaultParamDb.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.opensha.refFaultParamDb.gui.addEdit.deformationModel.EditDeformationModel;
import org.opensha.refFaultParamDb.gui.addEdit.faultModel.AddEditFaultModel;
import org.opensha.refFaultParamDb.gui.view.FaultSectionsDistanceCalcGUI;
import org.opensha.refFaultParamDb.gui.view.ViewFaultSection;

/**
 *  This class creates the GUI to allow the user to view/edit fault sections, fault models and deformation models
 * 
 * @author vipingupta
 *
 */
public class FaultSectionsAndModelsApp extends JFrame {
	private JTabbedPane tabbedPane = new JTabbedPane();
	private final static String FAULT_SECTION = "Fault Section";
	private final static String FAULT_MODEL = "Fault Model";
	private final static String DEFORMATION_MODEL = "Deformation Model";
	private final static String FAULT_SECTIONS_DIST_CALC = "Fault Sections Distance Calc";
	
	public FaultSectionsAndModelsApp() {
		tabbedPane.addTab(FAULT_SECTION, new JScrollPane(new ViewFaultSection()));
		tabbedPane.addTab(FAULT_MODEL, new JScrollPane(new AddEditFaultModel()));
		tabbedPane.addTab(DEFORMATION_MODEL, new JScrollPane(new EditDeformationModel()));
		tabbedPane.addTab(FAULT_SECTIONS_DIST_CALC, new JScrollPane(new FaultSectionsDistanceCalcGUI()));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.show();
	}

}
