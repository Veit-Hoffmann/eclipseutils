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
package com.googlecode.eclipseutils.mtf.ui.action;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.googlecode.eclipseutils.mtf.check.CheckComponent;
import com.googlecode.eclipseutils.mtf.util.CustomWorkflowRunner;


public abstract class ModelValidationActionDelegate extends
		AbstractActionDelegate {

	private static final Log logger = LogFactory
			.getLog(ModelValidationActionDelegate.class);

	// store model validation component
	// TODO: work with generationUnit/transformationUnit
	private CheckComponent modelValidationComponent;

	/**
	 * Constructor for Action1.
	 */
	public ModelValidationActionDelegate() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		checkConfiguration();

		Object obj = selection.getFirstElement();
		if (obj instanceof IFile) {
			IFile file = (IFile) obj;
			String modelFileName = file.getFullPath().toString();

			// call check component
			Issues issues = new IssuesImpl();
			CustomWorkflowRunner.run(modelValidationComponent, issues, "model", null); // TODO This may produce errors and needs to be reworked...

			// analyze results (on console)
			MessageConsole console = new MessageConsole("", null);
			MessageConsoleStream output = new MessageConsoleStream(console);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(
					new IConsole[] { console });
			ConsolePlugin.getDefault().getConsoleManager().showConsoleView(
					console);
			try {
				output.write(issues.toString());
				output.write("\n\n");
				output.write("Results of checking model \"" + file.getName()
						+ "\": " + issues.getWarnings().length + " warnings, "
						+ issues.getErrors().length + " errors. \n");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	/**
	 * set model validation component
	 * 
	 * @param modelValidationComponent
	 */
	public void setCheckComponent(CheckComponent modelValidationComponent) {
		this.modelValidationComponent = modelValidationComponent;
	}

	// @Override
	protected void checkConfiguration() {
		// super.checkConfiguration();

		if (modelValidationComponent == null) {
			logger
					.error("ERROR: A model validation component should be specified! \n");
		}
	}
}
