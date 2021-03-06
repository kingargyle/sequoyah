/**
 * Copyright (c) 2009 Motorola
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gustavo de Paula (Motorola) - Initial implementation
 *     David Dubrow (Nokia)
 *     David Marques (Motorola) - Fixing sorting repositories.
 */
package org.eclipse.sequoyah.pulsar.internal.provisional.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.sequoyah.pulsar.core.Activator;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides services that are needed by the quickinstall view
 * 
 */
public class QuickInstallCore {
	
	private static QuickInstallCore instance = new QuickInstallCore();

	private QuickInstallCore() {
	}
	
	/**
	 * Returns an instance of the quick instance core services singleton
	 * 
	 * @return QuickInstallCore
	 */
	public static QuickInstallCore getInstance() {
		return instance;
	}

	/**
	 * Returns a collection of SDK repositories. 
	 * 
	 * @return Collection
	 */
	public Collection<ISDKRepository> getSDKRepositories() {
		Set<ISDKRepository> repositories = new HashSet<ISDKRepository>(); 
		
		Collection<ISDKRepositoryProvider> providers = Activator.getDefault().getSDKRepositoryProviders();
		for (ISDKRepositoryProvider provider : providers) {
			repositories.addAll(provider.getRepositories());
		}
		
		// Sorts the SDK repositories by name (ignoring the case) 
		List<ISDKRepository> list = new ArrayList<ISDKRepository>(repositories); 
		Collections.sort(list, new Comparator<ISDKRepository>() { 
			public int compare(ISDKRepository arg0, ISDKRepository arg1) { 
				return arg0.getName().compareToIgnoreCase(arg1.getName()); 
			} 
		});
		return list;
	}
	
	/**
	 * Installs an SDK using the installer UI
	 * 
	 * @param parentShell Shell
	 * @param sdk ISDK
	 * @param installerUI IInstallerUI
	 * 
	 * @throws CoreException
	 */
	public void installSDK(Shell parentShell, ISDK sdk, IInstallerUI installerUI) throws CoreException {
		installerUI.runInstaller(parentShell, sdk);
	}
	
	/**
	 * Uninstalls an SDK using the installer UI
	 * 
	 * @param parentShell Shell
	 * @param sdk ISDK
	 * @param installerUI IInstallerUI
	 * 
	 * @throws CoreException
	 */
	public void uninstallSDK(Shell parentShell, ISDK sdk, IInstallerUI installerUI) throws CoreException {
		installerUI.runUninstaller(parentShell, sdk);
	}
 
	
}
