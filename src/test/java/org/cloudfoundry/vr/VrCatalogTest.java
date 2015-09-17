package org.cloudfoundry.vr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@WebIntegrationTest(value = "server.port=9873")
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VrCatalogTest {

	ParameterizedTypeReference<Map<String, String>> mapType = new ParameterizedTypeReference<Map<String, String>>() {
	};

	ParameterizedTypeReference<HttpEntity<String>> stringType = new ParameterizedTypeReference<HttpEntity<String>>() {
	};

	private final RestTemplate restTemplate = new TestRestTemplate();

	private static final String URI = "http://localhost:9873";

	@Test
	public void testGetToken() {
		ResponseEntity<Map<String, String>> token = getToken();
		assertNotNull(token);
		assertEquals(HttpStatus.OK, token.getStatusCode());
		assertEquals("mycompany", token.getBody().get("tenant"));
	}

	@Test
	public void testCheckToken() {
		ResponseEntity<Map<String, String>> token = getToken();
		assertNotNull(token);
		assertEquals(HttpStatus.NO_CONTENT, checkToken(token.getBody().get("id")));

		assertEquals(HttpStatus.FORBIDDEN, checkToken(null));
	}

	@Test
	public void testGetCatalog() {
		ResponseEntity<String> resp = getCatalog(VrController.TOKEN);
		assertNotNull(resp);
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		String cat = resp.getBody().toString();
		assertNotNull(cat);
		assertEquals("nsumerEnti", cat.substring(70, 80));
	}

	@Test
	public void testUseCase() {
		Map<String, String> token = getToken().getBody();
		assertNotNull(token);
		assertEquals(HttpStatus.NO_CONTENT, checkToken(token.get("id")));
		assertNotNull(getCatalog(token.get("id")));
	}

	private ResponseEntity<Map<String, String>> getToken() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("username", "csummers@example.com");
		m.put("password", "mypassword");
		m.put("tenant", "mycompany");

		return restTemplate.exchange(URI + "/identity/api/tokens",
				HttpMethod.POST, new HttpEntity<Map<String, String>>(m),
				mapType);
	}

	private HttpStatus checkToken(String token) {
		return restTemplate.exchange(URI + "/identity/api/tokens/" + token,
				HttpMethod.GET, null, stringType).getStatusCode();
	}
	
	private ResponseEntity<String> getCatalog(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", " Bearer " + token);
		HttpEntity<String> he = new HttpEntity<String>(headers);

		return restTemplate
				.exchange(
						URI
								+ "/catalog-service/api/consumer/entitledCatalogItemViews",
						HttpMethod.GET, he, String.class);
	}
}
