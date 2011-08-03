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
package com.googlecode.eclipseutils.mtf.ui.generation.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.googlecode.eclipseutils.mtf.generation.GenerationUnit;
import com.googlecode.eclipseutils.mtf.ui.action.AbstractActionDelegate;
import com.googlecode.eclipseutils.mtf.ui.generation.wizards.GenerationWizard;


public abstract class AbstractGenerationActionDelegate extends AbstractActionDelegate {

	private static final Log logger = LogFactory.getLog(AbstractGenerationActionDelegate.class);

	private GenerationUnit generationUnit = null;

	public AbstractGenerationActionDelegate(GenerationUnit generationUnit) {
		super();
		this.generationUnit = generationUnit;
	}

	public abstract String getDialogTitle();

	public abstract String getWizardPageName();

	public abstract String getWizardTitle();

	public void run(IAction action) {
		checkConfiguration();

		Shell shell = new Shell();
		GenerationWizard snw = createGenerationWizard(generationUnit, getWizardPageName(), getWizardTitle());
		WizardDialog dialog = new WizardDialog(shell, snw);
		dialog.setTitle(getDialogTitle());
		snw.init(null, selection);
		dialog.open();
	}

	protected void checkConfiguration() {
		if (generationUnit.getCheckComponent() == null || generationUnit.getGenerationComponent() == null) {
			logger.error("ERROR: An extension component should be specified! \n");
		}
	}

	protected GenerationWizard createGenerationWizard(GenerationUnit generationUnit, String wizardPageName, String wizardTitle) {
		return new GenerationWizard(generationUnit, wizardPageName, wizardTitle);
	}

}