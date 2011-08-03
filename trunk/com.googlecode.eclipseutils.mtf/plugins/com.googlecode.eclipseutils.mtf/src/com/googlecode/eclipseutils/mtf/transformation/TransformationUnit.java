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
package com.googlecode.eclipseutils.mtf.transformation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.xpand2.output.PostProcessor;

import com.googlecode.eclipseutils.mtf.check.CheckComponent;
import com.googlecode.eclipseutils.mtf.util.CustomWorkflowRunner;


public class TransformationUnit {

	private CheckComponent checkComponent;
	private PostProcessor postProcessor;
	private TransformationComponent transformationComponent;

	protected TransformationUnit() {
	}

	public CheckComponent getCheckComponent() {
		return checkComponent;
	}

	public PostProcessor getPostProcessor() {
		return postProcessor;
	}

	public TransformationComponent getTransformationComponent() {
		return transformationComponent;
	}

	public void setCheckComponent(CheckComponent modelCheckComponent) {
		checkComponent = modelCheckComponent;
	}

	public void setPostProcessor(PostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	public void setTransformationComponent(
			TransformationComponent modelTransformationComponent) {
		transformationComponent = modelTransformationComponent;
	}

	public EObject transform(EObject model) {
		TransformationUnit transformationUnit = this;
		TransformationComponent transformationComponent = transformationUnit
				.getTransformationComponent();
		transformationComponent.setOutputSlot("targetModel");

		Issues issues = new IssuesImpl();
		WorkflowContext ctx = CustomWorkflowRunner.createContext();
		ctx.set("model", null);
		ctx.set("topLevelComponent", model);
		ctx = CustomWorkflowRunner.run(transformationComponent, issues, ctx);
		return (EObject) ctx.get("targetModel");
	}

}
