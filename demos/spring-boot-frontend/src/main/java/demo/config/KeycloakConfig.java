package demo.config;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Principal;

@KeycloakConfiguration
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
public class KeycloakConfig extends KeycloakWebSecurityConfigurerAdapter {

  @Bean
  public KeycloakConfigResolver keycloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(keycloakAuthenticationProvider());
  }

  @Bean
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new RegisterSessionAuthenticationStrategy(buildSessionRegistry());
  }

  @Bean
  protected SessionRegistry buildSessionRegistry() {
    return new SessionRegistryImpl();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http
      .authorizeRequests()
      .antMatchers("/account", "/todos*").authenticated()
      .anyRequest().permitAll();
  }

  /**
   * Returns the {@link KeycloakSecurityContext} from the Spring {@link ServletRequestAttributes}'s {@link Principal}.
   * <p>
   * The principal must support retrieval of the KeycloakSecurityContext, so at this point, only {@link KeycloakPrincipal}
   * values and {@link KeycloakAuthenticationToken} are supported.
   *
   * @return the current <code>KeycloakSecurityContext</code>
   */
  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public KeycloakSecurityContext getKeycloakSecurityContext() {

    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    Principal principal = attributes.getRequest().getUserPrincipal();
    if (principal == null) {
      return null;
    }

    if (principal instanceof KeycloakAuthenticationToken) {
      principal = Principal.class.cast(KeycloakAuthenticationToken.class.cast(principal).getPrincipal());
    }

    if (principal instanceof KeycloakPrincipal) {
      return KeycloakPrincipal.class.cast(principal).getKeycloakSecurityContext();
    }

    return null;
  }

  @Bean
  public FilterRegistrationBean keycloakAuthenticationProcessingFilterRegistrationBean(
    KeycloakAuthenticationProcessingFilter filter) {
    FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean keycloakPreAuthActionsFilterRegistrationBean(
    KeycloakPreAuthActionsFilter filter) {
    FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  @Bean
  public KeycloakHelper keycloakHelper(KeycloakSpringBootProperties keycloakSpringBootProperties) {
    return new KeycloakHelper(keycloakSpringBootProperties);
  }
}
