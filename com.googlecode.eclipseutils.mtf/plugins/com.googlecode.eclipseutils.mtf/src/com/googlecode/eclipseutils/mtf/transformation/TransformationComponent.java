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
package com.googlecode.eclipseutils.mtf.transformation;

import org.eclipse.emf.mwe.core.resources.ResourceLoaderFactory;
import org.eclipse.emf.mwe.core.resources.ResourceLoaderImpl;
import org.eclipse.xtend.XtendComponent;
import org.eclipse.xtend.typesystem.emf.EmfRegistryMetaModel;

import com.googlecode.eclipseutils.mtf.Activator;


// abstract
public class TransformationComponent extends XtendComponent {

	public TransformationComponent(String xtendFile, String expression) {
		ResourceLoaderFactory
				.setCurrentThreadResourceLoader(new ResourceLoaderImpl(
						Activator.class.getClassLoader()));
		this.setInvoke(xtendFile + "::" + expression);
	}

	// added:
	public TransformationComponent(String xtendFile, String expression,
			EmfRegistryMetaModel incomingModel,
			EmfRegistryMetaModel outgoingModel) {
		this(xtendFile, expression);
		addMetaModel(incomingModel);
		addMetaModel(outgoingModel);
	}

	public void addGlobalVarDef(String name, String value) {
		GlobalVarDef def = new GlobalVarDef();
		def.setName(name);
		def.setValue(value);

		this.addGlobalVarDef(def);
	}

}