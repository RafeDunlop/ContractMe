package nz.ac.canterbury.seng302.homehelper.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
     * Security filter chain. Provides authentication for pages, defines logging in/out, and
     * allows access to database.
     * @param http http security configuration object from spring
     * @return Custom SecurityFilterChain
     * @throws Exception if building the object fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        // Give access to database and allow all users to go on the matching pages.
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2/**")).permitAll()
                        .requestMatchers("/", "/register", "/login",  "/location/**", "/confirm-registration", "/password/**", "/webjars/**", "/css/**", "/js/**").permitAll()

                        // Only the specified roles can reach the matching pages
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/main", "/user/**", "/renovations/**", "/logout").hasRole("USER")
                        .anyRequest().authenticated())

                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2/**")))

                // Define logging in, a POST "/login" endpoint now exists under the hood, after login redirect to main page.
                // Errors are handled by CustomAuthFailHandler and displayed by the LoginController.
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/main", true)
                        .failureHandler(authFailHandler))
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }
}