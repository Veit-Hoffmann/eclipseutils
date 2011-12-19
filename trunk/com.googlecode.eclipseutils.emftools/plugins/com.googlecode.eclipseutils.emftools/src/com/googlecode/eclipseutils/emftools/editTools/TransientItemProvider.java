/**
 * <copyright>
 *
 * Copyright (C) 2011 Research Group Software Construction,
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
package com.googlecode.eclipseutils.emftools.editTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.UnexecutableCommand;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;

public abstract class TransientItemProvider extends ItemProviderAdapter implements
		IEditingDomainItemProvider, IStructuredItemContentProvider,
		ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {

	private EObject residentObject;
	
	public TransientItemProvider(AdapterFactory adapterFactory, EObject residentObject) {
		super(adapterFactory);
		this.residentObject = residentObject;
		residentObject.eAdapters().add(this);
		Assert.isTrue(target != null);
	}

	public Object getParent(Object object) {
		Assert.isTrue(target != null);
		return target;
	}
	
	public EObject getResidentObject() {
		return residentObject;
	}

	@Override
	public Collection<?> getNewChildDescriptors(Object object,
			EditingDomain editingDomain, Object sibling) {
	    
		Collection<Object> newChildDescriptors = new ArrayList<Object>();
	    // collectNewChildDescriptors(newChildDescriptors, object);
		return newChildDescriptors;
	}

	/* */
	
	public boolean hasChildren(Object object) {
		Assert.isTrue(target != null);
		return super.hasChildren(target);
	}

	public Collection<?> getChildren(Object object) {
		if (target != null) {
			return super.getChildren(target);
		}
		return null;
	}

	/**
	 * Return the resource locator for this item provider's resources. 
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		Assert.isTrue(target != null);
		return this.getSpecificResourceLocator();
	}

	protected abstract ResourceLocator getSpecificResourceLocator();
	
	public Collection<?> getElements(Object object) {
		if (target != null) {
			return super.getElements(target);
		}
		return null;
	}

	public Object getEditableValue(Object object) {
		Assert.isTrue(target != null);
		return super.getEditableValue(target);
	}

	public IItemPropertyDescriptor getPropertyDescriptor(Object object,
			Object propertyID) {
		Assert.isTrue(target != null);
		return super.getPropertyDescriptor(target, propertyID);
	}

	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		Assert.isTrue(target != null);
		return super.getPropertyDescriptors(target);
	}

	@Override
	public Command createCommand(Object object, EditingDomain domain,
			Class<? extends Command> commandClass,
			CommandParameter commandParameter) {
		if (commandParameter.getCollection()!=null) {
			for (Object element : commandParameter.getCollection()) {
				if (!(element instanceof EObject)) {
					return UnexecutableCommand.INSTANCE;
				}
			}
		}
		return UnexecutableCommand.INSTANCE;
	}
	
	@Override
	protected Command createDragAndDropCommand(EditingDomain domain,
			java.lang.Object owner, float location, int operations,
			int operation, java.util.Collection<?> collection) {
		return UnexecutableCommand.INSTANCE;
	}

	protected abstract List<ChildDescriptor> getChildDescriptors(); 
	
	@Override
	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);

		for (ChildDescriptor descriptor : this.getChildDescriptors()) {
			newChildDescriptors.add
			(createChildParameter
				(descriptor.getLiteral(),
				 descriptor.getEInstance()
				)
			);
		}
	}	
	
	protected abstract EStructuralFeature getChildFeatureLiteral(); 

	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);
			childrenFeatures.add(getChildFeatureLiteral());
		}
		return childrenFeatures;
	}

	@Override
	protected EStructuralFeature getChildFeature(Object object, Object child) {
		// Check the type of the specified child object and return the proper feature to use for
		// adding (see {@link AddCommand}) it as a child.

		return super.getChildFeature(object, child);
	}

}
