package uk.co.froot.demo.openid.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.froot.demo.openid.AppConfiguration.OAuthCfgClass;
import uk.co.froot.demo.openid.OpenIDDemoService;
import uk.co.froot.demo.openid.core.InMemoryUserCache;
import uk.co.froot.demo.openid.model.security.Authority;
import uk.co.froot.demo.openid.model.security.User;

import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;

/**
 * <p>
 * Resource to provide the following to application:
 * </p>
 * <ul>
 * <li>OAuth authentication handling</li>
 * </ul>
 * 
 * @since 0.0.1
 */
@Path("/oauth")
@Produces(MediaType.TEXT_HTML)
public class PublicOAuthResource extends BaseResource {

    private static final Logger log = LoggerFactory
            .getLogger(PublicOAuthResource.class);

    /**
     * Default constructor
     */
    public PublicOAuthResource() {
    }

    @GET
    @Timed
    @Path("/request")
    public Response requestOAuth(@Context HttpServletRequest request,
            @QueryParam("provider") String provider) throws URISyntaxException {
        if (provider == null) {
            log.debug("Missing provider ID");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // instantiate SocialAuth for this provider type and tuck into session
        List<OAuthCfgClass> oauthCfg = OpenIDDemoService.getConfig()
                .getOAuthCfg();
        if (oauthCfg != null) {
            // get the authentication URL for this provider
            try {
                SocialAuthManager manager = getSocialAuthManager();
                request.getSession().setAttribute("authManager", manager);

                java.net.URI url = new URI(manager.getAuthenticationUrl(
                        provider, OpenIDDemoService.getConfig()
                                .getOAuthSuccessUrl()));
                log.debug("OAuth Auth URL: {}", url);
                return Response.temporaryRedirect(url).build();
            } catch (Exception e) {
                log.error("SocialAuth error: {}", e);
            }
        }
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    /**
     * Handles the OAuth server response to the earlier AuthRequest
     * 
     * @return The OAuth identifier for this user if verification was
     *         successful
     */
    @GET
    @Timed
    @Path("/verify")
    public Response verifyOAuthServerResponse(
            @Context HttpServletRequest request) {

        // this was placed in the session in the /request resource        
        SocialAuthManager manager = (SocialAuthManager) request.getSession()
                .getAttribute("authManager");

        if (manager != null) {
            try {
                // call connect method of manager which returns the provider
                // object
                Map<String, String> params = SocialAuthUtil
                        .getRequestParametersMap(request);
                AuthProvider provider = manager.connect(params);

                // get profile
                Profile p = provider.getUserProfile();

                log.info("Logging in user '{}'", p);

                // at this point, we've been validated, so save off this user's
                // info
                User tempUser = new User(null, UUID.randomUUID());
                tempUser.setOpenIDIdentifier(p.getValidatedId());
                tempUser.setOAuthInfo(provider.getAccessGrant());

                tempUser.setEmailAddress(p.getEmail());
                if ((p.getFirstName() == null) && (p.getLastName() == null)) {
                    // Twitter doesn't return first/last name fields but does include
                    // a fullname property we can use to generate them
                    if (p.getFullName() != null) {
                        String[] parts = p.getFullName().split("-");
                        if (parts.length > 1) {
                            tempUser.setFirstName(parts[0]);
                            tempUser.setLastName(parts[parts.length-1]);
                        } else {
                            tempUser.setFirstName(parts[0]);
                            tempUser.setLastName(parts[0]);
                        }
                    }
                } else {
                    tempUser.setFirstName(p.getFirstName());
                    tempUser.setLastName(p.getLastName());
                }
                tempUser.setUserName(p.getFullName());
                

                // Provide a basic authority in light of successful
                // authentication
                tempUser.getAuthorities().add(Authority.ROLE_PUBLIC);
                // see if this is an admin user (match email addr and provider)
                if ((OpenIDDemoService.getConfig().getAdminUsers() != null)
                        && (tempUser.getEmailAddress() != null)) {
                    Map<String, String> adminUsers = OpenIDDemoService
                            .getConfig().getAdminUsers();
                    if (adminUsers.containsKey(tempUser.getEmailAddress())
                            && (adminUsers.get(tempUser.getEmailAddress())
                                    .equals(provider.getProviderId()))) {
                        tempUser.getAuthorities().add(Authority.ROLE_ADMIN);
                    }
                }

                // Search for a pre-existing User matching the temp User
                Optional<User> userOptional = InMemoryUserCache.INSTANCE
                        .getByOpenIDIdentifier(tempUser.getOpenIDIdentifier());
                if (!userOptional.isPresent()) {
                    // Persist the user with the generated session token
                    InMemoryUserCache.INSTANCE.put(tempUser.getSessionToken(),
                            tempUser);

                } else {
                    tempUser = userOptional.get();
                }

                return Response
                        .temporaryRedirect(new URI("/private/home"))
                        .cookie(replaceSessionTokenCookie(Optional.of(tempUser)))
                        .build();
            } catch (Exception e1) {
                log.error("Error reading profile info: {}, {}", e1.getMessage(), e1.getCause().getMessage());
            }
        }

        // Must have failed to be here
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    /**
     * Gets an initialized SocialAuthManager
     * @return gets an initialized SocialAuthManager
     */
    private SocialAuthManager getSocialAuthManager() {
        SocialAuthConfig config = SocialAuthConfig.getDefault();
        try {
            config.load(OpenIDDemoService.getConfig().getOAuthCfgProperties());
            SocialAuthManager manager = new SocialAuthManager();
            manager.setSocialAuthConfig(config);
            return manager;
        } catch (Exception e) {
            log.error("SocialAuth error: " + e);
        }
        return null;
    }

}
