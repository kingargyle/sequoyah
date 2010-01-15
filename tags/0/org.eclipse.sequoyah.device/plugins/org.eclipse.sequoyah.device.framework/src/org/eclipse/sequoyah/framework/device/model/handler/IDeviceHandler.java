/********************************************************************************
 * Copyright (c) 2007-2008 Motorola Inc and others.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * Fabio Fantato (Motorola)
 * 
 * Contributors:
 * Otavio Luiz Ferranti (Eldorado Research Institute) - bug#221733 - Device handlers
 *                                         shall be able to create device instances.
 * Fabio Fantato (Instituto Eldorado) - [263188] - Create new examples to support tutorial presentation
 ********************************************************************************/
package org.eclipse.tml.framework.device.model.handler;

import org.eclipse.tml.framework.device.model.IDeviceLauncher;
import org.eclipse.tml.framework.device.model.IInstance;

public interface IDeviceHandler {

	public IInstance createDeviceInstance (String id);
	public IDeviceLauncher createDeviceLauncher(IInstance instance);
	
}