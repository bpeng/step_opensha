package org.opensha.commons.gridComputing.condor;

/**
 * This represents a single parent child relationship between two {@link SubmitScriptForDAG}'s
 * within a {@link DAG}.
 * 
 * @author kevin
 *
 */
public class ParentChildRelationship {
	
	private SubmitScriptForDAG parent;
	private SubmitScriptForDAG child;
	
	private String comment = null;
	
	public ParentChildRelationship(SubmitScriptForDAG parent, SubmitScriptForDAG child) {
		if (parent.getJobName().equals(child.getJobName()))
			throw new IllegalArgumentException("Parent and child cannot be the same!");
		this.parent = parent;
		this.child = child;
	}
	
	public SubmitScriptForDAG getParent() {
		return parent;
	}

	public void setParent(SubmitScriptForDAG parent) {
		this.parent = parent;
	}

	public SubmitScriptForDAG getChild() {
		return child;
	}

	public void setChild(SubmitScriptForDAG child) {
		this.child = child;
	}
	
	public boolean hasComment() {
		return comment != null && comment.length() > 0;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		if (comment != null && comment.length() > 0 && !comment.startsWith("#"))
			comment = "#" + comment;
		this.comment = comment;
	}

}
