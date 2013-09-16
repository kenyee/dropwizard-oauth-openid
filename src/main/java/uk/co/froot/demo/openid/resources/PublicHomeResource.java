package uk.co.froot.demo.openid.resources;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import uk.co.froot.demo.openid.core.InMemoryUserCache;
import uk.co.froot.demo.openid.model.BaseModel;
import uk.co.froot.demo.openid.model.ModelBuilder;
import uk.co.froot.demo.openid.model.security.User;
import uk.co.froot.demo.openid.views.PublicFreemarkerView;

import com.google.common.base.Optional;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import com.yammer.dropwizard.views.View;
import com.yammer.metrics.annotation.Timed;

/**
 * <p>Resource to provide the following to application:</p>
 * <ul>
 * <li>Provision of configuration for public home page</li>
 * </ul>
 *
 * @since 0.0.1
 */
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class PublicHomeResource extends BaseResource {
    //private static final Logger log = LoggerFactory.getLogger(PublicHomeResource.class);

    private final ModelBuilder modelBuilder = new ModelBuilder();

  /**
   * Provide the initial view on to the system
   *
   * @return A localised view containing HTML
   */
  @GET
  @Timed
  @CacheControl(noCache = true)
  public PublicFreemarkerView<BaseModel> viewHome() {

    BaseModel model = newBaseModel();
    return new PublicFreemarkerView<BaseModel>("common/home.ftl",model);
  }

  /**
   * Provide the initial view on to the system
   *
   * @return A localised view containing HTML
   */
  @GET
  @Path("/markdown")
  @Timed
  @CacheControl(noCache = true)
  public PublicFreemarkerView<BaseModel> viewMarkdown() {

    BaseModel model = newBaseModel();
    return new PublicFreemarkerView<BaseModel>("common/markdown.ftl",model);
  }

  /**
   * Provide the initial view on to the system
   *
   * @return A the favicon images from the assets
   */
  @GET
  @Path("favicon.ico")
  @Timed
  @CacheControl(maxAge = 24, maxAgeUnit = TimeUnit.HOURS)
  public Response viewFavicon() {

    InputStream is = PublicHomeResource.class.getResourceAsStream("/assets/favicon.ico");

    return Response.ok(is).build();
  }

  
  /**
   * @return A login view with a session token
   */
  @GET
  @Path("/login")
  public View login() {

    return new PublicFreemarkerView<BaseModel>("common/login.ftl",
            modelBuilder.newBaseModel(httpHeaders));
  }

  /**
   * @return A login view with a session token
 * @throws URISyntaxException 
   */
  @GET
  @Path("/logout")
  public Response logout() throws URISyntaxException {

    BaseModel model = modelBuilder.newBaseModel(httpHeaders);
    User user = model.getUser();
    if (user != null) {
      // (We'll delete the user but really this would just be an update)
      InMemoryUserCache.INSTANCE.hardDelete(user);
      model.setUser(null);
    }

    // Remove the session token which will have the effect of logout
    return Response
      .temporaryRedirect(new URI("/"))
      .cookie(replaceSessionTokenCookie(Optional.<User>absent()))
      .build();

  }
}
