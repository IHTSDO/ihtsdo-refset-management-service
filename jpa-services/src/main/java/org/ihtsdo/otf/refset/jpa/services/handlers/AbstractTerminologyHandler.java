/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Abstract implementation of {@link TerminologyHandler} for default google
 * translate support.
 */
public abstract class AbstractTerminologyHandler implements TerminologyHandler {

  /** The api key. */
  private String apiKey;

  /**
   * Instantiates an empty {@link AbstractTerminologyHandler}.
   *
   * @throws Exception the exception
   */
  public AbstractTerminologyHandler() throws Exception {
    super();
  }

  /**
   * This default implementation uses Google translate.
   *
   * @param text the text
   * @param language the language
   * @return the string
   * @throws Exception the exception
   */
  @Override
  public String translate(String text, String language) throws Exception {
    GoogleTranslate.setHttpReferrer(
        ConfigUtility.getConfigProperties().getProperty("base.url"));
    GoogleTranslate.setKey("AIzaSyBGKPvbj4JSB1zftiOcai5azkPug6SV_o4");
    return GoogleTranslate.execute(text, "en", language);
  }

  /**
   * Class for translating via google translate.
   */
  static final class GoogleTranslate {

    /** The referrer. */
    private static String referrer;

    /** The key. */
    private static String key;

    /**
     * Sets the HTTP Referrer.
     * @param pReferrer The HTTP referrer parameter.
     */
    public static void setHttpReferrer(final String pReferrer) {
      referrer = pReferrer;
    }

    /**
     * Sets the API key.
     *
     * @param pKey the key
     */
    public static void setKey(final String pKey) {
      key = pKey;
    }

    /**
     * Validate referrer.
     *
     * @throws Exception the exception
     */
    public static void validateReferrer() throws Exception {
      if (referrer == null || referrer.length() == 0) {
        throw new Exception("Referrer is not set.");
      }
    }

    /**
     * Forms an HTTP request, sends it using POST method and returns the result
     * of the request as a JSONObject.
     * 
     * @param url The URL to query for a JSONObject.
     * @param parameters Additional POST parameters
     * @return The translated String.
     * @throws Exception on error.
     */
    protected static JSONObject retrieveJSON(final URL url,
      final String parameters) throws Exception {
      try {
        final HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        uc.setRequestProperty("referer", referrer);
        uc.setRequestMethod("POST");
        uc.setDoOutput(true);
        uc.setRequestProperty("X-HTTP-Method-Override", "GET");

        final PrintWriter pw = new PrintWriter(uc.getOutputStream());
        pw.write(parameters);
        pw.close();
        uc.getOutputStream().close();

        try {
          final String result = inputStreamToString(uc.getInputStream());

          return new JSONObject(result);
        } finally {
          // http://java.sun.com/j2se/1.5.0/docs/guide/net/http-keepalive.html
          if (uc.getInputStream() != null) {
            uc.getInputStream().close();
          }
          if (uc.getErrorStream() != null) {
            uc.getErrorStream().close();
          }
          if (pw != null) {
            pw.close();
          }
        }
      } catch (Exception ex) {
        throw new Exception("Error retrieving translation.", ex);
      }
    }

    /**
     * Reads an InputStream and returns its contents as a String. Also effects
     * rate control.
     * @param inputStream The InputStream to read from.
     * @return The contents of the InputStream as a String.
     * @throws Exception on error.
     */
    private static String inputStreamToString(final InputStream inputStream)
      throws Exception {
      final StringBuilder outputBuilder = new StringBuilder();

      String string;
      if (inputStream != null) {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while (null != (string = reader.readLine())) {
          outputBuilder.append(string).append('\n');
        }
      }

      return outputBuilder.toString();
    }

    /**
     * Execute.
     *
     * @param text the text
     * @param fromLanguage the from language
     * @param toLanguage the to language
     * @return the string
     * @throws Exception the exception
     */
    public static String execute(final String text, final String fromLanguage,
      final String toLanguage) throws Exception {
      validateReferrer();

      if (key == null) {
        throw new IllegalStateException(
            "You MUST have a Google API Key to use the V2 APIs. See http://code.google.com/apis/language/translate/v2/getting_started.html");
      }

      final String parameters =
          "format=text&key=" + key + "&q=" + URLEncoder.encode(text, "UTF-8")
              + "&target=" + toLanguage + "&source=" + fromLanguage;
      final URL url =
          new URL("https://www.googleapis.com/language/translate/v2");
      final JSONObject json = retrieveJSON(url, parameters);
      return getJSONResponse(json);
    }

    /**
     * Returns the JSON response data as a String. Throws an exception if the
     * status is not a 200 OK.
     * 
     * @param json The JSON object to retrieve the response data from.
     * @return The responseData from the JSONObject.
     * @throws Exception If the responseStatus is not 200 OK.
     */
    private static String getJSONResponse(final JSONObject json)
      throws Exception {
      final JSONObject data = json.getJSONObject("data");
      final JSONArray translations = data.getJSONArray("translations");
      final JSONObject translation = translations.getJSONObject(0);
      final String translatedText = translation.getString("translatedText");
      return translatedText;
    }
  }

  /**
   * Returns the api key.
   *
   * @return the api key
   */
  public String getApiKey() {
    return apiKey;
  }

  /**
   * Sets the api key.
   *
   * @param apiKey the api key
   */
  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }
}