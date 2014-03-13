package demo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitResourceDetails;
import org.springframework.security.oauth2.common.util.OAuth2Utils;

/**
 * @author Ryan Heaton
 * @author Dave Syer
 */
@SpringApplicationConfiguration(classes=Application.class)
public class ImplicitProviderTests extends AbstractIntegrationTests {

	@Test
	@OAuth2ContextConfiguration(resource = NonAutoApproveImplicit.class, initialize = false)
	public void testPostForNonAutomaticApprovalToken() throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Basic " + new String(Base64.encode("user:password".getBytes())));
		context.getAccessTokenRequest().setHeaders(headers);
		try {
			assertNotNull(context.getAccessToken());
			fail("Expected UserRedirectRequiredException");
		}
		catch (UserRedirectRequiredException e) {
			// ignore
		}
		// add user approval parameter for the second request
		context.getAccessTokenRequest().add(OAuth2Utils.USER_OAUTH_APPROVAL, "true");
		context.getAccessTokenRequest().add("scope.read", "true");
		assertNotNull(context.getAccessToken());
	}

	static class NonAutoApproveImplicit extends ImplicitResourceDetails {
		public NonAutoApproveImplicit(Object target) {
			super();
			setClientId("my-trusted-client");
			setId(getClientId());
			setPreEstablishedRedirectUri("http://anywhere");
			ImplicitProviderTests test = (ImplicitProviderTests) target;
			setAccessTokenUri(test.serverRunning.getUrl("/oauth/authorize"));
			setUserAuthorizationUri(test.serverRunning.getUrl("/oauth/authorize"));
		}
	}

}
