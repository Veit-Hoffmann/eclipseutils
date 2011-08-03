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
package com.googlecode.eclipseutils.mtf.generation;

import java.util.List;

import org.eclipse.emf.mwe.core.resources.ResourceLoaderFactory;
import org.eclipse.emf.mwe.core.resources.ResourceLoaderImpl;
import org.eclipse.xpand2.Generator;
import org.eclipse.xpand2.output.Outlet;
import org.eclipse.xpand2.output.PostProcessor;
import org.eclipse.xtend.typesystem.emf.EmfMetaModel;

import com.googlecode.eclipseutils.mtf.Activator;


// abstract
public class GenerationComponent extends Generator {

	private boolean initialized = false;

	public GenerationComponent(String xpandFile, String expression) {
		ResourceLoaderFactory.setCurrentThreadResourceLoader(new ResourceLoaderImpl(Activator.class.getClassLoader()));
		this.setExpand(xpandFile + "::" + expression);
	}

	// Added Constructor
	public GenerationComponent(String xpandFile, String expression, EmfMetaModel metaModel) {
		this(xpandFile,
			expression);
		this.addMetaModel(metaModel);
	}

	// Added:
	public void initialize(String path, List<PostProcessor> postProcessors) {
		setOutput(null); // workaround to deal with lazy initialization of outlets in Generator
		Outlet outlet = new Outlet();
		addOutlet(outlet);
		outlet.setPath(path);
		if (postProcessors != null) {
			for (PostProcessor postProcessor : postProcessors) {
				outlet.addPostprocessor(postProcessor);
			}
		}
		setPrSrcPaths(path);
		initialized = true;
	}

	public boolean isInitialized() {
		return initialized;
	}

}
