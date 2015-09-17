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

		return new ResponseEntity<>(getCatalogJson(), HttpStatus.OK);
	}

	private String getCatalogJson() {
		try {
			URI u = new ClassPathResource("catalog.json").getURI();
			return new String(Files.readAllBytes(Paths.get(u)));
		} catch (IOException e) {
			LOG.error(e);
			return e.getMessage();
		}
	}
}
