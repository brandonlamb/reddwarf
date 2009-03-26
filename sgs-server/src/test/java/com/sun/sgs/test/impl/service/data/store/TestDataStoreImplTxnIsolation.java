/*
 * Copyright 2007-2008 Sun Microsystems, Inc.
 *
 * This file is part of Project Darkstar Server.
 *
 * Project Darkstar Server is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation and
 * distributed hereunder to you.
 *
 * Project Darkstar Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sun.sgs.test.impl.service.data.store;

import com.sun.sgs.impl.service.data.store.DataStore;
import com.sun.sgs.impl.service.data.store.DataStoreImpl;
import com.sun.sgs.impl.service.data.store.db.bdb.BdbEnvironment;
import com.sun.sgs.impl.service.data.store.db.je.JeEnvironment;
import com.sun.sgs.tools.test.FilteredNameRunner;
import java.io.File;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Tests the isolation that {@link DataStoreImpl} enforces between
 * transactions.
 */
@RunWith(FilteredNameRunner.class)
public class TestDataStoreImplTxnIsolation extends BasicTxnIsolationTest {

    /** The name of the DataStoreImpl class. */
    private static final String DataStoreImplClassName =
	DataStoreImpl.class.getName();

    /** The directory used for the database shared across multiple tests. */
    private static final String dbDirectory =
	System.getProperty("java.io.tmpdir") + File.separator +
	"TestDataStoreImplTxnIsolation.db";

    /** Clean the database directory. */
    @BeforeClass
    public static void beforeClass() {
	cleanDirectory(dbDirectory);
    }

    /** Adds properties specific to {@link DataStoreImpl}. */
    protected Properties createProperties() {
	Properties props = super.createProperties();
	props.setProperty(DataStoreImplClassName + ".directory", dbDirectory);
	props.setProperty(BdbEnvironment.LOCK_TIMEOUT_PROPERTY,
			  String.valueOf(timeoutSuccess));
	props.setProperty(JeEnvironment.LOCK_TIMEOUT_PROPERTY,
			  String.valueOf(timeoutSuccess));
	return props;
    }

    /** Creates a {@link DataStoreImpl}. */
    protected DataStore createDataStore() {
	return new DataStoreImpl(props, accessCoordinator);
    }

    /** Insures an empty version of the directory exists. */
    private static void cleanDirectory(String directory) {
	File dir = new File(directory);
	if (dir.exists()) {
	    for (File f : dir.listFiles()) {
		if (!f.delete()) {
		    throw new RuntimeException("Failed to delete file: " + f);
		}
	    }
	    if (!dir.delete()) {
		throw new RuntimeException(
		    "Failed to delete directory: " + dir);
	    }
	}
	if (!dir.mkdir()) {
	    throw new RuntimeException(
		"Failed to create directory: " + dir);
	}
    }
}
