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

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.emf.workspace.WorkspaceEditingDomainFactory;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.googlecode.eclipseutils.mtf.check.CheckComponent;
import com.googlecode.eclipseutils.mtf.generation.GenerationComponent;
import com.googlecode.eclipseutils.mtf.generation.GenerationUnit;
import com.googlecode.eclipseutils.mtf.ui.Activator;
import com.googlecode.eclipseutils.mtf.util.CustomWorkflowRunner;


public class GenerationWizard extends Wizard implements INewWizard {

	protected static final Log logger = LogFactory
			.getLog(GenerationWizard.class);

	// wizard page
	protected GenerateCodeWizardPage page1;

	// extension
	private GenerationUnit generationUnit;
	// model
	private EObject model;

	// wizard page attributes
	private String pageName;

	// selection
	private IStructuredSelection selection;

	private String title;

	/**
	 * Constructor for GenerateCodeFromAnsiCWizard.
	 */
	public GenerationWizard(GenerationUnit generationUnit, String pageName,
			String title) {
		super();
		setWindowTitle("Generation...");
		setNeedsProgressMonitor(true);
		this.generationUnit = generationUnit;
		this.pageName = pageName;
		this.title = title;

		setupDialogSettings();
	}

	/**
	 * Adding the page to the wizard.
	 */

	@Override
	public void addPages() {
		page1 = createGenerateCodeWizardPage(pageName, selection, model,
				generationUnit.getCheckComponent());
		page1.setTitle(title);
		addPage(page1);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;

		if (selection != null && selection.isEmpty() == false) {
			// only single selection
			if (selection.size() > 1) {
				return;
			}

			// open progress monitor
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(
					this.getShell());
			IProgressMonitor monitor = pmd.getProgressMonitor();
			pmd.open();
			monitor.beginTask("Load model...", 3);

			// model file path and read in model
			try {
				IResource resource = (IResource) ((IStructuredSelection) selection)
						.getFirstElement();
				EditingDomain domain = WorkspaceEditingDomainFactory.INSTANCE
						.createEditingDomain();
				Resource emfresource = domain.loadResource(resource
						.getLocationURI().toString());
				Object m = emfresource.getContents().get(0);
				model = (EObject) m;
			} catch (Exception e) {
				logger.error(e);
				MessageDialog.openError(this.getShell(), "Error",
						"Error while loading model.: " + e.getMessage());
				throw new IllegalStateException(e);
			} finally {
				monitor.worked(1);
				pmd.close();
			}
		}
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final String containerName = page1.getGenerationContainerName();
		final EObject model = page1.getModel();
		final boolean clearDir = page1.isClearDirectory();
		IRunnableWithProgress op = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					doFinish(model, containerName, clearDir, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error",
					realException.getMessage());
			logger.error(e);
			return false;
		}
		return true;
	}

	protected GenerateCodeWizardPage createGenerateCodeWizardPage(
			String pageName, IStructuredSelection selection, EObject model,
			CheckComponent checkComponent) {
		return new GenerateCodeWizardPage(pageName, selection, model,
				checkComponent);
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */

	protected void doFinish(EObject model, String containerName,
			boolean isClearDirectory, IProgressMonitor monitor)
			throws CoreException {
		monitor.beginTask("Generate Code...", 7);
		monitor.worked(1);

		// set generation directory from selection (might be created on-the-fly)
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IResource resource = root.getFolder(new Path(containerName));

		if (isClearDirectory) {
			monitor.setTaskName("Deleting target directory...");
			resource.delete(true, monitor);
			monitor.worked(1);
		}

		if (!resource.exists() || !(resource instanceof IContainer)) {
			try {
				monitor.setTaskName("Creating target directory...");
				((IFolder) resource).create(true, true, monitor);
				monitor.worked(1);
			} catch (CoreException e) {
				logger.error(e);
			}
		}

		monitor.setTaskName("Generate Code...");
		GenerationComponent component = generationUnit.getGenerationComponent();
		component.initialize(resource.getRawLocation().toString(),
				generationUnit.getPostProcessors());
		CustomWorkflowRunner.run(component, new IssuesImpl(), "model", model);
		monitor.worked(1);

		// sync resources
		try {
			resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (CoreException e) {
			logger.error("problem with sync: ", e);
		}
		monitor.worked(1);

	}

	protected GenerationUnit getGenerationUnit() {
		return generationUnit;
	}

	protected EObject getModel() {
		return model;
	}

	/**
	 * Registers a section for saving settings with the plugin activator, thus
	 * enabling pages to persist dialog settings between wizard calls.
	 */
	protected void setupDialogSettings() {
		final String id = getClass().getName();

		IDialogSettings pluginSettings = Activator.getDefault()
				.getDialogSettings();
		IDialogSettings wizardSettings = pluginSettings.getSection(id);
		if (wizardSettings == null) {
			wizardSettings = pluginSettings.addNewSection(id);
		}

		setDialogSettings(wizardSettings);
	}
}