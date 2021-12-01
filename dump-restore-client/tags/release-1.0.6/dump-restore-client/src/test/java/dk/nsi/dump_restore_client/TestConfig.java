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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.6/dump-restore-client/src/test/java/dk/nsi/dump_restore_client/TestConfig.java $
 *  $Id: TestConfig.java 34023 2017-03-01 15:18:49Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2017-03-01 16:18:49 +0100 (Wed, 01 Mar 2017) $
 * @version $Revision: 34023 $
 */
public class TestConfig {

    private static final String MINIMAL_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<config>\n" +
            "    <environments>\n" +
            "        <environment>Test1</environment>\n" +
            "    </environments>\n" +
            "    <services>\n" +
            "        <service name=\"FMK\">\n" +
            "            <endpoint environment=\"Test1\">http://test1.fmk.dk</endpoint>\n" +
            "        </service>\n" +
            "    </services>\n" +
            "    <cpr-whitelist>\n" +
            "        1111112222\n" +
            "    </cpr-whitelist>\n" +
            "</config>\n";

    @Before
    public void initLogging() {
        BasicConfigurator.configure(new NullAppender());
    }

    @After
    public void resetLogging() {
        BasicConfigurator.resetConfiguration();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullInputStream() {
        ConfigImpl.loadFromStream(null);
    }

    @Test(expected = Config.ConfigException.class)
    public void testEmptyStream() {
        ConfigImpl.loadFromStream(new ByteArrayInputStream("".getBytes()));
    }

    @Test(expected = Config.ConfigException.class)
    public void testMalformedConfiguration() {
        ConfigImpl.loadFromStream(new ByteArrayInputStream("<root/>".getBytes()));
    }

    @Test
    public void testInvalidConfigurations() {
        try {
            String configString = MINIMAL_CONFIG.replace("<environment>Test1</environment>","<environment>Test2</environment>");
            ConfigImpl.loadFromStream(new ByteArrayInputStream(configString.getBytes()));
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("Undefined environment: 'Test1' in service definition for service 'FMK'", e.getMessage());
        }

        try {
            String configString = MINIMAL_CONFIG.replace("http://","");
            ConfigImpl.loadFromStream(new ByteArrayInputStream(configString.getBytes()));
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("Cannot parse endpoint 'test1.fmk.dk' in service definition for service 'FMK' as an URL", e.getMessage());
        }

        try {
            String configString = MINIMAL_CONFIG.replace("<environment>Test1</environment>","<environment>Test1</environment><environment>Test2</environment>");
            ConfigImpl.loadFromStream(new ByteArrayInputStream(configString.getBytes()));
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("Missing endpoint definition for environment 'Test2' in service definition for service 'FMK'", e.getMessage());
        }

        try {
            String configString = MINIMAL_CONFIG.replace("1111112222","11111");
            ConfigImpl.loadFromStream(new ByteArrayInputStream(configString.getBytes()));
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("CPR '11111' in cpr-whitelist is not 10 characters long", e.getMessage());
        }

        try {
            String configString = MINIMAL_CONFIG.replace("1111112222","111111-2222");
            ConfigImpl.loadFromStream(new ByteArrayInputStream(configString.getBytes()));
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("CPR '111111-2222' in cpr-whitelist is not 10 characters long", e.getMessage());
        }

        try {
            String configString = MINIMAL_CONFIG.replace("1111112222","1111112222\n111111");
            ConfigImpl.loadFromStream(new ByteArrayInputStream(configString.getBytes()));
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("CPR '111111' in cpr-whitelist is not 10 characters long", e.getMessage());
        }
    }

    @Test
    public void testValidConfiguration() throws MalformedURLException {
        Config staticConfig = ConfigImpl.loadFromStream(TestConfig.class.getResourceAsStream("/unittest-dump-restore-config.xml"));
        List<String> environments = staticConfig.getEnvironments();
        assertNotNull(environments);
        assertEquals(2, environments.size());
        assertTrue(environments.contains("Test1"));
        assertTrue(environments.contains("Test2"));

        List<String> services = staticConfig.getServices();
        assertNotNull(services);
        assertEquals(2, services.size());
        assertTrue(services.contains("FMK"));
        assertTrue(services.contains("DDV"));

        assertEquals(new URL("http://test1.fmk.dk"), staticConfig.getEndpoint("Test1", "FMK"));
        assertEquals(new URL("http://test2.fmk.dk"), staticConfig.getEndpoint("Test2", "FMK"));
        assertEquals(new URL("http://ddv-test1.dk"), staticConfig.getEndpoint("Test1", "DDV"));
        assertEquals(new URL("http://ddv-test2.dk"), staticConfig.getEndpoint("Test2", "DDV"));

        try {
            staticConfig.getEndpoint("Test3", "FMK");
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("Unknown environment: 'Test3'", e.getMessage());
        }

        try {
            staticConfig.getEndpoint("", "FMK");
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("Unknown environment: ''", e.getMessage());
        }

        try {
            staticConfig.getEndpoint(null, "FMK");
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("Unknown environment: 'null'", e.getMessage());
        }

        try {
            staticConfig.getEndpoint("Test1", "CPR");
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("Unknown service: 'CPR'", e.getMessage());
        }

        try {
            staticConfig.getEndpoint("Test1", "");
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("Unknown service: ''", e.getMessage());
        }

        try {
            staticConfig.getEndpoint("Test1", null);
            fail();
        } catch (Config.ConfigException e) {
            assertEquals("Unknown service: 'null'", e.getMessage());
        }

        List<String> whitelistedCPRs = staticConfig.getWhitelistedCPRs();
        assertNotNull(whitelistedCPRs);
        assertEquals(3, whitelistedCPRs.size());
        assertTrue(whitelistedCPRs.contains("1111112222"));
        assertTrue(whitelistedCPRs.contains("1111113333"));
        assertTrue(whitelistedCPRs.contains("1111114444"));
    }

}
