/********************************************************************************
 * Copyright (c) 2007-2008 Motorola Inc and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Fabio Fantato (Motorola)
 *
 * Contributors:
 * Otavio Luiz Ferranti (Eldorado Research Institute) - bug#221733 - Enhancing instance wizard
 ********************************************************************************/

package org.eclipse.tml.device.qemureact.wizard;

import org.eclipse.tml.device.qemureact.QEmuReactPlugin;
import org.eclipse.tml.framework.device.wizard.ui.AbstractNewEmulatorInstanceWizard;


/**
 * New wizard specific for this emulator
 * @author Fabio Fantato
 */
public class DefaultNewEmulatorInstanceWizard extends AbstractNewEmulatorInstanceWizard {
	
	/**
	 * Constructor - Create a new wizard specific for this emulator
	 */
	public DefaultNewEmulatorInstanceWizard(){
		super(QEmuReactPlugin.PLUGIN_ID, QEmuReactPlugin.DEVICE_ID, QEmuReactPlugin.WIZARD_ID);		
	}
		
}
