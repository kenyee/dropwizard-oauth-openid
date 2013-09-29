package uk.co.froot.demo.openid.model;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.brickred.socialauth.Album;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.AuthProviderFactory;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.plugin.AlbumsPlugin;
import org.brickred.socialauth.util.AccessGrant;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.froot.demo.openid.AppConfiguration;
import uk.co.froot.demo.openid.AppConfiguration.OAuthCfgClass;
import uk.co.froot.demo.openid.OpenIDDemoService;
import uk.co.froot.demo.openid.model.security.User;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * <p>
 * Base class to provide the following to views:
 * </p>
 * <ul>
 * <li>Access to common data (user, adverts etc)</li>
 * 
 * @since 0.0.1  
 */
public class BaseModel {
    private static final Logger log = LoggerFactory
            .getLogger(BaseModel.class);

    public BaseModel() {
        AppConfiguration cfg = OpenIDDemoService.getConfig();
        oauthCfg = cfg.getOAuthCfg();
    }

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // list of OAuth configs is used for display on login page
    // probably should be moved to a new LoginPageModel
    private List<OAuthCfgClass> oauthCfg;

    public List<OAuthCfgClass> getOAuthCfg() {
        return oauthCfg;
    }

    // list of social network contacts
    private List<Contact> contacts = null;
    public List<Contact> getContacts() {
        return contacts;
    }
    
    // list of social network albums
    private List<Album> albums = null;
    public List<Album> getAlbums() {
        return albums;
    }

    /**
     * @return Some Markdown rendered as HTML - this is an inefficient way of
     *         performing this operation See the
     *         <code>/common/markdown.ftl</code> to see where it is displayed
     * 
     * @throws IOException
     *             If something goes wrong
     */
    public String getMarkdownHtml() throws IOException {

        URL url = BaseModel.class
                .getResource("/views/markdown/demo-all-elements.md");
        String markdown = Resources.toString(url, Charsets.UTF_8).trim();

        // New processor each time due to pegdown not being thread-safe
        // internally
        PegDownProcessor processor = new PegDownProcessor();

        // Return the rendered HTML
        return processor.markdownToHtml(markdown);

    }

    public void loadContacts() {
        if (getUser() != null) {
            try {
                if (getUser().getOAuthInfo() != null) {
                    AccessGrant grant = getUser().getOAuthInfo();
                    AuthProvider provider = AuthProviderFactory.getInstance(grant.getProviderId(),
                            OpenIDDemoService.getConfig().getOAuthCfgProperties());
                    provider.setAccessGrant(grant);
                    provider.registerPlugins(); // this activates the plugins for this provider
                    contacts = provider.getContactList();
                    if (provider.getProviderId().equals("linkedin")) {
                        // LinkedIn profiles are returned even if private w/ a private name
                        // so scrub weird private contacts that linkedin's API returns
                        for (int i = (contacts.size()-1);  i >= 0;  i--) {
                            Contact contact = contacts.get(i);
                            if (contact.getFirstName().equals("private")) {
                                contacts.remove(i);
                            }
                        }                        
                        // patch up bogus null DisplayName, profileURLs returned by LinkedIn plugin
                        for (Contact contact: contacts) {
                            if (contact.getProfileUrl() == null) {
                                contact.setProfileUrl("#");
                            }
                            if ((contact.getDisplayName() == null) &&
                                    (contact.getFirstName() != null) &&
                                    (contact.getLastName() != null)) {
                                contact.setDisplayName(contact.getFirstName() + " " + contact.getLastName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error loading contacts: " + e);
            } 
        }
    }

    public void loadAlbums() {
        if (getUser() != null) {
            try {
                if (getUser().getOAuthInfo() != null) {
                    // use the accessgrant we stored away    
                    AccessGrant grant = getUser().getOAuthInfo();
                    AuthProvider provider = AuthProviderFactory.getInstance(grant.getProviderId(),
                            OpenIDDemoService.getConfig().getOAuthCfgProperties());
                    provider.setAccessGrant(grant);
                    provider.registerPlugins(); // this activates the plugins for this provider
                    if (provider.isSupportedPlugin(org.brickred.socialauth.plugin.AlbumsPlugin.class)) {
                        AlbumsPlugin p = provider.getPlugin(org.brickred.socialauth.plugin.AlbumsPlugin.class);
                        albums = p.getAlbums();
                    }
                }
            } catch (Exception e) {
                log.error("Error loading albums: " + e);
            } 
        }
    }

}
