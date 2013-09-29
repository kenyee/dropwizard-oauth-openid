package uk.co.froot.demo.openid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;

/**
 * <p>DropWizard Configuration to provide the following to application:</p>
 * <ul>
 * <li>Initialisation code</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public class AppConfiguration extends Configuration {

  /**
   * The cookie name for the session token
   */
  public static final String SESSION_TOKEN_NAME ="OpenIDDemo-Session";

  @NotEmpty
  @JsonProperty
  private String assetCachePolicy="maximumSize=10000, expireAfterAccess=5s";

  /**
   * How long a session cookie authentication can remain inactive before the user must signin in
   * TODO Implement this
   */
  @NotEmpty
  @JsonProperty
  private String cookieAuthenticationCachePolicy ="maximumSize=10000, expireAfterAccess=600s";

  @Valid
  @NotNull
  @JsonProperty
  private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

  public String getAssetCachePolicy() {
    return assetCachePolicy;
  }

  public JerseyClientConfiguration getJerseyClientConfiguration() {
    return httpClient;
  }

  public String getCookieAuthenticationCachePolicy() {
    return cookieAuthenticationCachePolicy;
  }

  @JsonProperty private String oauthSuccessUrl = "";
  public String getOAuthSuccessUrl() {
       return oauthSuccessUrl;
  }
  
  public static class OAuthCfgClass {
      @JsonProperty private String url;
      @JsonProperty private String name;
      @JsonProperty private String prefix;
      @JsonProperty private String key;
      @JsonProperty private String secret;
      @JsonProperty private String permissions;
      
      public String getUrl() {
          return url;
      }
      public String getName() {
          return name;
      }
      public String getPrefix() {
          return prefix;
      }
      public String getKey() {
          return key;
      }
      public String getSecret() {
          return secret;
      }
      public String getPermissions() {
          return permissions;
      }
  }
  @JsonDeserialize(contentAs = OAuthCfgClass.class)
  private List<OAuthCfgClass> oauthCfg;
  public List<OAuthCfgClass> getOAuthCfg() {
      return oauthCfg;
  }
  
  @JsonProperty
  private HashMap<String, String> oauthCustomCfg = null;
  public Map<String, String> OAuthCustomCfg() {
      return oauthCustomCfg;
  }

  public Properties getOAuthCfgProperties() {
      Properties properties = new Properties();
      for (OAuthCfgClass oauth : oauthCfg) {
          properties.put(oauth.getPrefix() + ".consumer_key",
                  oauth.getKey());
          properties.put(oauth.getPrefix() + ".consumer_secret",
                  oauth.getSecret());
          if (oauth.getPermissions() != null) {
              properties.put(oauth.getPrefix() + ".custom_permissions",
                      oauth.getPermissions());
          }
      }
      if (oauthCustomCfg != null) {
          // add any custom config strings
          properties.putAll(oauthCustomCfg);
      }
      return properties;
  }
  
  @JsonProperty
  private HashMap<String, String> adminUsers = null;
  public Map<String, String> getAdminUsers() {
      return adminUsers;
  }
  
  @JsonProperty
  private String proxyHost = null;
  public String getProxyHost() {
      return proxyHost;
  }
  
  @JsonProperty
  private int proxyPort = 8080;
  public int getProxyPort() {
      return proxyPort;
  }
}
