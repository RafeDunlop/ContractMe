package nz.ac.canterbury.seng302.homehelper.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
     * Security filter chain.
     *
     * @param http http security configuration object from spring
     * @return Custom SecurityFilterChain
     * @throws Exception if building the object fails
     */
    @Bean

    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(AntPathRequestMatcher.antMatcher("/h2/**")).permitAll())
                // Permit access to the h2 console
                .headers(headers -> headers.frameOptions().disable())
                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2/**")))
                .authorizeHttpRequests()
                // Allow "/", "/register", "login", "/main", and "/webjars" to anyone (need webjars for bootstrap css)
                .requestMatchers("/", "/register", "/login", "/main", "/webjars/**")
                .permitAll()
                // Only allow admins to reach the "/admin" page
                .requestMatchers("/admin")
                .hasRole("ADMIN")
                .requestMatchers("/user", "user/edit", "user/edit/updatePassword")
                .hasRole("USER")
                .anyRequest()
                .authenticated()
                .and()
                // Define logging in, a POST "/login" endpoint now exists under the hood, after login redirect to main page
                // Errors are handled by CustomAuthFailHandler and displayed by the LoginController
                .formLogin().loginPage("/login").loginProcessingUrl("/login").defaultSuccessUrl("/main").failureHandler(authFailHandler)
                .and()
                .logout().logoutUrl("/logout").logoutSuccessUrl("/login").invalidateHttpSession(true).deleteCookies("JSESSIONID");
        return http.build();
    }
}
