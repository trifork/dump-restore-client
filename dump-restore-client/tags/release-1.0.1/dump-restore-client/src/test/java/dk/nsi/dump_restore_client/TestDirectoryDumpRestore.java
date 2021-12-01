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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.1/dump-restore-client/src/test/java/dk/nsi/dump_restore_client/TestDirectoryDumpRestore.java $
 *  $Id: TestDirectoryDumpRestore.java 10985 2013-04-02 20:22:06Z ChristianGasser $
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
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2013-04-02 22:22:06 +0200 (Tue, 02 Apr 2013) $
 * @version $Revision: 10985 $
 */
public class TestDirectoryDumpRestore {

    @Test
    public void testDumpRestore() throws IOException {
        File directory = new File(".");
        File file = new File("1234567890-FMK.dump");
        try {
            DirectoryDumpRestore dumpRestore = new DirectoryDumpRestore(directory);
            dumpRestore.restore("FMK", "1234567890", "fgfdgfdhgaer324526gfg5u7gsfgsdf");
            assertTrue(file.exists());
            BufferedReader reader = new BufferedReader(new FileReader(file));
            assertEquals("fgfdgfdhgaer324526gfg5u7gsfgsdf", reader.readLine());
            reader.close();

            //test overwrite
            dumpRestore.restore("FMK", "1234567890", "Huuuuuuhuuuuu");
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
