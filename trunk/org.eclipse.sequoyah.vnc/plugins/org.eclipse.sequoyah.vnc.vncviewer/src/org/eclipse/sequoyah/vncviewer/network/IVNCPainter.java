/********************************************************************************
 * Copyright (c) 2007 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Daniel Franco (Motorola)
 *
 * Contributors:
 * Eugene Melekhov (Montavista) - Bug [227793] - Implementation of the several encodings, performance enhancement etc
 ********************************************************************************/

package org.eclipse.tml.vncviewer.network;

import java.io.DataInputStream;



public interface IVNCPainter extends IPainter{

	/**
	 * Sets the PixelFormat used by the IPainter implementation.
	 * @param pixelFormat the PixelFomat.
	 */
	public void setPixelFormat(PixelFormat pixelFormat);
	
	/**
	 * Returns the PixelFormat object.
	 * @return the PixelFormat
	 */
	public PixelFormat getPixelFormat();
	
	/**
	 * Process a rectangle of data sent by the VNC Server.
	 */
	public void processRectangle(RectHeader rectHeader, DataInputStream in) throws Exception;

	void updateRectangle(int x1, int y1, int x2, int y2);

	int[] getSupportedEncodings();	
}
