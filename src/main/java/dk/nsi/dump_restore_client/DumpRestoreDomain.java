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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/trunk/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/DumpRestoreDomain.java $
 *  $Id: DumpRestoreDomain.java 34625 2017-12-13 16:09:52Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2017-12-13 17:09:52 +0100 (Wed, 13 Dec 2017) $
 * @version $Revision: 34625 $
 */
public interface DumpRestoreDomain {

    public String dump(String service, String cpr);

    public void restore(String service, String cpr, String base64Data, String date);

    public void reset(String service, String cpr);

    public String toPrettyString();

    public boolean checkCpr(String cpr, String cprType);

}
