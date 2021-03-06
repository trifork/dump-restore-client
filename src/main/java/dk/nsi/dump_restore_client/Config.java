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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/trunk/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/Config.java $
 *  $Id: Config.java 34023 2017-03-01 15:18:49Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import java.net.URL;
import java.util.List;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2017-03-01 16:18:49 +0100 (Wed, 01 Mar 2017) $
 * @version $Revision: 34023 $
 */
public interface Config {
    List<String> getEnvironments();

    List<String> getServices();

    List<String> getSelectedServices();

    void addServiceToSelected(String service);

    void removeServiceFromSelected(String service);

    URL getEndpoint(String environment, String service);

    List<String> getWhitelistedCPRs();

    class ConfigException extends RuntimeException {
        public ConfigException(String msg, Exception e) {
            super(msg, e);
        }

        public ConfigException(String msg) {
            super(msg);
        }
    }
}
