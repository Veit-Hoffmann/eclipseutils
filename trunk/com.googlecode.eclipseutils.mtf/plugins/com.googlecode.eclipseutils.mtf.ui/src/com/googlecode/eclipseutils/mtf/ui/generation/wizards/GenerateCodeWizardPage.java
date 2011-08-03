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
package com.googlecode.eclipseutils.mtf.ui.generation.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.googlecode.eclipseutils.mtf.check.CheckComponent;
import com.googlecode.eclipseutils.mtf.util.CustomWorkflowRunner;


// import sc.viper.uml2.codegen.uml22ansic.generation.vcf.VCFGenerationUnit;
public class GenerateCodeWizardPage extends WizardPage {

	private Button checkButton;
	private CheckComponent checkComponent;
	private Text checksSummaryText;

	private Text generationContainerText;

	private EObject model;
	private IStructuredSelection selection;

	/**
	 * Constructor for GenerateCodeFromUMLWizardPage.
	 * 
	 * @param pageName
	 */
	public GenerateCodeWizardPage(String pageName,
			IStructuredSelection selection, EObject model,
			CheckComponent checkComponent) {
		super(pageName);
		setPageComplete(false);

		this.selection = selection;
		this.model = model;
		this.checkComponent = checkComponent;
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;

		// checks summary
		Label label = new Label(container, SWT.NULL);
		label.setText("&Model checking:");

		checksSummaryText = new Text(container, SWT.BORDER | SWT.MULTI
				| SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		checksSummaryText.setLayoutData(gd);

		// src-gen container
		label = new Label(container, SWT.NULL);
		label.setText("&Generation folder:");

		Composite subcontainer = new Composite(container, SWT.NULL);
		layout = new GridLayout();
		subcontainer.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		gd = new GridData(GridData.FILL_HORIZONTAL);
		subcontainer.setLayoutData(gd);

		generationContainerText = new Text(subcontainer, SWT.BORDER
				| SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		generationContainerText.setLayoutData(gd);
		generationContainerText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(subcontainer, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});

		checkButton = new Button(subcontainer, SWT.CHECK);
		checkButton.setText("Clear directory first");

		initialize();
		dialogChanged();
		setControl(container);
	}

	public String getGenerationContainerName() {
		return generationContainerText.getText();
	}

	public EObject getModel() {
		return model;
	}

	public boolean isClearDirectory() {
		return checkButton.getSelection();
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getGenerationContainerName()));

		if (getGenerationContainerName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container != null && container.exists()
				&& container.getResourceAttributes().isReadOnly()) {
			updateStatus("Project must be writable");
			return;
		}
		updateStatus(null);
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				generationContainerText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {

		// get generation path
		if (selection != null && selection.isEmpty() == false) {
			// only one selected element
			if (selection.size() > 1) {
				return;
			}

			// get paths
			Object obj = selection.getFirstElement();
			if (obj instanceof IFile) {
				IFile file = (IFile) obj;
				IContainer container = file.getParent();
				generationContainerText.setText(container.getFullPath()
						.toString() + "/" + "src-gen");
			}
		}

		// check model, if supplied directly
		if (model != null) {
			// checking ansic model
			checksSummaryText.append("Starting model checks...");

			if (checkComponent == null) {
				// TODO Add real exception here or handle no check...
				checkComponent.getId(); // You need to configure a
										// checkComponent!
			}

			Issues issues = new IssuesImpl();
			CustomWorkflowRunner.run(checkComponent, issues, "model", model);

			// print out results
			checksSummaryText
					.append("\nChecks finished with "
							+ issues.getErrors().length
							+ " errors, "
							+ issues.getWarnings().length
							+ " warnings"
							+ (issues.getErrors().length
									+ issues.getWarnings().length > 0 ? ":"
									: "."));
			checksSummaryText.append("\n" + issues.toString());
			checksSummaryText.append("Finished...");
		}
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
}