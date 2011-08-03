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
package com.googlecode.eclipseutils.mtf.util;

import org.eclipse.emf.mwe.core.WorkflowComponent;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.WorkflowContextDefaultImpl;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.emf.mwe.core.monitor.NullProgressMonitor;
import org.eclipse.emf.mwe.core.resources.ResourceLoaderFactory;
import org.eclipse.emf.mwe.core.resources.ResourceLoaderImpl;



public abstract class CustomWorkflowRunner {

	// find dummy monitor
	static final NullProgressMonitor progressMonitor = new NullProgressMonitor();

	public static WorkflowContext createContext() {
		return new WorkflowContextDefaultImpl();
	}

	public static WorkflowContext run(WorkflowComponent component) {
		ResourceLoaderFactory.setCurrentThreadResourceLoader(new ResourceLoaderImpl(CustomWorkflowRunner.class.getClassLoader()));
		WorkflowContext context = CustomWorkflowRunner.createContext();
		component.invoke(context, progressMonitor, new IssuesImpl());
		return context;
	}

	public static WorkflowContext run(WorkflowComponent component, Issues issues, String key, Object value) {
		WorkflowContext context = CustomWorkflowRunner.createContext();
		ResourceLoaderFactory.setCurrentThreadResourceLoader(new ResourceLoaderImpl(CustomWorkflowRunner.class.getClassLoader()));
		context.set(key, value);
		component.checkConfiguration(issues);
		component.invoke(context, progressMonitor, issues);
		return context;
	}

	public static WorkflowContext run(WorkflowComponent component, Issues issues, WorkflowContext context) {
		ResourceLoaderFactory.setCurrentThreadResourceLoader(new ResourceLoaderImpl(CustomWorkflowRunner.class.getClassLoader()));
		component.checkConfiguration(issues);
		component.invoke(context, progressMonitor, issues);
		return context;
	}
}
