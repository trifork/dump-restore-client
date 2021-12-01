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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.0/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/Controller.java $
 *  $Id: Controller.java 10932 2013-03-22 14:42:28Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2013-03-22 15:42:28 +0100 (Fri, 22 Mar 2013) $
 * @version $Revision: 10932 $
 */
public class Controller {

    private static Logger log = Logger.getLogger(Controller.class);

    private final StaticConfig staticConfig;

    public Controller(StaticConfig staticConfig) {
        this.staticConfig = staticConfig;
    }

    public void invokeDumpRestore(DumpRestoreDomain source, DumpRestoreDomain target, List<CprPair> cprMapping) {
        if (!validateTargetCprNumbers(target, cprMapping)) return;

        log.info("Starter dump/restore fra '" + source.toPrettyString() + "' til '" + target.toPrettyString() + "'");
        int numberOfErrors = 0;
        for (CprPair cprPair : cprMapping) {
            log.info("Starter dump/restore fra '" + cprPair.getSourceCpr() + "' til '" + cprPair.getTargetCpr() + "'");
            List<String> services = staticConfig.getServices();
            for (String service : services) {
                boolean error = invokeForCprPairAndService(source, target, cprPair, service);
                if (error) numberOfErrors++;
            }
        }
        logErrors(numberOfErrors);
    }

    private boolean validateTargetCprNumbers(DumpRestoreDomain target, List<CprPair> cprMapping) {
        boolean containsInvalidCpr = false;
        for (CprPair cprPair : cprMapping) {
            if (!target.validateCprPair(cprPair)) {
                containsInvalidCpr = true;
            }
        }
        if (containsInvalidCpr) {
            log.error("Dump/restore blev ikke gennemført!");
            return false;
        }
        return true;
    }

    private boolean invokeForCprPairAndService(DumpRestoreDomain source, DumpRestoreDomain target, CprPair cprPair, String service) {
        try {
            log.info("Kalder dump for '" + cprPair.getSourceCpr() + "' for service '" + service + "'");
            String dump = source.dump(service, cprPair.getSourceCpr());
            log.info("Kalder restore til '" + cprPair.getTargetCpr() + "' for service '" + service + "'");
            target.restore(service, cprPair.getTargetCpr(), dump);
            return false;
        } catch (NonFatalException e) {
            log.error(e.getMessage());
            return true;
        }
    }

    private void logErrors(int numberOfErrors) {
        if (numberOfErrors == 0) {
            log.info("Dump/restore afsluttet - alt gik godt");
        } else {
            log.warn("Dump/restore afsluttet - der var " + numberOfErrors + " fejl");
        }
    }

}
