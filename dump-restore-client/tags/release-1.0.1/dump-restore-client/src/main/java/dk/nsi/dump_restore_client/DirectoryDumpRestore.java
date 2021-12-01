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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.1/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/DirectoryDumpRestore.java $
 *  $Id: DirectoryDumpRestore.java 10985 2013-04-02 20:22:06Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2013-04-02 22:22:06 +0200 (Tue, 02 Apr 2013) $
 * @version $Revision: 10985 $
 */
public class DirectoryDumpRestore implements DumpRestoreDomain{

    private static final Logger log = Logger.getLogger(DirectoryDumpRestore.class);

    private final File directory;

    public DirectoryDumpRestore(File directory) {
        this.directory = directory;
    }

    public String dump(String service, String cpr) {
        File file = getFile(service, cpr);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String dump = reader.readLine();
            reader.close();
            return dump;
        } catch (FileNotFoundException e) {
            throw new NonFatalException("Ingen dump fil fundet for CPR '" + cpr + "' og service '" + service + "'! Forventede at finde filen " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new NonFatalException("Kunne ikke læse fra filen: " + file.getAbsolutePath(), e);
        }
    }

    public void restore(String service, String cpr, String base64Data) {
        File file = getFile(service, cpr);
        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.append(base64Data);
            writer.close();
        } catch (IOException e) {
            throw new NonFatalException("Kunne ikke skrive til filen: " + file.getAbsolutePath(), e);
        }

    }

    public String toPrettyString() {
        return directory.getAbsolutePath();
    }

    public boolean validateCprPair(CprPair cprPair) {
        return true;
    }

    private File getFile(String service, String cpr) {
        return new File(directory, cpr + "-" + service + ".dump");
    }
}
