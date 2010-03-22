/********************************************************************************
 * Copyright (c) 2007-2010 Motorola Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * Fabio Fantato (Motorola)
 * 
 * Contributors:
 * Fabio Rigo (Eldorado Research Institute) - Bug [262632] - Avoid providing raw streams to the user in the protocol framework
 * Daniel Pastore (Eldorado) - [289870] Moving and renaming Tml to Sequoyah
 ********************************************************************************/
package org.eclipse.sequoyah.vnc.vncviewer.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.sequoyah.vnc.vncviewer.VNCViewerPlugin;
import org.osgi.framework.Bundle;

import static org.eclipse.sequoyah.vnc.utilities.logger.Logger.log;

public class EclipsePropertiesFileHandler implements IPropertiesFileHandler {

	
	public Properties loadPropertiesFile(String fileAddress) {

	    log(EclipsePropertiesFileHandler.class).info(Messages.EclipsePropertiesFileHandler_0 + fileAddress);
		Properties properties = new Properties(); 
		Bundle pluginBundle = VNCViewerPlugin.getDefault().getBundle();
		URL vncViewerConf = pluginBundle.getResource(fileAddress);
		
		try {
			InputStream vncViewerConfStream = vncViewerConf.openStream();
			properties.load(vncViewerConfStream);
		} catch (IOException e) {
			// TODO handle properly
		    log(EclipsePropertiesFileHandler.class).error(Messages.EclipsePropertiesFileHandler_1 + 
		            e.getMessage());
			e.printStackTrace();
		}
		
		return properties;
	}
	
	

	public boolean savePropertiesFile(URL fileURL, Properties properties)
			throws Exception {

		return false;
	}



}