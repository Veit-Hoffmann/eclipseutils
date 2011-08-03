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
package com.googlecode.eclipseutils.mtf.ui.transformation.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.googlecode.eclipseutils.mtf.transformation.TransformationUnit;
import com.googlecode.eclipseutils.mtf.ui.action.AbstractActionDelegate;
import com.googlecode.eclipseutils.mtf.ui.transformation.wizards.TransformationWizard;


public abstract class AbstractTransformationActionDelegate extends AbstractActionDelegate {

	private static final Log logger = LogFactory.getLog(AbstractTransformationActionDelegate.class);

	private TransformationUnit unit = null;

	public AbstractTransformationActionDelegate(TransformationUnit unit) {
		super();
		this.unit = unit;
	}

	public abstract String getDialogTitle();

	public abstract String getFirstPageTitle();

	public abstract TransformationWizard getTransformationWizard(String firstPageTitle, TransformationUnit unit);

	public void run(IAction action) {
		checkConfiguration();

		Shell shell = new Shell();
		TransformationWizard snw = getTransformationWizard(getFirstPageTitle(), unit);
		WizardDialog dialog = new WizardDialog(shell, snw);
		dialog.setTitle(getDialogTitle());

		snw.init(null, selection);
		dialog.open();
	}

	// @Override
	protected void checkConfiguration() {
		if (unit.getCheckComponent() == null || unit.getTransformationComponent() == null) {
			logger.error("ERROR: An extension component should be specified! \n");
		}
	}
}