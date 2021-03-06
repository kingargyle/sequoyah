/********************************************************************************
 * Copyright (c) 2009-2010 Motorola Inc.
 * All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * Vinicius Hernandes (Motorola)
 * 
 * Contributors:
 * Daniel Pastore (Eldorado) - [289870] Moving and renaming Tml to Sequoyah 
 ********************************************************************************/
package org.eclipse.sequoyah.localization.tools.persistence;

import java.util.List;

/**
 *
 */
public class PersistableAttributes {

	private boolean all;

	private List<String> attributeNames;

	/**
	 * 
	 */
	public void setAll() {

	}

	/**
	 * @return
	 */
	public List<String> getAttributeNames() {
		return attributeNames;
	}

	/**
	 * @param attributeNames
	 */
	public void setAttributeNames(List<String> attributeNames) {
		this.attributeNames = attributeNames;
	}

	/**
	 * @param attrName
	 */
	public void addAttributeName(String attrName) {

	}

}
