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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.googlecode.eclipseutils.mtf.transformation.TransformationComponent;
import com.googlecode.eclipseutils.mtf.util.CustomWorkflowRunner;


public abstract class TransformAndSaveResultingModelWizardPage extends
		WizardPage {

	private static final Log logger = LogFactory
			.getLog(TransformAndSaveResultingModelWizardPage.class);

	private EObject resultingModel;

	private Text resultingModelFileText;

	private IStructuredSelection selection;

	private EObject sourceModel;

	private TransformationComponent transformationComponent;

	private Text transformationSummaryText;

	@Override
	public boolean isPageComplete() {
		return resultingModel != null;
	}
	
	/**
	 * Constructor for GenerateCodeFromUMLWizardPage.
	 * 
	 * @param pageName
	 */
	public TransformAndSaveResultingModelWizardPage(
			IStructuredSelection selection,
			TransformationComponent transformationComponent) {
		super("TransformAndSavePage");
		setTitle("Save created model");
		setDescription("");
		setPageComplete(false);

		this.selection = selection;
		this.transformationComponent = transformationComponent;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;

		// checks summary
		Label label = new Label(container, SWT.NULL);
		label.setText("&Transformation:");

		transformationSummaryText = new Text(container, SWT.BORDER | SWT.MULTI
				| SWT.WRAP | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		transformationSummaryText.setLayoutData(gd);

		// ansic model location
		label = new Label(container, SWT.NULL);
		label.setText("&Resulting model file name:");

		resultingModelFileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		resultingModelFileText.setLayoutData(gd);
		resultingModelFileText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);
	}

	public EObject getResultingModel() {
		return resultingModel;
	}

	protected CheckSourceModelWizardPage getCheckSourceModelWizardPage() {
		return ((CheckSourceModelWizardPage) getPreviousPage());
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			// set source model
			sourceModel = getCheckSourceModelWizardPage().getSourceModel();

			// get toplevel componente
			EObject topLevelComponent = getCheckSourceModelWizardPage()
					.getToplevelComponent();

			// transform
			transformationSummaryText.setText("Starting transformation...");
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(this
					.getShell());
			IProgressMonitor monitor = pmd.getProgressMonitor();
			pmd.open();
			monitor.beginTask("Starting transformation...\n", 30);

			Issues issues = new IssuesImpl();
			// set source model
			try {
				transformationComponent.setOutputSlot("newModel");

				addInformationToTransformationComponent(transformationComponent);

				WorkflowContext ctx = CustomWorkflowRunner.createContext();
				ctx.set("model", sourceModel);
				ctx.set("topLevelComponent", topLevelComponent);
				ctx = CustomWorkflowRunner.run(transformationComponent, issues,
						ctx);
				resultingModel = (EObject) ctx.get("newModel");
				// show the error messages!
			} catch (Exception e) {
				logger.error(
						"Exception in running transformation component...", e);
				MessageDialog.openError(new Shell(), "Error in transformation",
						"Transformation component unhappy because of "
								+ e.getMessage());
			} finally {
				// pmd.close();
			}

			if (issues.getErrors().length > 0 || resultingModel == null) {
				StringBuffer errorMessages = new StringBuffer();
				errorMessages
						.append("Errors occured during the transformation: ");
				for (int i = 0; i < issues.getErrors().length; i++) {
					errorMessages.append(issues.getErrors()[i].toString()
							+ "\n");
				}
				MessageDialog.openError(new Shell(), "Transformation error",
						errorMessages.toString());
				transformationSummaryText
						.append("\nTransformation not finished.");
				updateStatus(errorMessages.toString());
			} else {
				transformationSummaryText.append("\nTransformation done.");
				updateStatus(null);
			}
			monitor.worked(1);
			pmd.close();
		}
	}

	protected void addInformationToTransformationComponent(
			TransformationComponent transformationComponent) {
	}

	protected abstract String getResultingModelFileExtension();

	protected String getResultingModelFileName() {
		return resultingModelFileText.getText();
	}

	/**
	 * Ensures that text field is set.
	 */

	private void dialogChanged() {
		String fileName = getResultingModelFileName();

		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		updateStatus(null);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {

		if (selection != null && selection.isEmpty() == false) {
			// only one selected element
			if (selection.size() > 1) {
				return;
			}

			// get filename & path of ansic-model
			Object obj = selection.getFirstElement();
			if (obj instanceof IFile) {
				IFile file = (IFile) obj;
				IContainer container = file.getParent();
				String fileName = file.getName();
				resultingModelFileText.setText(container.getFullPath()
						.toString()
						+ "/"
						+ fileName.substring(0, fileName.lastIndexOf("."))
						+ "." + getResultingModelFileExtension());
			}
		}
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

}