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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public abstract class AbstractActionDelegate implements IActionDelegate {

	private static final Log logger = LogFactory.getLog(AbstractActionDelegate.class);

	// // store model reader
	// protected IModelReader modelReader;

	// store the current selection
	protected IStructuredSelection selection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		}
		else {
			this.selection = null;
		}
	}

	// /**
	// * set the model reader
	// *
	// * @param modelReader
	// */
	// public void setModelReader(IModelReader modelReader) {
	// this.modelReader = modelReader;
	// }

	/**
	 * @return selected file
	 */
	protected IFile getSelectionFile() {
		Object obj = selection.getFirstElement();
		if (obj instanceof IFile) {
			return (IFile) obj;
		}
		return null;
	}

	/**
	 * log issues to console
	 * 
	 * @param file
	 * @param issues
	 */
	protected void logIssues(IFile file, Issues issues) {
		// analyze results (on console)
		MessageConsole console = new MessageConsole("", null);
		MessageConsoleStream output = new MessageConsoleStream(console);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
		try {
			output.write(issues.toString());
			output.write("\n\n");
			output.write("Results of checking model \""
				+ file.getName()
				+ "\": "
				+ issues.getWarnings().length
				+ " warnings, "
				+ issues.getErrors().length
				+ " errors. \n");
		}
		catch (IOException e) {
			logger.error(e);
		}
	}

}
