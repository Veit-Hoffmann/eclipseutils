package com.googlecode.eclipseutils.emftools.editTools;

import org.eclipse.emf.ecore.EStructuralFeature;

public class ChildDescriptor {
	private Object eInstance;
	private EStructuralFeature literal;

	public ChildDescriptor(Object eInstance, EStructuralFeature literal) {
		super();
		
		this.setEInstance(eInstance);
		this.setLiteral(literal);
	}

	public Object getEInstance() {
		return eInstance;
	}

	protected void setEInstance(Object eInstance) {
		this.eInstance = eInstance;
	}

	public EStructuralFeature getLiteral() {
		return literal;
	}

	protected void setLiteral(EStructuralFeature literal) {
		this.literal = literal;
	}
}
