package org.cloudfoundry.vr;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VrController {

	private static final Logger LOG = Logger.getLogger(VrController.class);

	public static final String TOKEN = "MTM5MTI1OTg5MDQwMzozNDQyZWMxZmQ5ZDliODUzMGFiMjp0ZW5hbnQ6cWV1c2VybmFtZTjYjY1ZjhiODI2OTg4ODU3M2UwOTUwOWRkMjlmYWRjNWQ4NjJkOTk1YmE3MTg1MWZhOTc2MjEyYjYxZmU3YTVhZDcwNzM3ZTg3ZDNjNDk2ZDlmNA==";
	public static final String CATALOG_ID = "dc808d12-3786-4f7c-b5a1-d5f997c8ad66";
	public static final String REQUEST_ID = "7aaf9baf-aa4e-47c4-997b-edd7c7983a5b";

	@RequestMapping(value = "/identity/api/tokens ", method = RequestMethod.POST)
	public HttpEntity<Map<String, String>> tokens(
			@RequestBody Map<String, String> request) {
		Map<String, String> m = new HashMap<String, String>();
		m.put("expires", "2015-08-01T13:09:45.619Z");
		m.put("id", TOKEN);
		m.put("tenant", request.get("tenant").toString());

		return new ResponseEntity<>(m, HttpStatus.OK);
	}

	@RequestMapping(value = "/identity/api/tokens/{token}", method = RequestMethod.GET)
	public HttpEntity<String> tokens(@PathVariable String token) {
		if (token == null || token.length() < 1
				|| token.trim().equalsIgnoreCase("null")) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Authorization", " Bearer " + TOKEN);

		return new ResponseEntity<>(responseHeaders, HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/catalog-service/api/consumer/entitledCatalogItemViews", method = RequestMethod.GET)
	public HttpEntity<String> entitledCatalogItemViews(
			@RequestHeader("Authorization") String token) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(getJson("catalog.json"), HttpStatus.OK);
	}

	@RequestMapping(value = "/service/api/consumer/entitledCatalogItems/{catalogId}/requests/template", method = RequestMethod.GET)
	public HttpEntity<String> requestTemplate(
			@RequestHeader("Authorization") String token,
			@PathVariable String catalogId) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		if (catalogId == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(getJson("template.json"), HttpStatus.OK);
	}

	@RequestMapping(value = "/service/api/consumer/entitledCatalogItems/{catalogId}/requests", method = RequestMethod.POST)
	public HttpEntity<String> requestCatalogItem(
			@RequestHeader("Authorization") String token,
			@PathVariable String catalogId, @RequestBody String request) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		if (catalogId == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(getJson("request.json"), HttpStatus.CREATED);
	}

	@RequestMapping(value = "/catalog-service/api/consumer/requests/{requestId}", method = RequestMethod.GET)
	public HttpEntity<String> requestDetails(
			@RequestHeader("Authorization") String token,
			@PathVariable String requestId) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		if (requestId == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(getJson("request.json"), HttpStatus.OK);
	}

	public static String getJson(String fileName) {
		try {
			URI u = new ClassPathResource(fileName).getURI();
			return new String(Files.readAllBytes(Paths.get(u)));
		} catch (IOException e) {
			LOG.error(e);
			return e.getMessage();
		}
	}
}
