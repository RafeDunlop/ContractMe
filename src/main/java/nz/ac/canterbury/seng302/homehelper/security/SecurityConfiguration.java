package nz.ac.canterbury.seng302.homehelper.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Custom security configuration based on the spring security handout.
 */
@Configuration
@EnableWebSecurity
@ComponentScan("com.baeldung.security")
public class SecurityConfiguration {

    /**
     * Custom authentication provider {@link CustomAuthenticationProvider}
     */
    @Autowired
    private CustomAuthenticationProvider authProvider;

    @Autowired
    private CustomAuthFailHandler authFailHandler;

    /**
     * Create an Authentication Manager with the custom auth provider.
     *
     * @param http http security configuration object
     * @return the new authentication manager
     * @throws Exception if building the object fails
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }

    /**
     * H2 Security filter chain.
     * For development purposes only.
     *
     * @param http http security configuration object from spring
     * @return Custom SecurityFilterChain
     * @throws Exception if building the object fails
     */
    @Bean
    @Order(1)
    public SecurityFilterChain h2AccessFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(AntPathRequestMatcher.antMatcher("/h2/**")).permitAll())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2/**")));
        return http.build();
    }

    /**
     * User Security filter chain.
     * This filter chain applies to admins and users and enforces the roles ADMIN, and USER respectively.
     *
     * @param http http security configuration object from spring
     * @return Custom SecurityFilterChain
     * @throws Exception if building the object fails
     */
    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        String[] paths = {"/user", "/renovations/**", "/admin", "/admin/**"};
        String[] userPaths = {"/user", "/renovations/**"};
        String[] adminPaths = {"/admin", "/admin/**"};
        http.securityMatcher(paths)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(userPaths).hasRole("USER")
                        .requestMatchers(adminPaths).hasRole("ADMIN")
                );
        return http.build();
    }

    /**
     * Default Security filter chain.
     *
     * @param http http security configuration object from spring
     * @return Custom SecurityFilterChain
     * @throws Exception if building the object fails
     */
    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        String[] allowedPaths = {"/", "/login", "/register", "/main", "/webjars/**", "/favicon.ico"};
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(allowedPaths).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/main")
                        .failureHandler(authFailHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );
        return http.build();
    }
}
