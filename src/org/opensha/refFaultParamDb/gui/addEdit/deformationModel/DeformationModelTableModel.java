/**
 * 
 */
package org.opensha.refFaultParamDb.gui.addEdit.deformationModel;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.table.DefaultTableModel;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;

/**
 * 
 * Deformation model table model
 * @author vipingupta
 *
 */
public class DeformationModelTableModel  extends DefaultTableModel  {
	  	private final static String []columnNames = { "Section Name", "Slip Rate", "Aseismic Slip Factor"};
	  	private int deformationModelId;
	  	private ArrayList faultSectionsInModel;
	  	private HashMap faultSectionsSummaryMap = new HashMap();
	  	private FaultSectionVer2_DB_DAO faultSectionDB_DAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	  	private DeformationModelDB_DAO deformationModelDAO = new DeformationModelDB_DAO(DB_AccessAPI.dbConnection);
	  	private ArrayList faultSectionSummries;
	  	
	  	public  DeformationModelTableModel() {
	  		faultSectionSummries = faultSectionDB_DAO.getAllFaultSectionsSummary();
	  		for(int i=0; i<faultSectionSummries.size(); ++i) {
	  			FaultSectionSummary faultSectionSummary = (FaultSectionSummary)faultSectionSummries.get(i);
	  			faultSectionsSummaryMap.put(new Integer(faultSectionSummary.getSectionId()), faultSectionSummary.getSectionName());
	    	}
	  	}
	
	  	
	  	public void setDeformationModel(int deformationModelId, ArrayList faultSectionIdList) {
	  		this.deformationModelId = deformationModelId;
	  		faultSectionsInModel = new ArrayList();
	  		for(int i=0; i<faultSectionSummries.size(); ++i) {
	  			FaultSectionSummary faultSectionSummary = (FaultSectionSummary)faultSectionSummries.get(i);
	  			if(faultSectionIdList.contains(new Integer(faultSectionSummary.getSectionId())))
	  				faultSectionsInModel.add(new Integer(faultSectionSummary.getSectionId()));
	  		}
	  	}
	  	
	  	public int getdeformationModelId() {
	  		return deformationModelId;
	  	}

		public int getColumnCount() {
			return columnNames.length;
		}
		
		public Class getColumnClass(int col) {
			if(col==0) return String.class;
			else return EstimateInstances.class;
		}
		

	    public int getRowCount() {
	    	int numRows = 0;
	    	if(faultSectionsInModel!=null)  numRows =  faultSectionsInModel.size();
	    	return numRows;
	    }

	      public String getColumnName(int col) {
	          return columnNames[col];
	      }
	      
	      public int getFaultSectionId(int row) {
	    	  return ((Integer)faultSectionsInModel.get(row)).intValue();
	      }

	      public Object getValueAt(int row, int col) {
	    	  int faultSectionId= ((Integer)faultSectionsInModel.get(row)).intValue();
	    	  return faultSectionsSummaryMap.get(new Integer(faultSectionId));
	      }
	      
	      public Object getValueForSlipAndAseismicFactor(int row, int col) {
	    	  int faultSectionId= ((Integer)faultSectionsInModel.get(row)).intValue();
	    	  if(col==2) { //aseismic slip factor
	    		  return deformationModelDAO.getAseismicSlipEstimate(deformationModelId, faultSectionId);
	    	  } else if(col==1) { // slip rate
	    		  return deformationModelDAO.getSlipRateEstimate(deformationModelId, faultSectionId);
	    	  }
	    	  return faultSectionsInModel.get(row);
	      }
	      
	     
	      /*
	       * Don't need to implement this method unless your table's
	       * editable.
	       */
	      public boolean isCellEditable(int row, int col) {
	           return false;
	      }
}
