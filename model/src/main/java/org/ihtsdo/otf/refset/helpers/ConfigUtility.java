/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Loads and serves configuration.
 */
public class ConfigUtility {

  /** The Constant DEFAULT. */
  public final static String DEFAULT = "DEFAULT";

  /** The date format. */
  public final static FastDateFormat DATE_FORMAT = FastDateFormat
      .getInstance("yyyyMMdd");

  /** The Constant DATE_FORMAT2. */
  public final static FastDateFormat DATE_FORMAT2 = FastDateFormat
      .getInstance("yyyy_MM_dd");

  /** The Constant DATE_FORMAT3. */
  public final static FastDateFormat DATE_FORMAT3 = FastDateFormat
      .getInstance("yyyy");

  /** The config. */
  public static Properties config = null;

  /** The transformer for DOM -> XML. */
  private static Transformer transformer;

  /** The date format. */
  public final static FastDateFormat format = FastDateFormat
      .getInstance("yyyyMMdd");

  static {
    try {
      TransformerFactory factory = TransformerFactory.newInstance();
      transformer = factory.newTransformer();
      // Indent output.
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(
          "{http://xml.apache.org/xslt}indent-amount", "4");
      // Skip XML declaration header.
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    } catch (TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Indicates whether or not the server is active.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  public static boolean isServerActive() throws Exception {
    if (config == null)
      config = ConfigUtility.getConfigProperties();

    try {
      // Attempt to logout to verify service is up (this works like a "ping").
      Client client = ClientBuilder.newClient();
      WebTarget target =
          client.target(config.getProperty("base.url")
              + "/security/logout/dummy");

      Response response = target.request(MediaType.APPLICATION_JSON).get();
      if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Returns the config properties.
   * @return the config properties
   *
   * @throws Exception the exception
   */
  public static Properties getConfigProperties() throws Exception {
    if (isNull(config)) {
      // Need to determine the label (default "")
      String label = "";
      Properties labelProp = new Properties();

      // If no resource is available, go with the default
      // ONLY setups that explicitly intend to override the setting
      // cause it to be something other than the default.
      InputStream input =
          ConfigUtility.class.getResourceAsStream("/label.prop");
      if (input != null) {
        labelProp.load(input);
        // If a refset.config.label override can be found, use it
        String candidateLabel = labelProp.getProperty("refset.config.label");
        // If the default, uninterpolated value is used, stick again with the
        // default
        if (candidateLabel != null
            && !candidateLabel.equals("${refset.config.label}")) {
          label = candidateLabel;
        }
      } else {
        Logger.getLogger(ConfigUtility.class.getName()).info(
            "  label.prop resource cannot be found, using default");

      }
      Logger.getLogger(ConfigUtility.class.getName()).info(
          "  refset.config.label = " + label);

      // Now get the properties from the corresponding setting
      // This is a complicated mechanism to support multiple simulataneous
      // installations within the same container (e.g. tomcat).
      // Default setups do not require this.
      String configFileName =
          System.getProperty("refset.config"
              + (label.isEmpty() ? "" : "." + label));
      Logger.getLogger(ConfigUtility.class.getName()).info(
          "  refset.config" + (label.isEmpty() ? "" : "." + label) + " = "
              + configFileName);
      config = new Properties();
      FileReader in = new FileReader(new File(configFileName));
      config.load(in);
      in.close();
      Logger.getLogger(ConfigUtility.class).info("  properties = " + config);
    }
    return config;
  }

  /**
   * New handler instance.
   *
   * @param <T> the
   * @param handler the handler
   * @param handlerClass the handler class
   * @param type the type
   * @return the object
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public static <T> T newHandlerInstance(String handler, String handlerClass,
    Class<T> type) throws Exception {
    if (handlerClass == null) {
      throw new Exception("Handler class " + handlerClass + " is not defined");
    }
    Class<?> toInstantiate = Class.forName(handlerClass);
    if (toInstantiate == null) {
      throw new Exception("Unable to find class " + handlerClass);
    }
    Object o = null;
    try {
      o = toInstantiate.newInstance();
    } catch (Exception e) {
      // do nothing
    }
    if (o == null) {
      throw new Exception("Unable to instantiate class " + handlerClass
          + ", check for default constructor.");
    }
    if (type.isAssignableFrom(o.getClass())) {
      return (T) o;
    }
    throw new Exception("Handler is not assignable from " + type.getName());
  }

  /**
   * Instantiates a handler using standard setup and configures it with
   * properties.
   *
   * @param <T> the
   * @param property the property
   * @param handlerName the handler name
   * @param type the type
   * @return the t
   * @throws Exception the exception
   */
  public static <T extends Configurable> T newStandardHandlerInstanceWithConfiguration(
    String property, String handlerName, Class<T> type) throws Exception {

    // Instantiate the handler
    // property = "metadata.service.handler" (e.g)
    // handlerName = "SNOMED" (e.g.)
    String classKey = property + "." + handlerName + ".class";
    if (config.getProperty(classKey) == null) {
      throw new Exception("Unexpected null classkey " + classKey);
    }
    String handlerClass = config.getProperty(classKey);
    Logger.getLogger(ConfigUtility.class).info("Instantiate " + handlerClass);
    T handler =
        ConfigUtility.newHandlerInstance(handlerName, handlerClass, type);

    // Look up and build properties
    Properties handlerProperties = new Properties();
    handlerProperties.setProperty("security.handler", handlerName);

    for (Object key : config.keySet()) {
      // Find properties like "metadata.service.handler.SNOMED.class"
      if (key.toString().startsWith(property + "." + handlerName + ".")) {
        String shortKey =
            key.toString().substring(
                (property + "." + handlerName + ".").length());
        Logger.getLogger(ConfigUtility.class).info(
            " property " + shortKey + " = "
                + config.getProperty(key.toString()));
        handlerProperties.put(shortKey, config.getProperty(key.toString()));
      }
    }
    handler.setProperties(handlerProperties);
    return handler;
  }

  /**
   * Returns the graph for string.
   *
   * @param xml the xml
   * @param graphClass the graph class
   * @return the graph for string
   * @throws JAXBException the JAXB exception
   */
  public static Object getGraphForString(String xml, Class<?> graphClass)
    throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(graphClass);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    return unmarshaller.unmarshal(new StreamSource(new StringReader(xml)));
  }

  /**
   * Returns the graph for json.
   *
   * @param json the json
   * @param graphClass the graph class
   * @return the graph for json
   * @throws Exception the exception
   */
  public static Object getGraphForJson(String json, Class<?> graphClass)
    throws Exception {
    InputStream in =
        new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector =
        new JaxbAnnotationIntrospector(mapper.getTypeFactory());
    mapper.setAnnotationIntrospector(introspector);
    return mapper.readValue(in, graphClass);

  }

  /**
   * Returns the graph for file.
   *
   * @param file the file
   * @param graphClass the graph class
   * @return the graph for file
   * @throws FileNotFoundException the file not found exception
   * @throws JAXBException the JAXB exception
   */
  @SuppressWarnings("resource")
  public static Object getGraphForFile(File file, Class<?> graphClass)
    throws FileNotFoundException, JAXBException {
    return getGraphForString(new Scanner(file, "UTF-8").useDelimiter("\\A")
        .next(), graphClass);
  }

  /**
   * Returns the graph for stream.
   *
   * @param in the in
   * @param graphClass the graph class
   * @return the graph for stream
   * @throws FileNotFoundException the file not found exception
   * @throws JAXBException the JAXB exception
   */
  @SuppressWarnings("resource")
  public static Object getGraphForStream(InputStream in, Class<?> graphClass)
    throws FileNotFoundException, JAXBException {
    return getGraphForString(new Scanner(in, "UTF-8").useDelimiter("\\A")
        .next(), graphClass);
  }

  /**
   * Returns the XML string for for graph object.
   *
   * @param object the object
   * @return the string for for graph
   * @throws JAXBException the JAXB exception
   */
  public static String getStringForGraph(Object object) throws JAXBException {
    StringWriter writer = new StringWriter();
    JAXBContext jaxbContext = null;
    jaxbContext = JAXBContext.newInstance(object.getClass());
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.marshal(object, writer);
    return writer.toString();
  }

  /**
   * Returns the json for graph.
   *
   * @param object the object
   * @return the json for graph
   * @throws Exception the exception
   */
  public static String getJsonForGraph(Object object) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector =
        new JaxbAnnotationIntrospector(mapper.getTypeFactory());
    mapper.setAnnotationIntrospector(introspector);
    return mapper.writeValueAsString(object);
  }

  /**
   * Returns the node for string.
   *
   * @param xml the xml
   * @return the node for string
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static Node getNodeForString(String xml)
    throws ParserConfigurationException, SAXException, IOException {

    InputStream in =
        new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    // Parse XML file.
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document document = db.parse(in);
    Node rootNode = document.getFirstChild();
    return rootNode;
  }

  /**
   * Returns the node for file.
   *
   * @param file the file
   * @return the node for file
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static Node getNodeForFile(File file)
    throws ParserConfigurationException, SAXException, IOException {
    InputStream in = new FileInputStream(file);
    // Parse XML file.
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document document = db.parse(in);
    Node rootNode = document.getFirstChild();
    in.close();
    return rootNode;
  }

  /**
   * Returns the string for node.
   *
   * @param root the root node
   * @return the string for node
   * @throws TransformerException the transformer exception
   * @throws ParserConfigurationException the parser configuration exception
   */
  public static String getStringForNode(Node root) throws TransformerException,
    ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.newDocument();
    document.appendChild(document.importNode(root, true));
    DOMSource source = new DOMSource(document);
    StringWriter out = new StringWriter();
    StreamResult result = new StreamResult(out);
    transformer.transform(source, result);
    return out.toString();
  }

  /**
   * Returns the graph for node.
   *
   * @param node the node
   * @param graphClass the graph class
   * @return the graph for node
   * @throws JAXBException the JAXB exception
   * @throws TransformerException the transformer exception
   * @throws ParserConfigurationException the parser configuration exception
   */
  public static Object getGraphForNode(Node node, Class<?> graphClass)
    throws JAXBException, TransformerException, ParserConfigurationException {
    return getGraphForString(getStringForNode(node), graphClass);
  }

  /**
   * Returns the node for graph.
   *
   * @param object the object
   * @return the node for graph
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws JAXBException the JAXB exception
   */
  public static Node getNodeForGraph(Object object)
    throws ParserConfigurationException, SAXException, IOException,
    JAXBException {
    return getNodeForString(getStringForGraph(object));
  }

  /**
   * Pretty format.
   *
   * @param input the input
   * @param indent the indent
   * @return the string
   */
  public static String prettyFormat(String input, int indent) {
    try {
      Source xmlInput = new StreamSource(new StringReader(input));
      StringWriter stringWriter = new StringWriter();
      StreamResult xmlOutput = new StreamResult(stringWriter);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute("indent-number", indent);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(xmlInput, xmlOutput);
      return xmlOutput.getWriter().toString();
    } catch (Exception e) {
      // simple exception handling, please review it
      throw new RuntimeException(e);
    }
  }

  /**
   * Merge-sort two files.
   * 
   * @param files1 the first set of files
   * @param files2 the second set of files
   * @param comp the comparator
   * @param dir the sort dir
   * @param headerLine the header_line
   * @return the sorted {@link File}
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static File mergeSortedFiles(File files1, File files2,
    Comparator<String> comp, File dir, String headerLine) throws IOException {

    final BufferedReader in1 = new BufferedReader(new FileReader(files1));
    final BufferedReader in2 = new BufferedReader(new FileReader(files2));
    final File outFile = File.createTempFile("t+~", ".tmp", dir);
    final BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
    String line1 = in1.readLine();
    String line2 = in2.readLine();
    String line = null;
    if (!headerLine.isEmpty()) {
      line = headerLine;
      out.write(line);
      out.newLine();
    }
    while (line1 != null || line2 != null) {
      if (line1 == null) {
        line = line2;
        line2 = in2.readLine();
      } else if (line2 == null) {
        line = line1;
        line1 = in1.readLine();
      } else if (comp.compare(line1, line2) < 0) {
        line = line1;
        line1 = in1.readLine();
      } else {
        line = line2;
        line2 = in2.readLine();
      }
      // if a header line, do not write
      if (!line.startsWith("id")) {
        out.write(line);
        out.newLine();
      }
    }
    out.flush();
    out.close();
    in1.close();
    in2.close();
    return outFile;
  }

  /**
   * Delete directory.
   *
   * @param path the path
   * @return true, if successful
   */
  static public boolean deleteDirectory(File path) {
    if (path.exists()) {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deleteDirectory(files[i]);
        } else {
          files[i].delete();
        }
      }
    }
    return (path.delete());
  }

  /**
   * Sends email.
   *
   * @param subject the subject
   * @param from the from
   * @param recipients the recipients
   * @param body the body
   * @param details the details
   * @param authFlag the auth flag
   * @throws Exception the exception
   */
  public static void sendEmail(String subject, String from, String recipients,
    String body, Properties details, boolean authFlag) throws Exception {
    // avoid sending mail if disabled
    if ("false".equals(details.getProperty("mail.enabled"))) {
      // do nothing
      return;
    }
    Session session = null;
    if (authFlag) {
      Authenticator auth = new SMTPAuthenticator();
      session = Session.getInstance(details, auth);
    } else {
      session = Session.getInstance(details);
    }

    MimeMessage msg = new MimeMessage(session);
    if (body.contains("<html")) {
      msg.setContent(body.toString(), "text/html; charset=utf-8");
    } else {
      msg.setText(body.toString());
    }
    msg.setSubject(subject);
    msg.setFrom(new InternetAddress(from));
    String[] recipientsArray = recipients.split(";");
    for (String recipient : recipientsArray) {
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
    }
    Transport.send(msg);
  }

  /**
   * SMTPAuthenticator.
   */
  public static class SMTPAuthenticator extends javax.mail.Authenticator {

    /**
     * Instantiates an empty {@link SMTPAuthenticator}.
     */
    public SMTPAuthenticator() {
      // do nothing
    }

    /* see superclass */
    @Override
    public PasswordAuthentication getPasswordAuthentication() {
      Properties config = null;
      try {
        config = ConfigUtility.getConfigProperties();
      } catch (Exception e) {
        // do nothing
      }
      if (config == null) {
        return null;
      } else {
        return new PasswordAuthentication(config.getProperty("mail.smtp.user"),
            config.getProperty("mail.smtp.password"));
      }
    }
  }

  /**
   * Reflection sort.
   *
   * @param <T> the
   * @param classes the classes
   * @param clazz the clazz
   * @param sortField the sort field
   * @throws Exception the exception
   */
  public static <T> void reflectionSort(List<T> classes, Class<T> clazz,
    String sortField) throws Exception {

    final Method getMethod =
        clazz.getMethod("get" + sortField.substring(0, 1).toUpperCase()
            + sortField.substring(1));
    if (getMethod.getReturnType().isAssignableFrom(Comparable.class)) {
      throw new Exception("Referenced sort field is not comparable");
    }
    Collections.sort(classes, new Comparator<T>() {
      @SuppressWarnings({
          "rawtypes", "unchecked"
      })
      @Override
      public int compare(T o1, T o2) {
        try {
          Comparable f1 = (Comparable) getMethod.invoke(o1, new Object[] {});
          Comparable f2 = (Comparable) getMethod.invoke(o2, new Object[] {});
          return f1.compareTo(f2);
        } catch (Exception e) {
          // do nothing
        }
        return 0;
      }
    });
  }

  /**
   * To arabic.
   *
   * @param number the number
   * @return the int
   * @throws Exception the exception
   */
  public static int toArabic(String number) throws Exception {
    if (number.isEmpty())
      return 0;
    if (number.startsWith("M"))
      return 1000 + toArabic(number.substring(1));
    if (number.startsWith("CM"))
      return 900 + toArabic(number.substring(2));
    if (number.startsWith("D"))
      return 500 + toArabic(number.substring(1));
    if (number.startsWith("CD"))
      return 400 + toArabic(number.substring(2));
    if (number.startsWith("C"))
      return 100 + toArabic(number.substring(1));
    if (number.startsWith("XC"))
      return 90 + toArabic(number.substring(2));
    if (number.startsWith("L"))
      return 50 + toArabic(number.substring(1));
    if (number.startsWith("XL"))
      return 40 + toArabic(number.substring(2));
    if (number.startsWith("X"))
      return 10 + toArabic(number.substring(1));
    if (number.startsWith("IX"))
      return 9 + toArabic(number.substring(2));
    if (number.startsWith("V"))
      return 5 + toArabic(number.substring(1));
    if (number.startsWith("IV"))
      return 4 + toArabic(number.substring(2));
    if (number.startsWith("I"))
      return 1 + toArabic(number.substring(1));
    throw new Exception("something bad happened");
  }

  /**
   * Indicates whether or not roman numeral is the case.
   *
   * @param number the number
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isRomanNumeral(String number) {
    return number
        .matches("^M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$");
  }

  /**
   * Returns the indent for level.
   *
   * @param level the level
   * @return the indent for level
   */
  public static String getIndentForLevel(int level) {

    StringBuilder sb = new StringBuilder().append("  ");
    for (int i = 0; i < level; i++) {
      sb.append("  ");
    }
    return sb.toString();
  }

  /**
   * Helper method to avoid a false-positive for a static code analysis issue.
   * O*
   * @param o the o
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isNull(Object o) {
    return o == null;
  }

  /**
   * Capitalize.
   *
   * @param value the value
   * @return the string
   */
  public static String capitalize(String value) {
    if (value == null) {
      return value;
    }
    return value.substring(0, 1).toUpperCase() + value.substring(1);
  }

  /**
   * Indicates whether or not background lookup is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  public static boolean isBackgroundLookup() throws Exception {
    String property = getConfigProperties().getProperty("lookup.background");
    boolean background = true;
    if (property != null && property.equals("false")) {
      background = false;
    }
    return background;
  }
}
