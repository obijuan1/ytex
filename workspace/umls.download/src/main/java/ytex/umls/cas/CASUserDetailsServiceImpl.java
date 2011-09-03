package ytex.umls.cas;

import java.util.List;

import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


/**
 * Custom UserDetailsService which accepts any CAS user, "registering" new
 * users so they can be welcomed back to the site on subsequent logins.
 * 
 * @author Luke Taylor
 * @since 3.1
 */
public class CASUserDetailsServiceImpl implements UserDetailsService,
		AuthenticationUserDetailsService {

	// private Map<String, CustomUserDetailsService> registeredUsers = new
	// HashMap<String, CustomUserDetailsService>();

	private static final List<GrantedAuthority> DEFAULT_AUTHORITIES = AuthorityUtils
			.createAuthorityList("ROLE_USER");

	/**
	 * Implementation of {@code UserDetailsService}. We only need this to
	 * satisfy the {@code RememberMeServices} requirements.
	 */
	public UserDetails loadUserByUsername(String id)
			throws UsernameNotFoundException {
		UserDetails user = new User(id, "unused", true, true, true, true, DEFAULT_AUTHORITIES);
		return user;
	}

	/**
	 * Implementation of {@code AuthenticationUserDetailsService} which allows
	 * full access to the submitted {@code Authentication} object. Used by the
	 * OpenIDAuthenticationProvider.
	 */
	public UserDetails loadUserDetails(CasAuthenticationToken token) {
		return loadUserByUsername(token.getName());
	}


	@Override
	public UserDetails loadUserDetails(Authentication arg0)
			throws UsernameNotFoundException {
		return null;
	}
}
