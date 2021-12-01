/*
 *
 *  Dump/Restore client - Copyright (C) 2013 National Board of e-Health (NSI)
 *
 *  All source code and information supplied as part of <Komponent-Navn> is
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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.0/dump-restore-client/src/test/java/dk/nsi/dump_restore_client/TestSoapDumpRestore.java $
 *  $Id: TestSoapDumpRestore.java 10919 2013-03-22 07:42:57Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2013-03-22 08:42:57 +0100 (Fri, 22 Mar 2013) $
 * @version $Revision: 10919 $
 */
public class TestSoapDumpRestore {

    private SoapDumpRestore soapDumpRestore;

    @Before
    public void setUp() {
        StaticConfig staticConfig = new StaticConfig() {
            public List<String> getEnvironments() {
                return null;
            }

            public List<String> getServices() {
                return null;
            }

            public URL getEndpoint(String environment, String service) {
                try {
                    return new URL("http://test2.fmk.netic.dk/fmk12/ws/DumpRestore");
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }

            public List<String> getWhitelistedCPRs() {
                return null;
            }
        };
        soapDumpRestore = new SoapDumpRestore(staticConfig, "TEST2");
    }

    @After
    public void tearDown() {
        soapDumpRestore = null;
    }

    @Test
    public void testBuildDumpRequest() {
        String xml = soapDumpRestore.buildDumpRequest("1111111118");
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soapenv:Body>\n" +
                "    <ssi2013:DumpPatientsRequest xmlns:ssi2013=\"http://www.ssi.dk/nsi/xml.schema/2013/01/01\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "      <ssi2013:Identifier>\n" +
                "        <ssi2013:PersonIdentifier>1111111118</ssi2013:PersonIdentifier>\n" +
                "      </ssi2013:Identifier>\n" +
                "    </ssi2013:DumpPatientsRequest>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>", xml);
    }

    @Test
    public void testBuildRestoreRequest() {
        String xml = soapDumpRestore.buildRestoreRequest("1111111118", "rtrdfadfvwedk124");
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soapenv:Body>\n" +
                "    <ssi2013:RestorePatientsRequest xmlns:ssi2013=\"http://www.ssi.dk/nsi/xml.schema/2013/01/01\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "      <ssi2013:Dump>\n" +
                "        <ssi2013:Identifier>\n" +
                "          <ssi2013:PersonIdentifier>1111111118</ssi2013:PersonIdentifier>\n" +
                "        </ssi2013:Identifier>\n" +
                "        <ssi2013:DumpData>rtrdfadfvwedk124</ssi2013:DumpData>\n" +
                "      </ssi2013:Dump>\n" +
                "    </ssi2013:RestorePatientsRequest>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>", xml);
    }

    @Ignore("Disabled in order not to be run as part of the build")
    @Test
    public void callDump() throws MalformedURLException {
        System.setProperty("javax.net.debug", "all");
        String base64Dump = soapDumpRestore.dump("whatever", "2401010000");
        System.out.println(base64Dump);
    }

    @Ignore("Disabled in order not to be run as part of the build")
    @Test
    public void callDumpAndRestore() throws MalformedURLException {
        String cpr = "2401010001";
        String base64Dump = soapDumpRestore.dump("whatever", cpr);
        // Restore to same CPR
        soapDumpRestore.restore("whatever", cpr, base64Dump);

    }


}
