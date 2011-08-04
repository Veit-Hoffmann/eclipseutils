/**
 * <copyright>
 *
 * Copyright (C) 2005, 2008 Research Group Software Construction,
 *                          RWTH Aachen University, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License
 * version 1.0, which accompanies this distribution, and is available
 * at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *   Research Group Software Construction - Initial API and implementation
 *
 * </copyright>
 */
package com.googlecode.eclipseutils.mtf.generation;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.xpand2.output.PostProcessor;
import org.eclipse.xtend.expression.AbstractExpressionsUsingWorkflowComponent.GlobalVarDef;

import com.googlecode.eclipseutils.mtf.check.CheckComponent;
import com.googlecode.eclipseutils.mtf.util.CustomWorkflowRunner;


public class GenerationUnit {

	private CheckComponent checkComponent;
	private GenerationComponent generationComponent;
	private List<PostProcessor> postProcessors;

	protected GenerationUnit() {
	}

	public void generate(EObject model, String folder) {
		GenerationComponent component = getGenerationComponent();
		component.initialize(folder, getPostProcessors());

		CustomWorkflowRunner
				.run(component, new IssuesImpl(), "newModel", model);
	}

	public CheckComponent getCheckComponent() {
		return checkComponent;
	}

	public GenerationComponent getGenerationComponent() {
		return generationComponent;
	}

	public List<PostProcessor> getPostProcessors() {
		return postProcessors;
	}

	public void setCheckComponent(CheckComponent checkComponent) {
		this.checkComponent = checkComponent;
	}

	public void setGenerationComponent(GenerationComponent generationComponent) {
		this.generationComponent = generationComponent;
	}

	public void setPostProcessors(List<PostProcessor> postProcessors) {
		this.postProcessors = postProcessors;
	}
	
	protected void addParameter(String name, String value) {

		// breaks through encapsulation - is this really the correct way to
		// create a globalVarDef?
		GlobalVarDef param = new GlobalVarDef();
		param.setName(name);
		param.setValue("'" + value + "'");

		getGenerationComponent().addGlobalVarDef(param);
	}
}
