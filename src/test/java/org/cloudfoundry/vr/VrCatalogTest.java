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
		assertEquals(HttpStatus.NO_CONTENT, checkToken(token.getBody()
				.get("id")));

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
	public void testGetTemplateRequest() {
		ResponseEntity<String> resp = getTemplateRequest(VrController.TOKEN,
				VrController.CATALOG_ID);
		assertNotNull(resp);
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		String temp = resp.getBody().toString();
		assertNotNull(temp);
		assertEquals("sioningReq", temp.substring(70, 80));
	}

	@Test
	public void testPostRequest() {
		ResponseEntity<String> resp = postRequest(VrController.TOKEN,
				VrController.CATALOG_ID, VrController.getJson("template.json"));
		assertNotNull(resp);
		assertEquals(HttpStatus.CREATED, resp.getStatusCode());
		String temp = resp.getBody().toString();
		assertNotNull(temp);
		assertEquals("b-edd7c798", temp.substring(70, 80));
	}

	@Test
	public void testRequestDetails() {
		ResponseEntity<String> dets = getRequestDetails(VrController.TOKEN,
				VrController.REQUEST_ID);
		assertNotNull(dets);
		assertEquals(HttpStatus.OK, dets.getStatusCode());
		assertEquals("b-edd7c798", dets.getBody().substring(70, 80));
	}

	// TODO string these together by processing responses
	@Test
	public void testUseCase() {
		// get an auth token
		Map<String, String> token = getToken().getBody();
		assertNotNull(token);

		// make sure it's valid
		assertEquals(HttpStatus.NO_CONTENT, checkToken(token.get("id")));

		// make sure it's still valid
		assertNotNull(getCatalog(token.get("id")));

		// get the catalog
		assertNotNull(getCatalog(token.get("id")));

		// get a template to request something from the catalog
		assertNotNull(getTemplateRequest(token.get("id"),
				VrController.CATALOG_ID));

		// use the template to make the request
		assertNotNull(postRequest(token.get("id"), VrController.CATALOG_ID,
				VrController.getJson("template.json")));

		// check on the request
		assertNotNull(getRequestDetails(token.get("id"),
				VrController.REQUEST_ID));
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

		return restTemplate.exchange(URI
				+ "/catalog-service/api/consumer/entitledCatalogItemViews",
				HttpMethod.GET, he, String.class);
	}

	private ResponseEntity<String> getTemplateRequest(String token,
			String catalogId) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", " Bearer " + token);
		HttpEntity<String> he = new HttpEntity<String>(headers);

		return restTemplate.exchange(URI
				+ "/service/api/consumer/entitledCatalogItems/" + catalogId
				+ "/requests/template", HttpMethod.GET, he, String.class);
	}

	private ResponseEntity<String> postRequest(String token, String catalogId,
			String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", " Bearer " + token);
		HttpEntity<String> he = new HttpEntity<String>(body, headers);

		return restTemplate.exchange(URI
				+ "/service/api/consumer/entitledCatalogItems/ " + catalogId
				+ "/requests", HttpMethod.POST, he, String.class);
	}

	private ResponseEntity<String> getRequestDetails(String token,
			String requestId) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", " Bearer " + token);
		HttpEntity<String> he = new HttpEntity<String>(headers);

		return restTemplate.exchange(URI
				+ "/catalog-service/api/consumer/requests/" + requestId,
				HttpMethod.GET, he, String.class);
	}
}
