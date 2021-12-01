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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.0/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/CprValidator.java $
 *  $Id: CprValidator.java 10919 2013-03-22 07:42:57Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.apache.log4j.Logger;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2013-03-22 08:42:57 +0100 (Fri, 22 Mar 2013) $
 * @version $Revision: 10919 $
 */
public class CprValidator {

    private static Logger log = Logger.getLogger(CprValidator.class);

    static boolean validateCpr(String cpr) {
        if (cpr == null || cpr.isEmpty()) return false;
        if (cpr.length() != 10) {
            log.warn("'" + cpr + "' er ikke et gyldigt CPR-nummer på 10 tegn!");
            return false;
        } else {
            return true;
        }
    }
}
