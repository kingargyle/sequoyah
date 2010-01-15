/********************************************************************************
 * Copyright (c) 2008, 2009 Motorola Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * Fabio Rigo (Eldorado) - Bug [244066] - The services are being run at one of the UI threads 
 * 
 * Contributors:
 * Fabio Rigo (Eldorado Research Institute) - [246212] - Enhance encapsulation of protocol implementer 
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [246585] - VncViewerService is not working anymore after changes made in ProtocolHandle
 * Fabio Rigo (Eldorado Research Institute) - Bug [262632] - Avoid providing raw streams to the user in the protocol framework
 * Mauren Brenner (Eldorado) - [282394] Remove protocol handle from map in VNCProtocolRegistry
 ********************************************************************************/

package org.eclipse.tml.service.vncviewer.handler;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tml.framework.device.model.IInstance;
import org.eclipse.tml.framework.device.model.handler.IServiceHandler;
import org.eclipse.tml.framework.device.model.handler.ServiceHandler;
import org.eclipse.tml.protocol.PluginProtocolActionDelegate;
import org.eclipse.tml.protocol.lib.ProtocolHandle;
import org.eclipse.tml.vncviewer.registry.VNCProtocolRegistry;
import org.eclipse.tml.vncviewer.vncviews.views.VNCViewerView;

public class UnplugVNCViewerServiceHandler extends ServiceHandler
{

    @Override
    public IServiceHandler newInstance()
    {
        return new UnplugVNCViewerServiceHandler();
    }

    @Override
    public IStatus runService(IInstance instance, Map<Object, Object> arguments,
            IProgressMonitor monitor)
    {

        VNCViewerView.stop();

        ProtocolHandle handle = VNCViewerView.protocolHandle;
        PluginProtocolActionDelegate.requestStopProtocol(handle);
        
        VNCProtocolRegistry vncReg = VNCProtocolRegistry.getInstance();
        vncReg.unregister(handle);

        return Status.OK_STATUS;
    }

    @Override
    public IStatus updatingService(IInstance instance, IProgressMonitor monitor)
    {
        return Status.OK_STATUS;
    }

}
