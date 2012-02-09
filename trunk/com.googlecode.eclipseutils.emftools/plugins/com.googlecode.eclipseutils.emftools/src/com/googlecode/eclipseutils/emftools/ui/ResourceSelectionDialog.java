package com.googlecode.eclipseutils.emftools.ui;
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


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract Dialog for selecting a single Resource from the workspace.
 * 
 * @author Andreas Walter
 * @version $Revision: 1.8 $
 * 
 */
public class ResourceSelectionDialog extends WorkspaceResourceDialog {

	private String[] validFileExtensions;

	public ResourceSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider, String[] validFileExtensions) {
		super(parent,
			labelProvider,
			contentProvider);
		this.validFileExtensions = validFileExtensions;
		setTitle(getTitleText());
		setMessage(getMesssageText());
		addFilter(createDefaultViewerFilter(true));
		addFilter(createFileExtensionFilter());
		setAllowMultiple(false);
		loadContents();
	}

	protected String getTitleText() {
		return "Select a " + getValidFileExtensionsDisplayString() + " file";
	}

	@Override
	public IStatus validate(Object[] selectedElements) {

		if (selectedElements.length == 0 || selectedElements.length >= 2) {
			return super.validate(selectedElements);
		}
		else {
			Object selectedElement = selectedElements[0];
			if (selectedElement instanceof IFile) {
				IFile file = (IFile) selectedElement;
				if (isAccepted(file.getName())) {

					return new Status(IStatus.OK, Activator.PLUGIN_ID, "");
				}
				else {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "You have to select a " + getValidFileExtensionsDisplayString() + " file");
				}
			}
			else {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "You have to select a " + getValidFileExtensionsDisplayString() + " file");
			}
		}
	}

	protected ViewerFilter createFileExtensionFilter() {
		ViewerFilter fileExtensionFilter = new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IFile) {
					IFile file = (IFile) element;
					return isAccepted(file.getName());
				}
				else if(element instanceof IFolder){
					//if folder does not contains subfolders and does not contain valid
					// files, it is also not visible
					IFolder folder = (IFolder)element;
					try {
						boolean visible = false;
						IResource[] folderContents = folder.members();
						for(int i=0; i<folderContents.length && !visible; i++){
							if(isAccepted(folderContents[i].getName())){
								visible = true;
							}
							else{
								if(folderContents[i] instanceof IFolder){
									visible = true;
								}
							}
						}
						return visible;
					}
					catch (CoreException e) {
						return true;
					}
				}
				return true;
			}
		};
		return fileExtensionFilter;
	}

	private String getValidFileExtensionsDisplayString() {
		String suffixList = "";
		for (int i = 0; i < validFileExtensions.length; i++) {
			suffixList += "'";
			suffixList += ".";
			suffixList += validFileExtensions[i];
			suffixList += "'";
			if (i != validFileExtensions.length - 1) {
				suffixList += " or ";
			}
		}
		return suffixList;
	}

	private boolean isAccepted(String fileName) {
		boolean accepted = false;
		for (int i = 0; i < validFileExtensions.length && !accepted; i++) {
			if (fileName.endsWith("." + validFileExtensions[i])) {
				accepted = true;
			}
		}
		return accepted;
	}

	protected String getMesssageText() {
		return "Please select a " + getValidFileExtensionsDisplayString() + " resource:";
	}

}
