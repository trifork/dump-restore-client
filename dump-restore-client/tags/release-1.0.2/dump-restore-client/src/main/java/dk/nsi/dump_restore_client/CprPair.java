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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.2/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/CprPair.java $
 *  $Id: CprPair.java 21690 2015-11-13 13:43:28Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2015-11-13 14:43:28 +0100 (Fri, 13 Nov 2015) $
 * @version $Revision: 21690 $
 */
public class CprPair {

    private static final Logger log = Logger.getLogger(CprPair.class);

    private final String sourceCpr;
    private final String targetCpr;

    public CprPair(String sourceCpr, String targetCpr) {
        this.sourceCpr = sourceCpr;
        this.targetCpr = targetCpr;
    }

    public String getSourceCpr() {
        return sourceCpr;
    }

    public String getTargetCpr() {
        return targetCpr;
    }

    public static List<String> listCprSourceFromDoubleArray(Object[][] array) {
        List<String> sourceCprs = new LinkedList<String>();
        if (array != null) {
            for (Object[] pair : array){
                String sourceCpr = ((String) pair[0]).trim();
                 if (CprValidator.validateNonEmpty(sourceCpr))
                    sourceCprs.add(sourceCpr);
            }
        }
       return sourceCprs;
    }

    public static List<CprPair> listFromDoubleArray(Object[][] array) {
        List<CprPair> cprPairs = new LinkedList<CprPair>();
        if (array != null) {
            for (Object[] pair : array) {
                validateAndAddToList(pair, cprPairs);
            }
        }
        return cprPairs;
    }

    private static void validateAndAddToList(Object[] pair, List<CprPair> cprPairs) {
        String sourceCpr = ((String) pair[0]).trim();
        String targetCpr = ((String) pair[1]).trim();
        if (CprValidator.validateNonEmpty(sourceCpr) && CprValidator.validateNonEmpty(targetCpr)) {
            cprPairs.add(new CprPair(sourceCpr, targetCpr));
        }
    }

    public static List<CprPair> listFromFile(File file) {
        try {
            LinkedList<CprPair> cprPairs = new LinkedList<CprPair>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] split = line.trim().split("\\s+");
                if (split.length != 2) {
                    log.warn("Linien '" + line + "' indeholder ikke to cpr numre og bliver ignoreret!");
                } else {
                    validateAndAddToList(split, cprPairs);
                }
            }
            return cprPairs;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static Object[][] toDoubleArray(List<CprPair> list) {
        if (list == null) return null;
        Object[][] objects = new Object[list.size()][2];
        for (int i = 0; i < list.size(); i++) {
            CprPair pair = list.get(i);
            objects[i] = new String[] {pair.getSourceCpr(), pair.getTargetCpr()};
        }
        return objects;
    }

}
