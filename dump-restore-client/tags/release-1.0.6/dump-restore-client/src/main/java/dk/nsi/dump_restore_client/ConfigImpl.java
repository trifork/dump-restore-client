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
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.6/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/ConfigImpl.java $
 *  $Id: ConfigImpl.java 34023 2017-03-01 15:18:49Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2017-03-01 16:18:49 +0100 (Wed, 01 Mar 2017) $
 * @version $Revision: 34023 $
 */
public class ConfigImpl implements Config {

    private static final String SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    private static final String SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    private static final String SCHEMA_FILE = "/dump-restore-config.xsd";

    private static Logger log = Logger.getLogger(ConfigImpl.class);

    private List<String> environments;
    private List<String> services;
    private List<String> selectedServices;
    private List<String> cprWhitelist;

    private Map<String, Map<String, URL>> environmentToServiceToEndpoint;
    private static final ErrorHandler ERROR_HANDLER = new ErrorHandler() {
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }

        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    };

    public static ConfigImpl loadFromStream(InputStream inputStream) {
        return new ConfigImpl(inputStream);
    }

    private ConfigImpl(InputStream inputStream) {
        Document doc = parseConfiguration(inputStream);
        loadEnvironments(doc);
        loadServices(doc);
        validateEndpoints();
        loadCPRWhitelist(doc);
        validatCPRWhitelist();
    }

    public List<String> getEnvironments() {
        return environments;
    }

    public List<String> getServices() {
        return services;
    }

    public List<String> getSelectedServices() {
        return selectedServices;
    }

    public void addServiceToSelected(String service) {
        selectedServices.add(service);
    }

    public void removeServiceFromSelected(String service) {
        selectedServices.remove(service);
    }

    public URL getEndpoint(String environment, String service) {
        URL endpoint = getEndpointInternal(environment, service);
        if (endpoint == null) {
            throw new ConfigException("Unknown service: '" + service + "'");
        }
        return endpoint;
    }

    private URL getEndpointInternal(String environment, String service) {
        Map<String, URL> serviceToEndpoints = environmentToServiceToEndpoint.get(environment);
        if (serviceToEndpoints == null) {
            throw new ConfigException("Unknown environment: '" + environment + "'");
        }
        return serviceToEndpoints.get(service);
    }

    public List<String> getWhitelistedCPRs() {
        return cprWhitelist;
    }

    private Document parseConfiguration(InputStream inputStream) {
        InputStream schemaStream = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setAttribute(SCHEMA_LANGUAGE, XML_SCHEMA);
            schemaStream = ConfigImpl.class.getResourceAsStream(SCHEMA_FILE);
            docBuilderFactory.setAttribute(SCHEMA_SOURCE, schemaStream);
            docBuilderFactory.setValidating(true);

            DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
            builder.setErrorHandler(ERROR_HANDLER);
            return builder.parse(inputStream);
        } catch (ParserConfigurationException e) {
            log.error("Error parsing static configuration", e);
            throw new ConfigException("Error parsing static configuration", e);
        } catch (SAXException e) {
            log.error("Error parsing static configuration", e);
            throw new ConfigException("Error parsing static configuration", e);
        } catch (IOException e) {
            log.error("Error parsing static configuration", e);
            throw new ConfigException("Error parsing static configuration", e);
        } finally {
            closeStream(inputStream);
            closeStream(schemaStream);
        }
    }

    private void loadEnvironments(Document doc) {
        Element environmentsNode = (Element) doc.getElementsByTagName("environments").item(0);
        NodeList environmentNodes = environmentsNode.getElementsByTagName("environment");
        environments = new ArrayList<String>(environmentNodes.getLength());
        environmentToServiceToEndpoint = new HashMap<String, Map<String, URL>>();
        for (int i = 0; i < environmentNodes.getLength(); i++) {
            String environmentName = environmentNodes.item(i).getTextContent();
            environments.add(environmentName);
            environmentToServiceToEndpoint.put(environmentName, new HashMap<String, URL>());
        }
    }

    private void loadServices(Document doc) {
        Element servicesNode = (Element) doc.getElementsByTagName("services").item(0);
        NodeList serviceNodes = servicesNode.getElementsByTagName("service");
        services = new ArrayList<String>(serviceNodes.getLength());
        // Initialize selected Services
        selectedServices = new ArrayList<String>(serviceNodes.getLength());
        for (int i = 0; i < serviceNodes.getLength(); i++) {
            Element serviceElement = (Element) serviceNodes.item(i);
            String service = serviceElement.getAttribute("name");
            services.add(service);
            NodeList endpointNodes = serviceElement.getElementsByTagName("endpoint");
            for (int j = 0; j < endpointNodes.getLength(); j++) {
                Element endpointElement = (Element) endpointNodes.item(j);
                String environment = endpointElement.getAttribute("environment");
                Map<String, URL> serviceToEndpoint = environmentToServiceToEndpoint.get(environment);
                if (serviceToEndpoint == null) {
                    throw new ConfigException("Undefined environment: '" + environment + "' in service definition for service '" + service + "'");
                }
                try {
                    URL endpoint = new URL(endpointElement.getTextContent());
                    serviceToEndpoint.put(service, endpoint);
                } catch (MalformedURLException e) {
                    throw new ConfigException("Cannot parse endpoint '" + endpointElement.getTextContent() + "' in service definition for service '" + service + "' as an URL");
                }
            }
        }
    }

    private void validateEndpoints() {
        for (String service : services) {
            for (String environment : environments) {
                if (getEndpointInternal(environment, service) == null) {
                    throw new ConfigException("Missing endpoint definition for environment '" + environment + "' in service definition for service '" + service + "'");
                }
            }
        }
    }

    private void loadCPRWhitelist(Document doc) {
        String cprWhitelistString = doc.getElementsByTagName("cpr-whitelist").item(0).getTextContent();
        cprWhitelist = Arrays.asList(cprWhitelistString.trim().split("\\s+"));
    }

    private void validatCPRWhitelist() {
        for (String cpr : cprWhitelist) {
            if (cpr.length() != 10) {
                throw new ConfigException("CPR '" + cpr + "' in cpr-whitelist is not 10 characters long");
            }
        }
    }

    private void closeStream(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }


}
