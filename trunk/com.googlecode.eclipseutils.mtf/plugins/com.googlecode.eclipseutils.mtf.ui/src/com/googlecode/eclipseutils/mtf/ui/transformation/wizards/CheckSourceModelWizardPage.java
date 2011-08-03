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
package com.googlecode.eclipseutils.mtf.ui.transformation.wizards;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.googlecode.eclipseutils.mtf.check.CheckComponent;
import com.googlecode.eclipseutils.mtf.util.CustomWorkflowRunner;


public abstract class CheckSourceModelWizardPage extends WizardPage {

	private static final Log logger = LogFactory
			.getLog(CheckSourceModelWizardPage.class);

	// the standard check to perform
	private CheckComponent checkComponent;
	// ui components
	private Text checksSummaryText;

	private Issues issues;

	// store the model
	private EObject sourceModel;

	private Combo topLevelComponentView;

	// List for model components
	private List<EObject> topLevelElements;

	/**
	 * Constructor for GenerateCodeFromUMLWizardPage.
	 * 
	 * @param pageName
	 */
	public CheckSourceModelWizardPage(IStructuredSelection selection,
			EObject model, CheckComponent checkComponent) {

		// init ui
		super("wizardPage");
		setDescription("");
		setPageComplete(false);

		// store model of Type MethodLibrary
		this.sourceModel = model;
		topLevelElements = getTopLevelElements(model);

		// init checks
		this.checkComponent = checkComponent;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		// checks
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Checks:");

		checksSummaryText = new Text(container, SWT.BORDER | SWT.MULTI
				| SWT.WRAP | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		checksSummaryText.setLayoutData(gd);

		// TODO: this isn't pretty: UML2AnsiC needs Optimization-Button
		// UMA2CS doesn't
		addSpecialControls(container);

		// top level component
		Composite twoColumnsComposite = new Composite(container, SWT.NULL);
		layout = new GridLayout();
		twoColumnsComposite.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;

		label = new Label(twoColumnsComposite, SWT.NULL);
		label.setText("&Realize as Toplevel Component:");

		topLevelComponentView = new Combo(twoColumnsComposite, SWT.SIMPLE
				| SWT.DROP_DOWN | SWT.READ_ONLY);

		for (Iterator iter = topLevelElements.iterator(); iter.hasNext();) {
			topLevelComponentView.add(getTopLevelElementName(iter.next()));
		}

		if (topLevelComponentView.getItemCount() > 0) {
			topLevelComponentView.select(0);
		}

		initialize();
		updateStatus();
		setControl(container);
	}

	public EObject getSourceModel() {
		return sourceModel;
	}

	public EObject getToplevelComponent() {
		return (EObject) topLevelElements.get(topLevelComponentView
				.getSelectionIndex());
	}

	protected abstract void addSpecialControls(Composite container);

	protected abstract String getTopLevelElementName(Object object);

	protected abstract List getTopLevelElements(EObject model);

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {

		// checking uml2 model
		checksSummaryText.append("Starting model checks...");

		issues = new IssuesImpl();
		try {
			CustomWorkflowRunner.run(checkComponent, issues, "model",
					sourceModel);
		} catch (Exception e) {
			issues.addError(e.getMessage());
			logger.error(e);
		}
		// print out results
		checksSummaryText
				.append("\nChecks finished with "
						+ issues.getErrors().length
						+ " errors, "
						+ issues.getWarnings().length
						+ " warnings"
						+ (issues.getErrors().length
								+ issues.getWarnings().length > 0 ? ":" : "."));
		checksSummaryText.append("\n" + issues.toString());
	}

	private void updateStatus() {
		String message = "Checks finished with " + issues.getErrors().length
				+ " errors, " + issues.getWarnings().length + " warnings.";
		if (issues.getErrors().length > 0) {
			setErrorMessage(message);
			setPageComplete(false);
		} else if (topLevelElements==null || topLevelElements.isEmpty()) {
			setErrorMessage("The methode doesn't contain any top level elements thus generation won't work");
			setPageComplete(false);
		}else {
			if (issues.getWarnings().length > 0) {
				setMessage(message, IMessageProvider.WARNING);
			} else {
				setMessage(message);
			}
			setPageComplete(true);
		}
	}

}