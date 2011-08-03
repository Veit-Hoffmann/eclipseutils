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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.resource.impl.URIConverterImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.mwe.core.ConfigurationException;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.emf.workspace.WorkspaceEditingDomainFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.xtend.typesystem.emf.XmiWriter;

import com.googlecode.eclipseutils.mtf.check.CheckComponent;
import com.googlecode.eclipseutils.mtf.transformation.TransformationComponent;
import com.googlecode.eclipseutils.mtf.transformation.TransformationUnit;
import com.googlecode.eclipseutils.mtf.util.CustomWorkflowRunner;


public abstract class TransformationWizard extends Wizard implements INewWizard {

	private static final Log logger = LogFactory
			.getLog(TransformationWizard.class);

	private String firstPageTitle;
	// wizard page
	private CheckSourceModelWizardPage page1;
	private TransformAndSaveResultingModelWizardPage page2;

	// selection
	private IStructuredSelection selection;

	// model
	private EObject sourceModel;

	// extension
	private TransformationUnit unit;

	protected WizardPage getCheckSourceModelWizarPage() {
		return page1;
	}
	
	protected WizardPage getTransformAndSaveResultingModelWizardPage() {
		return page2;
	}

	/**
	 * Constructor for GenerateAnsiCModelWizard.
	 */
	public TransformationWizard(String firstPageTitle, TransformationUnit unit) {
		super();
		setWindowTitle("Transformation...");
		setNeedsProgressMonitor(true);

		this.firstPageTitle = firstPageTitle;
		this.unit = unit;
	}

	protected void prepagePages() {
		page1 = getCheckSourceModelWizardPage(selection, sourceModel, unit
				.getCheckComponent());
		page1.setTitle(firstPageTitle);
		page2 = getTransformAndSaveResultingModelWizardPage(selection, unit
				.getTransformationComponent());
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		prepagePages();
		addPage(page1);
		addPage(page2);		
	}

	public abstract CheckSourceModelWizardPage getCheckSourceModelWizardPage(
			IStructuredSelection selection, EObject sourceModel,
			CheckComponent checkComponent);

	public abstract TransformAndSaveResultingModelWizardPage getTransformAndSaveResultingModelWizardPage(
			IStructuredSelection selection,
			TransformationComponent transformationComponent);

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;

		// load model
		if (selection != null && selection.isEmpty() == false) {
			// only one selected element
			if (selection.size() > 1) {
				return;
			}

			ProgressMonitorDialog pmd = new ProgressMonitorDialog(this
					.getShell());
			IProgressMonitor monitor = pmd.getProgressMonitor();
			pmd.open();
			monitor.beginTask("Load source model...", 3);

			// model file path and read in model
			try {
				IResource resource = (IResource) ((IStructuredSelection) selection).getFirstElement();
				monitor.worked(1);
				EditingDomain domain = WorkspaceEditingDomainFactory.INSTANCE.createEditingDomain();
				Resource emfresource = domain.loadResource(resource.getLocationURI().toString());
				monitor.worked(1);
				Object m = emfresource.getContents().get(0);
				sourceModel = (EObject) m;
				monitor.worked(1);
			} catch (Exception e) {
				logger.error(e);
				MessageDialog.openError(this.getShell(), "Error",
						"Error while loading model.: " + e.getMessage());
				throw new IllegalStateException(e);
			} finally {
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
		final String modelFileName = page2.getResultingModelFileName();
		final EObject resultingModel = page2.getResultingModel();
		IRunnableWithProgress op = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					doFinish(resultingModel, modelFileName, monitor);
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
			MessageDialog.openError(getShell(), "Error", realException
					.getMessage());
			return false;
		}
		return true;
	}

	protected TransformationUnit getTransformationUnit() {
		return unit;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */

	private void doFinish(EObject resultingModel, final String modelFileName,
			IProgressMonitor monitor) throws CoreException {

		// set generation directory from selection (might be created on-the-fly)
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.getFolder(new Path(modelFileName));

		monitor.worked(1);
		if (resultingModel == null) {
			// TODO this here is duplicated code, as the Transform... page already does the transformation...
			
			// set uml model
			EObject sourceModel = page1.getSourceModel();

			// set uml model
			try {
				TransformationComponent transformationComponent = unit
						.getTransformationComponent();
				transformationComponent.setOutputSlot("newModel");
				WorkflowContext ctx = CustomWorkflowRunner.run(
						transformationComponent, new IssuesImpl(), "model",
						sourceModel);
				resultingModel = (EObject) ctx.get("newModel");
				monitor.worked(1);
			} catch (Exception e) {
				logger.error("Exception in running transformation workflow...",
						e);
				MessageDialog.openError(new Shell(), "Error in transformation",
						"Transformation component unhappy because of "
								+ e.getMessage());
			}
		}

		// write model to disk //TODO copied code can be removed when change
		// request is implemented by oaw
		XmiWriter writer = new XmiWriter() {

			// COPIED FROM XmiWriter as attributes are not accessible in
			// subclasses
			private String pathPrefix = null;

			@Override
			public void checkConfiguration(final Issues issues) {
				setModelFile(getModelFile().replace('\\', '/'));
				int p = getModelFile().indexOf("/");
				if (p >= 0) {
					pathPrefix = getModelFile().substring(0, p + 1);
					setModelFile(getModelFile().substring(p + 1));
				}
			}

			// END COPY

			@Override
			@SuppressWarnings("unchecked")
			public void invokeInternal(final WorkflowContext ctx, final ProgressMonitor monitor, final Issues issues) {
				EObject model = null;
				final Object slotContent = ctx.get(getInputSlot());
				if (slotContent == null) {
					issues.addError(this, "slot '" + getInputSlot()
							+ "' is empty.");
					return;
				}
				if (slotContent instanceof List) {
					model = (EObject) ((List) slotContent).get(0);
				} else if (slotContent instanceof EObject) {
					model = (EObject) slotContent;
				} else {
					issues.addError(this, "slot '" + getInputSlot()
							+ "' neither contains an EList nor an EObject",
							slotContent);
					return;
				}

				Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
						.put("*", new XMIResourceFactoryImpl());

				URI fileURI = URI.createPlatformResourceURI(pathPrefix
						+ getModelFile(), true);
				URIConverter converter = new URIConverterImpl();
				fileURI = converter.normalize(fileURI);
				ResourceSetImpl rsImpl = new ResourceSetImpl();
				Resource r = rsImpl.createResource(fileURI);

				r.getContents().add(model);
				try {
					r.save(null);
				} catch (final IOException e) {
					throw new ConfigurationException(e);
				}

			}
		};
		writer.setInputSlot("model");
		writer.setModelFile(resource.getFullPath().toString());

		Issues issues = new IssuesImpl();
		writer.checkConfiguration(issues);

		CustomWorkflowRunner.run(writer, issues, "model", resultingModel);
		monitor.worked(1);

		// sync resources
		try {
			monitor.setTaskName("Syncing directories...");
			resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (CoreException e) {
			logger.error("problem with sync: ", e);
		}
		monitor.worked(1);

	}

}