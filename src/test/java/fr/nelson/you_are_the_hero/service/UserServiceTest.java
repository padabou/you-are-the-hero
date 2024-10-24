package fr.nelson.you_are_the_hero.service;

import fr.nelson.you_are_the_hero.exception.AdminAlreadyExistException;
import fr.nelson.you_are_the_hero.exception.UserAllreadyExistException;
import fr.nelson.you_are_the_hero.model.db.AppUser;
import fr.nelson.you_are_the_hero.model.db.Role;
import fr.nelson.you_are_the_hero.model.dto.AuthRequestDto;
import fr.nelson.you_are_the_hero.model.dto.UserDto;
import fr.nelson.you_are_the_hero.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void test_createNewUser_OK() throws Exception {
        // Given
        AuthRequestDto authRequestDto = new AuthRequestDto();
        authRequestDto.setPassword("my-password");
        authRequestDto.setUsername("my-username");


        when(appUserRepository.existsByUsername(Mockito.anyString())).thenReturn(false);
        when(appUserRepository.save(Mockito.any())).thenReturn(new AppUser());
        when(passwordEncoder.encode(Mockito.anyString())).thenReturn("toto");

        // When
        AppUser result = userService.saveUser(authRequestDto);

        // Then
        Assertions.assertNotNull(result);
        verify(appUserRepository, times(1)).save(Mockito.any());
    }

    @Test
    public void test_createNewUserWithExistingOne_ThrowsException() {
        // Given
        AuthRequestDto authRequestDto = new AuthRequestDto();
        authRequestDto.setPassword("my-password");
        authRequestDto.setUsername("my-username");

        // When
        when(appUserRepository.existsByUsername(Mockito.anyString())).thenReturn(true);

        // Then
        Assertions.assertThrows(UserAllreadyExistException.class, () -> this.userService.saveUser(authRequestDto));
    }

    @Test
    public void testLoadUserByUsername_UserExists() {
        AppUser appUser = new AppUser();
        appUser.setUsername("jean neige");
        appUser.setPassword("Ygrid");
        appUser.setRole(Role.PLAYER);

        when(appUserRepository.findByUsername("jean neige")).thenReturn(appUser);

        UserDetails userDetails = userService.loadUserByUsername("jean neige");

        assertNotNull(userDetails);
        assertEquals("jean neige", userDetails.getUsername());
        assertEquals("Ygrid", userDetails.getPassword());
    }

    @Test
    public void testFindAppUserByUsername_UserExists() {
        AppUser appUser = new AppUser();
        appUser.setUsername("jean neige");
        appUser.setPassword("Ygrid");
        appUser.setRole(Role.PLAYER);

        when(appUserRepository.findByUsername("jean neige")).thenReturn(appUser);

        AppUser user = userService.findAppUserByUsername("jean neige");

        assertNotNull(user);
        assertEquals("jean neige", user.getUsername());
    }

    @Test
    public void testFindAppUserByUsername_UserNotFound() {
        when(appUserRepository.findByUsername("unknown-username")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("unknown-username");
        });

    }

    @Test
    public void testCreateAdminUser_Ok() {
        AppUser appUser = new AppUser();
        appUser.setUsername("jean neige");
        appUser.setPassword("Ygrid");
        appUser.setRole(Role.PLAYER);

        UserDto userDto = new UserDto("jean neige");

        when(userService.isAlreadyAnAdmin()).thenReturn(false);
        when(appUserRepository.findByUsername("jean neige")).thenReturn(appUser);
        when(appUserRepository.save(appUser)).thenReturn(appUser);

        AppUser adminUser = userService.createAdminUser(userDto);

        assertNotNull(adminUser);
        assertEquals("jean neige", adminUser.getUsername());
        assertEquals(Role.ADMIN, adminUser.getRole());

        verify(appUserRepository).save(appUser);
    }

    @Test
    public void testCreateAdminUser_AdminExists() {
        UserDto userDto = new UserDto("jean neige");

        when(userService.isAlreadyAnAdmin()).thenReturn(true);

        assertThrows(AdminAlreadyExistException.class, () -> {
            userService.createAdminUser(userDto);
        });
    }

    @Test
    public void testCreateAdminUser_WithInexistingAppUser() {
        UserDto userDto = new UserDto("jean neige");

        when(appUserRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.createAdminUser(userDto);
        });
    }

    @Test
    public void testIsAlreadyAnAdmin_NoAdminExist() {
        when(appUserRepository.existsByRole(Role.ADMIN)).thenReturn(false);

        Boolean isAnAdmin = userService.isAlreadyAnAdmin();

        assertFalse(isAnAdmin);
    }

    @Test
    public void testIsAlreadyAnAdmin_AdminExist() {
        when(appUserRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        Boolean isAnAdmin = userService.isAlreadyAnAdmin();

        assertTrue(isAnAdmin);
    }
}
