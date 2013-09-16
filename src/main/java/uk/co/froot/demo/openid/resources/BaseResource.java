package uk.co.froot.demo.openid.resources;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import uk.co.froot.demo.openid.OpenIDDemoConfiguration;
import uk.co.froot.demo.openid.model.BaseModel;
import uk.co.froot.demo.openid.model.ModelBuilder;
import uk.co.froot.demo.openid.model.security.User;

/**
 * <p>Abstract base class to provide the following to subclasses:</p>
 * <ul>
 * <li>Provision of common methods</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public abstract class BaseResource {
  private static final Logger log = LoggerFactory.getLogger(BaseResource.class);

  protected static final String OPENID_IDENTIFIER_KEY = "openid-identifier-key";

  /**
   * Jersey creates a fresh resource every request so this is safe
   */
  @Context
  protected UriInfo uriInfo;

  /**
   * Jersey creates a fresh resource every request so this is safe
   */
  @Context
  protected HttpHeaders httpHeaders;

  /**
   * Jersey creates a fresh resource every request so this is safe
   */
  @Context
  protected HttpServletRequest request;
  

  protected final ModelBuilder modelBuilder = new ModelBuilder();

  public BaseResource() {

  }

  /**
   * @return The most appropriate locale for the upstream request (never null)
   */
  public Locale getLocale() {
    // TODO This should be a configuration setting
    Locale defaultLocale = Locale.UK;

    Locale locale;
    if (httpHeaders == null) {
      locale = defaultLocale;
    } else {
      locale = httpHeaders.getLanguage();
      if (locale == null) {
        locale = defaultLocale;
      }
    }
    return locale;
  }

  /**
   * Utility method to create a base model present on all non-authenticated resources
   *
   * @return A base model
   */
  protected BaseModel newBaseModel() {

    BaseModel model = modelBuilder.newBaseModel(httpHeaders);   

    return model;
  }


  /**
   * @param user A user with a session token. If absent then the cookie will be removed.
   *
   * @return A cookie with a long term expiry date suitable for use as a session token for OpenID
   */
  protected NewCookie replaceSessionTokenCookie(Optional<User> user) {

    if (user.isPresent()) {

      String value = user.get().getSessionToken().toString();

      log.debug("Replacing session token with {}", value);

      return new NewCookie(
        OpenIDDemoConfiguration.SESSION_TOKEN_NAME,
        value,   // Value
        "/",     // Path
        null,    // Domain
        null,    // Comment
        86400 * 30, // 30 days
        false);
    } else {
      // Remove the session token cookie
      log.debug("Removing session token");

      return new NewCookie(
        OpenIDDemoConfiguration.SESSION_TOKEN_NAME,
        null,   // Value
        null,    // Path
        null,   // Domain
        null,   // Comment
        0,      // Expire immediately
        false);
    }
  }

}
