/*
 * $Id$
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * -Redistributions of source code must retain the above copyright
 * notice, this  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF  OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package com.sun.gi.apps.jnwn;

import com.sun.gi.comm.routing.UserID;
import com.sun.gi.comm.routing.ChannelID;
import com.sun.gi.logic.GLO;
import com.sun.gi.logic.GLOReference;
import com.sun.gi.logic.SimTask;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 *
 * @author  James Megquier
 * @version $Rev$, $Date$
 */
public class Character implements GLO {

    private static final long serialVersionUID = 1L;

    private static Logger log = Logger.getLogger("com.sun.gi.apps.jnwn");

    private int     characterID;
    private String  name;
    private GLOReference<User>       userRef;
    private GLOReference<Area>       areaRef;
    private GLOReference<Character>  thisRef;

    private final String DEFAULT_AREA_NAME = "Area:uncleflakeyshous";

    public static GLOReference<Character> create(GLOReference<User> uref) {
	SimTask task = SimTask.getCurrent();
	Character c = new Character(uref);
	String charName = "Character-" + c.getCharacterID();
	GLOReference<Character> ref = task.createGLO(c, charName);
	ref.get(task).boot(ref);
	return ref;
    }

    protected Character(GLOReference<User> uref) {
	SimTask task = SimTask.getCurrent();
	characterID = (int) System.currentTimeMillis(); // FIXME
	name = uref.peek(task).getName();
	userRef = uref;
	areaRef = task.findGLO(DEFAULT_AREA_NAME);
    }

    protected void boot(GLOReference<Character> ref) {
	thisRef = ref;
    }

    public void setAreaRef(GLOReference<Area> area) {
	areaRef = area;
    }

    public String getName() {
	return userRef.peek(SimTask.getCurrent()).getName();
    }

    public GLOReference<Character> getReference() {
	return thisRef;
    }
    public void joinArea() {
	areaRef.peek(SimTask.getCurrent()).addCharacter(this);
    }

    public UserID getUID() {
	return userRef.peek(SimTask.getCurrent()).getUID();
    }

    public int getCharacterID() {
	return characterID;
    }

    public void joinedChannel(ChannelID channelID) {
	SimTask task = SimTask.getCurrent();
	if (areaRef != null) {
	    areaRef.get(task).characterJoined(thisRef.get(task));
	}
    }

    public void leftChannel(ChannelID channelID) {
	SimTask task = SimTask.getCurrent();
	if (areaRef != null) {
	    areaRef.get(task).characterLeft(thisRef.get(task));
	}
    }

    public void dataReceived(ByteBuffer data) {
	SimTask task = SimTask.getCurrent();
	if (areaRef != null) {
	    areaRef.get(task).dataReceived(thisRef.get(task), data);
	}
    }
}