package demo.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeycloakHelper {

	private static final String CLIENT_LINK_TEMPLATE = "%s/realms/%s/clients/%s/redirect";

	private final KeycloakSpringBootProperties keycloakSpringBootProperties;

	public String getClientLink(String clientId) {
		return String.format(CLIENT_LINK_TEMPLATE, //
				keycloakSpringBootProperties.getAuthServerUrl(), //
				keycloakSpringBootProperties.getRealm(), //
				clientId //
		);
	}
}
