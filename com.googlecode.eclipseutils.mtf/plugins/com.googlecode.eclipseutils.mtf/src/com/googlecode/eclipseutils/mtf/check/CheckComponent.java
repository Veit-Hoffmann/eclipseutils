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
package com.googlecode.eclipseutils.mtf.check;

import org.eclipse.emf.mwe.core.resources.ResourceLoaderFactory;
import org.eclipse.emf.mwe.core.resources.ResourceLoaderImpl;
import org.eclipse.xtend.typesystem.emf.EmfRegistryMetaModel;

import com.googlecode.eclipseutils.mtf.Activator;


/**
 * 
 * @author mfunk
 * 
 */
// abstract
public class CheckComponent extends org.eclipse.xtend.check.CheckComponent {

	public CheckComponent(String checkFile, String expression) {
		ResourceLoaderFactory
				.setCurrentThreadResourceLoader(new ResourceLoaderImpl(
						Activator.class.getClassLoader()));
		initialize();
		this.addCheckFile(checkFile);
		this.setExpression(expression);
	}

	// Added Constructor
	public CheckComponent(String checkFile, String expression,
			EmfRegistryMetaModel metaModel) {
		this(checkFile, expression);
		addMetaModel(metaModel);
	}

	/**
	 * configure check component
	 * 
	 */
	public void initialize() {
		this.setAbortOnError(false);
		this.setSkipOnErrors(false);
		this.setWarnIfNothingChecked(false);
	}

}
