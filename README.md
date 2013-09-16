# OAuth Dropwizard Demo

This project is based on [Gary Rowe's OpenID Dropwizard sample](https://github.com/gary-rowe/DropwizardOpenID),
but integrates OAuth support for authentication instead, because some 
applications need to be able to post to a user's timeline or access photos, 
etc. in their social network.  OpenID is only for authentication, not
authorization.  OAuth can be used for authentication (as long as
you request email address access) and authorization to use the
social network.

Note that you can also login using OpenID.  OAuth as implemented in this
is an alternative login method, but one that also gives you access to
feeds/contacts/albums.


## OAuth Libraries

Unfortunately, there's no single Java library that does everything.  Every
library seems to have a weakness:

* [Scribe](https://github.com/fernandezpablo85/scribe-java) - only covers 
OAuth URL generation, access token creation
from the request token and signing subsequent requests, but doesn't
wrap any of the APIs (you have to call the REST endpoints yourself)
* [Spring Social](http://www.springsource.org/spring-social) - provides 
OAuth URL generation, a servlet filter to handle the return tokens, 
controllers for Spring MVC, and wrappers for the social network APIs, 
but doesn't include Google+ APIs (though there is a 
[3rd party plugin](https://github.com/GabiAxel/spring-social-google) for Google+).
* [SocialAuth](http://code.google.com/p/socialauth) - similar 
to Spring Social in that it provides OAuth handling, a servlet filter to 
handle the returned token, integration examples for Seam and JBoss CDI/JSF 
and Struts and Spring MVC, and plugins to wrapper the social network feed 
APIs (only Facebook, Twitter, and LinkedIn, but not Google+ oddly enough).
* [Google's OAuth Library](http://code.google.com/p/google-oauth-java-client/)
 and [API Client Library](http://code.google.com/p/google-api-java-client/)
 can be used for access to Google's social network to fill in any gaps
 the other libraries are missing.

Scribe is the most flexible, but also the most basic.
SocialAuth is a middle ground where you can write plugins for accessing
parts of each social network.
Spring Social is the most comprehensive, but also the most complicated
to hook in and is best used with other parts of the Spring Framework.

SocialAuth seemed the best middle ground, so that was used for
the OAuth support for this application.

## Getting started

From an IDE, just run the `OpenIDDemoService.main()` with a runtime 
configuration that passes application parameters of `server app.yml`.

From the console, just build with Maven and run it:
```
mvn clean install
java -jar target/dropwizard-openid-1.1.0.jar server app.yml
```

From the console, you can also use Gradle to build it into a single jar and run
```
gradle runShadow
```

## Proxy settings

If you are behind a firewall you will need to set the proxy
This is configured in the app.yml file.

## Authorization

Here is an example of the authorization annotation as used in ```PrivateInfoResource```. 

```java
/**
 * @return The private home view if authenticated
 */
@GET
@Path("/home")
@Timed
@CacheControl(noCache = true)
public PublicFreemarkerView viewHome(
@RestrictedTo(Authority.ROLE_PUBLIC)
User publicUser) {

  BaseModel model = newBaseModel();
  return new PublicFreemarkerView<BaseModel>("private/home.ftl", model);

}
```

## Adding yourself as an "admin"

The code supports different levels of authority (PUBLIC and ADMIN). 
To add yourself as an admin, add your email address to the list of adminUsers in app.yml.

The user is uniquely identified by email address and provider ID; if you only check the
email address, they might be able to find a provider that lets them enter your email address.
If you're using openid, use "openid" for the provider ID.

Note that you can't use a Twitter login for specifying an admin because there's no
way to get the user's email address.  This is because Twitter is too lazy to implement
a permissions system like Facebook/Google have.  [Gripe to Twitter](https://dev.twitter.com/discussions/1737)
if you think this is stupid.

## General code layout

* Configuration class for app.yaml is in src/main/java/OpenIDDemoConfiguration.java
* The standard Dropwizard service/app is src/main/java/OpenIDDemoService.java
* OpenID user classes are used for both OpenID and OAuth despite the naming (which came
from Gary Rowe's original project); I did minimal renames initially, just in case these
changes are merged into the original project
* The in-mem user cache is in src/main/java/core/*.java
* The user model is in src/main/java/model/security/User.java
* The REST path handlers are in src/main/java/resources/*.java as 
usual for DropWizard projects.
The OAuth rest handler is in src/main/java/resources/PublicOAuthResource.java.
* The model used for the FreeMarker views is in src/main/java/model/BaseModel.java.  
It also holds anything that can be displayed on the small set of FreeMarker templates 
included.
* The home page when you log in w/ all the social contacts/albums displayed is
in src/main/resources/views/ftl/private/home.ftl.  The contacts/albums should not
be loaded this way in a real application because it takes one second...sometimes way
over if you have a lot :-)  In a real application, the social info should be AJAX
loaded in the background but this was done for simpler testing.
* Bootstrap was added by modifying the resources/views/ftl/includes/common header.ftl
and cdn-scripts.ftl and header.ftl and footer.ftl files.

## Changes to Dropwizard-OpenID
 
* Cleaned up warning msgs
* Added Gradle support, though currently you have to comment out the
shadow plugin references to import into Eclipse because of a shadow plugin
bug with a create method in that plugin or a bug in the Eclipse Gradle plugin :-(
* Added User to BaseResource so pages can display a logout button if you're logged in
* Added Bootstrap via a CDN to make templates look better
* Moved login.ftl from openid subdir to common
* Added logout support
* Added support for configuring OAuth providers/permissions via the app.yml
* Moved /login and /logout REST APIs from /openid to / (into PublicHomeResource)
* Added images for facebook/google/twitter/linkedin OAuth buttons
* Added logout button and user state to all BaseModels
* Added adminUsers config in app.yml
* Added display of user's contact list and albums to /private/home
(note that SocialAuth doesn't currently have a Google+ plugin for these)
* Added proxy config to app.yml
* Added custom SocialAuth OAuth properties to app.yml
* Moved cookie generator to BaseResource since it's used by OpenID and OAuth
