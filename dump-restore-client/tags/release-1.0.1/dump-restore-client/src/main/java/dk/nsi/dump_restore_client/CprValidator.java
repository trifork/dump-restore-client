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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.1/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/CprValidator.java $
 *  $Id: CprValidator.java 10993 2013-04-03 11:44:21Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.apache.log4j.Logger;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2013-04-03 13:44:21 +0200 (Wed, 03 Apr 2013) $
 * @version $Revision: 10993 $
 */
public class CprValidator {

    private static Logger log = Logger.getLogger(CprValidator.class);

    static boolean validateNonEmpty(String cpr) {
        return cpr != null && ! cpr.isEmpty();
    }

    static boolean validatePair(CprPair pair) {
        boolean sourceValid = validate(pair.getSourceCpr());
        boolean targetValid = validate(pair.getTargetCpr());
        return sourceValid && targetValid;
    }

    static boolean validate(String cpr) {
        if (cpr == null || cpr.isEmpty()) return false;
        if (cpr.length() != 10) {
            log.warn("'" + cpr + "' er ikke et gyldigt CPR-nummer på 10 tegn!");
            return false;
        } else {
            return true;
        }
    }

}
