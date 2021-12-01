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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.1/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/SoapDumpRestore.java $
 *  $Id: SoapDumpRestore.java 10985 2013-04-02 20:22:06Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.soap.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2013-04-02 22:22:06 +0200 (Tue, 02 Apr 2013) $
 * @version $Revision: 10985 $
 */
public class SoapDumpRestore implements DumpRestoreDomain {

    private static final Logger log = Logger.getLogger(SoapDumpRestore.class);

    private static final String DUMP_SOAP_ACTION = "http://www.ssi.dk/nsi/xml.schema/2013/01/01#DumpPatients";
    private static final String RESTORE_SOAP_ACTION = "http://www.ssi.dk/nsi/xml.schema/2013/01/01#RestorePatients";
    private static final String NS_SSI2013 = "http://www.ssi.dk/nsi/xml.schema/2013/01/01";

    private static final String DUMP_REQUEST_FRAGMENT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "  <soapenv:Body>\n" +
            "    <ssi2013:DumpPatientsRequest xmlns:ssi2013=\"http://www.ssi.dk/nsi/xml.schema/2013/01/01\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "      <ssi2013:Identifier>\n" +
            "        <ssi2013:PersonIdentifier>%s</ssi2013:PersonIdentifier>\n" +
            "      </ssi2013:Identifier>\n" +
            "    </ssi2013:DumpPatientsRequest>\n" +
            "  </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    private static final String RESTORE_REQUEST_FRAGMENT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "  <soapenv:Body>\n" +
            "    <ssi2013:RestorePatientsRequest xmlns:ssi2013=\"http://www.ssi.dk/nsi/xml.schema/2013/01/01\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "      <ssi2013:Dump>\n" +
            "        <ssi2013:Identifier>\n" +
            "          <ssi2013:PersonIdentifier>%s</ssi2013:PersonIdentifier>\n" +
            "        </ssi2013:Identifier>\n" +
            "        <ssi2013:DumpData>%s</ssi2013:DumpData>\n" +
            "      </ssi2013:Dump>\n" +
            "    </ssi2013:RestorePatientsRequest>\n" +
            "  </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    private final StaticConfig staticConfig;
    private final String environment;

    public SoapDumpRestore(StaticConfig staticConfig, String environment) {
        this.staticConfig = staticConfig;
        this.environment = environment;
    }

    public String dump(String service, String cpr) {
        URL endpoint = staticConfig.getEndpoint(environment, service);
        String response = callService(endpoint, buildDumpRequest(cpr), DUMP_SOAP_ACTION);
        return parseAndValidateDumpResponse(service, response);
    }

    public void restore(String service, String cpr, String base64Data) {
        URL endpoint = staticConfig.getEndpoint(environment, service);
        String response = callService(endpoint, buildRestoreRequest(cpr, base64Data), RESTORE_SOAP_ACTION);
        parseAndValidateRestoreResponse(service, response);
    }

    public String toPrettyString() {
        return environment;
    }

    public boolean validateCprPair(CprPair cprPair) {
        String targetCpr = cprPair.getTargetCpr();
        if (!staticConfig.getWhitelistedCPRs().contains(targetCpr)) {
            log.error("Mål-cpr '" + targetCpr + "' optræder ikke i positivlisten!");
            return false;
        } else {
            return true;
        }
    }

    private String callService(URL url, String soapEnvelope, String soapAction) {
        try {
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            httpConn.setRequestProperty("Content-Length", String.valueOf(soapEnvelope.getBytes().length));
            httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            httpConn.setRequestProperty("SOAPAction", soapAction);
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);

            OutputStream out = httpConn.getOutputStream();
            out.write(soapEnvelope.getBytes("UTF-8"));
            out.close();

            int responseCode = httpConn.getResponseCode();

            InputStream inputStream = (responseCode >= 500) ? httpConn.getErrorStream() : httpConn.getInputStream();
            return consumeStream(inputStream);
        } catch (IOException e) {
            throw new FatalException("Fejl ved kald til " + url, e);
        }
    }

    String buildDumpRequest(String cpr) {
        return String.format(DUMP_REQUEST_FRAGMENT, cpr);
    }

    String parseAndValidateDumpResponse(String service, String response) {
        SOAPBody body = parseResponse(response);
        if (body.hasFault()) {
            throw new NonFatalException("Fejlsvar fra dump-kald til " + service + ": " + body.getFault().getTextContent());
        }
        Element dumpPatientsResponse = (Element) body.getElementsByTagNameNS(NS_SSI2013, "DumpPatientsResponse").item(0);
        Element dump = (Element) dumpPatientsResponse.getElementsByTagNameNS(NS_SSI2013, "Dump").item(0);
        Node dumpData = dump.getElementsByTagNameNS(NS_SSI2013, "DumpData").item(0);

        return dumpData.getTextContent();
    }

    String buildRestoreRequest(String cpr, String base64Data) {
        return String.format(RESTORE_REQUEST_FRAGMENT, cpr, base64Data);
    }

    void parseAndValidateRestoreResponse(String service, String response) {
        SOAPBody body = parseResponse(response);
        if (body.hasFault()) {
            throw new NonFatalException("Fejlsvar fra restore-kald til " + service + ": " + body.getFault().getTextContent());
        }
    }

    private SOAPBody parseResponse(String response) {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage message = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(response.getBytes(Charset.forName("UTF-8"))));
            message.saveChanges();
            return message.getSOAPBody();
        } catch (SOAPException e) {
            throw new NonFatalException("Could not parse response: '" + response + "'", e);
        } catch (IOException e) {
            throw new FatalException(e.getMessage(), e);
        }
    }

    private String consumeStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = is.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toString("UTF-8");
    }


}
