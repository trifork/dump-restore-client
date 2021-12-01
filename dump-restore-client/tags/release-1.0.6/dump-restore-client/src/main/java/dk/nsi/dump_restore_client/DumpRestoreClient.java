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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.6/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/DumpRestoreClient.java $
 *  $Id: DumpRestoreClient.java 34023 2017-03-01 15:18:49Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2017-03-01 16:18:49 +0100 (Wed, 01 Mar 2017) $
 * @version $Revision: 34023 $
 */
public class DumpRestoreClient {

    private static Logger log = Logger.getLogger(DumpRestoreClient.class);

    private static class EventQueueProxy extends EventQueue {

        protected void dispatchEvent(AWTEvent event) {
            try {
                super.dispatchEvent(event);
            } catch (Throwable t) {
                ExceptionHandler.handleThrowable(t);
            }
        }
    }


    public static void main(String[] args) {

        PropertyConfigurator.configure(DumpRestoreClient.class.getResource("/log4j-dump-restore-client.properties"));
        log.info("Starting dump/restore client ....");

        EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        queue.push(new EventQueueProxy());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Config staticConfig = ConfigImpl.loadFromStream(openConfiguration());
                Controller controller = new Controller(staticConfig);
                Presentation presentation = new Presentation(controller, staticConfig);
                presentation.createAndShowGUI();
            }
        });
    }

    private static InputStream openConfiguration() {
        String path = "config/dump-restore-config.xml";
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Kunne ikke finde konfigurationsfilen " + path, e);
        }
    }

}
