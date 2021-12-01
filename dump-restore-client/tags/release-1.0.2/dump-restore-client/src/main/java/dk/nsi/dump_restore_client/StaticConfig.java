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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.2/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/StaticConfig.java $
 *  $Id: StaticConfig.java 10985 2013-04-02 20:22:06Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import java.net.URL;
import java.util.List;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2013-04-02 22:22:06 +0200 (Tue, 02 Apr 2013) $
 * @version $Revision: 10985 $
 */
public interface StaticConfig {
    List<String> getEnvironments();

    List<String> getServices();

    URL getEndpoint(String environment, String service);

    List<String> getWhitelistedCPRs();

    public static class ConfigException extends RuntimeException {
        public ConfigException(String msg, Exception e) {
            super(msg, e);
        }

        public ConfigException(String msg) {
            super(msg);
        }
    }
}
