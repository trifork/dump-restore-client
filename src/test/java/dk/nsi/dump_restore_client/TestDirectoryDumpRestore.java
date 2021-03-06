/*
 *
 *  Dump/Restore client - Copyright (C) 2013 National Board of e-Health (NSI)
 *
 *  All source code and information supplied as part of 'dump-restore-client' is
 *  copyright to National Board of e-Health.
 *
 *  The source code has been released under a dual license - meaning you can
 *  use either licensed version of the library with your code.
 *
 *  It is released under the Common Public License 1.0, a copy of which can
 *  be found at the link below.
 *  http://www.opensource.org/licenses/cpl1.0.php
 *
 *  It is released under the LGPL (GNU Lesser General Public License), either
 *  version 2.1 of the License, or (at your option) any later version. A copy
 *  of which can be found at the link below.
 *  http://www.gnu.org/copyleft/lesser.html
 *
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/trunk/dump-restore-client/src/test/java/dk/nsi/dump_restore_client/TestDirectoryDumpRestore.java $
 *  $Id: TestDirectoryDumpRestore.java 34625 2017-12-13 16:09:52Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2017-12-13 17:09:52 +0100 (Wed, 13 Dec 2017) $
 * @version $Revision: 34625 $
 */
public class TestDirectoryDumpRestore {

    @Test
    public void testDumpRestore() throws IOException {
        File directory = new File(".");
        File file = new File("1234567890-FMK.dump");
        try {
            DirectoryDumpRestore dumpRestore = new DirectoryDumpRestore(directory);
            dumpRestore.restore("FMK", "1234567890", "fgfdgfdhgaer324526gfg5u7gsfgsdf", null);
            assertTrue(file.exists());
            BufferedReader reader = new BufferedReader(new FileReader(file));
            assertEquals("fgfdgfdhgaer324526gfg5u7gsfgsdf", reader.readLine());
            reader.close();

            //test overwrite
            dumpRestore.restore("FMK", "1234567890", "Huuuuuuhuuuuu", null);
            reader = new BufferedReader(new FileReader(file));
            assertEquals("Huuuuuuhuuuuu", reader.readLine());
            reader.close();

            String dump = dumpRestore.dump("FMK", "1234567890");
            assertEquals("Huuuuuuhuuuuu", dump);
        } finally {
            file.deleteOnExit();
        }
    }

    @Test(expected = NonFatalException.class)
    public void testNonExistingDump() {
        File directory = new File(".");
        new DirectoryDumpRestore(directory).dump("1234567890", "FMK");
    }

}
