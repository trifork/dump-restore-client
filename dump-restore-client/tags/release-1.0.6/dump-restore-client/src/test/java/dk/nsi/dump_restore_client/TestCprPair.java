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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.6/dump-restore-client/src/test/java/dk/nsi/dump_restore_client/TestCprPair.java $
 *  $Id: TestCprPair.java 10994 2013-04-03 11:52:45Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2013-04-03 13:52:45 +0200 (Wed, 03 Apr 2013) $
 * @version $Revision: 10994 $
 */
public class TestCprPair {

    @Test
    public void testFromDoubleArray() {
        List<CprPair> cprPairs = CprPair.listFromDoubleArray(null);
        assertEquals(0, cprPairs.size());

        cprPairs = CprPair.listFromDoubleArray(new Object[][] {});
        assertEquals(0, cprPairs.size());

        cprPairs = CprPair.listFromDoubleArray(new String[][] {{"",""}});
        assertEquals(0, cprPairs.size());

        cprPairs = CprPair.listFromDoubleArray(new String[][] {{"1234","1234567890"}});
        assertEquals(1, cprPairs.size());

        cprPairs = CprPair.listFromDoubleArray(new String[][] {{"1234567890","1234"}});
        assertEquals(1, cprPairs.size());

        cprPairs = CprPair.listFromDoubleArray(new String[][] {{"1234567890","1234567890"}});
        assertEquals(1, cprPairs.size());

        cprPairs = CprPair.listFromDoubleArray(new String[][] {{"  1234567890","1234567890  "}});
        assertEquals(1, cprPairs.size());

        cprPairs = CprPair.listFromDoubleArray(new String[][] {{"1234567890","1234567890"}, {"123","1234567890"}});
        assertEquals(2, cprPairs.size());

        cprPairs = CprPair.listFromDoubleArray(new String[][] {{"1234567890","2345678901"}, {"3456789012","4567890123"}});
        assertEquals(2, cprPairs.size());
        assertEquals("1234567890", cprPairs.get(0).getSourceCpr());
        assertEquals("2345678901", cprPairs.get(0).getTargetCpr());
        assertEquals("3456789012", cprPairs.get(1).getSourceCpr());
        assertEquals("4567890123", cprPairs.get(1).getTargetCpr());
    }

    @Test
    public void testToDoubleArray() {
        Object[][] objects = CprPair.toDoubleArray(null);
        assertNull(objects);

        List<CprPair> cprPairs = new LinkedList<CprPair>();
        objects = CprPair.toDoubleArray(cprPairs);
        assertEquals(0, objects.length);

        cprPairs.add(new CprPair("1234567890", "2345678901"));
        objects = CprPair.toDoubleArray(cprPairs);
        assertEquals(1, objects.length);
        assertEquals("1234567890", objects[0][0]);
        assertEquals("2345678901", objects[0][1]);

        cprPairs.add(new CprPair("3456789012", "4567890123"));
        objects = CprPair.toDoubleArray(cprPairs);
        assertEquals(2, objects.length);
        assertEquals("3456789012", objects[1][0]);
        assertEquals("4567890123", objects[1][1]);

    }

    @Test
    public void testToAndFromDoubleArray() {
        List<CprPair> cprPairs = new LinkedList<CprPair>();
        cprPairs.add(new CprPair("1234567890", "2345678901"));
        cprPairs.add(new CprPair("3456789012", "4567890123"));

        Object[][] objects = CprPair.toDoubleArray(cprPairs);
        List<CprPair> newCprPairs = CprPair.listFromDoubleArray(objects);

        assertEquals(cprPairs.size(), newCprPairs.size());
        assertEquals(cprPairs.get(0).getSourceCpr(), newCprPairs.get(0).getSourceCpr());
        assertEquals(cprPairs.get(0).getTargetCpr(), newCprPairs.get(0).getTargetCpr());
        assertEquals(cprPairs.get(1).getSourceCpr(), newCprPairs.get(1).getSourceCpr());
        assertEquals(cprPairs.get(1).getTargetCpr(), newCprPairs.get(1).getTargetCpr());

    }

    @Test
    public void testFromFile() throws IOException {
        File file = new File("cprpair-unittest.tmp");
        file.createNewFile();
        file.deleteOnExit();

        List<CprPair> cprPairs = CprPair.listFromFile(file);
        assertEquals(0, cprPairs.size());

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        writer.newLine();
        writer.flush();
        cprPairs = CprPair.listFromFile(file);
        assertEquals(0, cprPairs.size());

        writer.write("   ");
        writer.newLine();
        writer.flush();
        cprPairs = CprPair.listFromFile(file);
        assertEquals(0, cprPairs.size());

        writer.write("# This is a comment");
        writer.newLine();
        writer.flush();
        cprPairs = CprPair.listFromFile(file);
        assertEquals(0, cprPairs.size());

        writer.write("1234567890 2345678901");
        writer.newLine();
        writer.flush();
        cprPairs = CprPair.listFromFile(file);
        assertEquals(1, cprPairs.size());

        writer.write("2345678901 3456789012");
        writer.newLine();
        writer.flush();
        cprPairs = CprPair.listFromFile(file);
        assertEquals(2, cprPairs.size());

        writer.write("2345678901 345678");
        writer.newLine();
        writer.flush();
        cprPairs = CprPair.listFromFile(file);
        assertEquals(3, cprPairs.size());

        writer.write("345678 2345678901");
        writer.newLine();
        writer.flush();
        cprPairs = CprPair.listFromFile(file);
        assertEquals(4, cprPairs.size());

        writer.write("   1234567890          2345678901   ");
        writer.newLine();
        writer.flush();
        cprPairs = CprPair.listFromFile(file);
        assertEquals(5, cprPairs.size());

        assertEquals(cprPairs.get(0).getSourceCpr(), "1234567890");
        assertEquals(cprPairs.get(0).getTargetCpr(), "2345678901");

        assertEquals(cprPairs.get(1).getSourceCpr(), "2345678901");
        assertEquals(cprPairs.get(1).getTargetCpr(), "3456789012");

        assertEquals(cprPairs.get(4).getSourceCpr(), "1234567890");
        assertEquals(cprPairs.get(4).getTargetCpr(), "2345678901");
    }

}
