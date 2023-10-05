package com.devsuperior.dscommerce.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscommerce.dto.UserDTO;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.projections.UserDetailsProjection;
import com.devsuperior.dscommerce.repositories.UserRepository;
import com.devsuperior.dscommerce.tests.UserDetailsFactory;
import com.devsuperior.dscommerce.tests.UserFactory;
import com.devsuperior.dscommerce.utils.CustomUserUtil;

@ExtendWith(SpringExtension.class)
public class UserServiceTests {

	@InjectMocks
	private UserService service;

	@Mock
	private UserRepository repository;

	@Mock
	private CustomUserUtil customUserUtil;

	private String existingUsername, noExistingUsername;
	@SuppressWarnings("unused")
	private User user;
	private List<UserDetailsProjection> userDeatils;
	private UserDTO userDTO;

	@BeforeEach
	void setup() throws Exception {

		existingUsername = "maria@gmail.com";
		noExistingUsername = "user@gmail.com";

		user = UserFactory.createCustomClientUser(1L, existingUsername);

		userDeatils = UserDetailsFactory.createCustomAdminUser(existingUsername);

		Mockito.when(repository.searchUserAndRolesByEmail(existingUsername)).thenReturn(userDeatils);
		Mockito.when(repository.searchUserAndRolesByEmail(noExistingUsername)).thenReturn(new ArrayList<>());

		Mockito.when(repository.findByEmail(existingUsername)).thenReturn(Optional.of(user));
		Mockito.when(repository.findByEmail(noExistingUsername)).thenReturn(Optional.empty());


	}

	@Test
	public void loadUserByUsernameshouldReturnUserDetailsWhenUserExisting() {

		UserDetails result = service.loadUserByUsername(existingUsername);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getUsername(), existingUsername);
	}

	@Test
	public void loadUserByUsernameshouldReturnThrowUsernameNotFoundExceptionWhenUserDoesNotExisting() {

		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			service.loadUserByUsername(noExistingUsername);
		});
	}

	@Test
	public void authenticatedShouldReturnUserWhenUserExistis() {

		Mockito.when(customUserUtil.getLoggedUsername()).thenReturn(existingUsername);

		User result = service.authenticated();

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getUsername(), existingUsername);

	}

	@Test
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExisting() {

		Mockito.doThrow(ClassCastException.class).when(customUserUtil).getLoggedUsername();

		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			service.authenticated();
		});
	}

	@Test
	public void getMeShouldReturnUserDTOWhenUserAuthenticated() {
			
		
		UserService spyService = Mockito.spy(service);
		Mockito.doReturn(user).when(spyService).authenticated();
		userDTO = spyService.getMe();
		
		Assertions.assertNotNull(userDTO);
		Assertions.assertEquals(userDTO.getEmail(), existingUsername);
		
	}
	
	@Test
	public void getMeShouldThrowUsernameNotFoundExceptionWhenUserNoAuthenticated() {
		UserService spyService = Mockito.spy(service);
		Mockito.doThrow(UsernameNotFoundException.class).when(spyService).authenticated();
		
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			@SuppressWarnings("unused")
			UserDTO result = spyService.getMe();
		});
	}
}
