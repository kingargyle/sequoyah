/********************************************************************************
 * Copyright (c) 2007-2008 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Daniel Franco (Motorola)
 *
 * Contributors:
 * Fabio Rigo - Bug [221741] - Support to VNC Protocol Extension
 * Eugene Melekhov (Montavista) - Bug [227793] - Implementation of the several encodings, performance enhancement etc
 * Fabio Rigo (Eldorado Research Institute) - [246212] - Enhance encapsulation of protocol implementer
 * Fabio Rigo (Eldorado Research Institute) - Bug [262632] - Avoid providing raw streams to the user in the protocol framework
 ********************************************************************************/

package org.eclipse.sequoyah.vnc.vncviewer.network;

import java.io.DataInput;
import java.io.OutputStream;

import org.eclipse.sequoyah.vnc.protocol.lib.exceptions.ProtocolException;

/**
 * This class implements the version 3.8 of the RFB Protocol.
 */
public class VNCProtocol38 extends VNCProtocol {

	/**
	 * Constant that represents the RFB protocol version.
	 */
	static final String RFB_VERSION = "RFB 003.008\n"; /* //$NON-NLS-1$ //$NON-NLS-1$
	 * used to compare the
	 * implemented version
	 * with the server
	 * version
	 */

	/**
	 * Constant that defines the number of bytes read in the handshake phase.
	 */
	static final int HANDSHAKE_MESSAGE_SIZE = 12; /*
	 * number of bytes read in
	 * the handshake phase
	 */

	/**
	 * This method compares each byte of the RFB Protocol client version using
	 * the String sent by the Server.
	 */
	protected void compareVersion(byte[] b) throws Exception {

		if (!(new String(b)).equals(RFB_VERSION)) {

			throw new ProtocolException("Wrong protocol version."); //$NON-NLS-1$

		}

	}

	private void handshakeFail38(DataInput in) throws Exception {

		int failReasonLength;
		String failReason = ""; //$NON-NLS-1$

		failReasonLength = in.readInt();
		for (int j = 0; j < failReasonLength; j++) {
			failReason += in.readByte();

		}

		throw new Exception("Connection failed: " + failReason); //$NON-NLS-1$

	}

	/**
	 * Implements the handshake phase of the RFB Protocol.
	 */
	protected void handShake(DataInput in, OutputStream out)
			throws Exception {


		int secTypesNumber;
		int rfbSecTypes[];
		int handshakeOK;

		byte[] b = new byte[HANDSHAKE_MESSAGE_SIZE];

		in.readFully(b, 0, HANDSHAKE_MESSAGE_SIZE);

		compareVersion(b);
		out.write(b);

		// Security types handling
		secTypesNumber = in.readByte();

		if (secTypesNumber > 0) {
			rfbSecTypes = new int[secTypesNumber];

			for (int i = 0; i < secTypesNumber; i++) {
				rfbSecTypes[i] = in.readByte();
			}

			out.write((byte) 1); // NONE
		} else {
			handshakeFail38(in);
		}

		// Security Result
		handshakeOK = in.readInt();

		if (handshakeOK != 0) { // if fail
			handshakeFail(in);
		}

		/* The Sec-types handling is not implemented yet */
	}

	@Override
	protected String getVersion() {
		return RFB_VERSION;
	}

}
