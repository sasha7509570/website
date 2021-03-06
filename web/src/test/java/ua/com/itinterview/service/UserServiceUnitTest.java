package ua.com.itinterview.service;

import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import ua.com.itinterview.dao.UserDao;
import ua.com.itinterview.entity.UserEntity;
import ua.com.itinterview.web.command.UserCommand;
import ua.com.itinterview.web.command.UserEditProfileCommand;

import javax.persistence.EntityNotFoundException;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class UserServiceUnitTest {

    private static final int USER_ID = 1;
    private static final String EMAIL = "user@email.com";
    private static final String NAME = "name";
    private static final String USER_NAME = "userName";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String FAKE_USER_NAME = "Fake User Name";
    private static final String NEW_NAME = "new name";
    private static final String NEW_EMAIL = "new email";
    private static final int NEW_USER_ID = USER_ID + 1;
    private UserDao userDaoMock;
    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @Before
    public void createMocks() {
	userDaoMock = createMock(UserDao.class);
	userService = new UserService();
	userService.userDao = userDaoMock;
	passwordEncoder = createMock(PasswordEncoder.class);
	userService.passwordEncoder = passwordEncoder;
    }

    private void replayAllMocks() {
	replay(userDaoMock, passwordEncoder);
    }

    @Test
    public void testConvertUserCommandToUserEntity() {
	replayAllMocks();
	UserCommand userCommand = createUserCommandWithPassword();
	UserEntity expectedUserEntity = createUserEntity();
	UserEntity actualUserEntity = new UserEntity(userCommand);
	assertEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void testConvertUserEntityToUserCommand() {
	replayAllMocks();
	UserEntity userEntity = createUserEntity();
	UserCommand expectedUserCommand = createUserCommand();
	UserCommand actualUserCommand = new UserCommand(userEntity);
	assertEquals(expectedUserCommand, actualUserCommand);
    }

    @Test
    public void testConvertUserEntityToUserEditProfileCommand() {
	replayAllMocks();
	UserEntity userEntity = createUserEntity();
	UserEditProfileCommand actualUserEditProfileCommand = new UserEditProfileCommand(
		userEntity);

	UserEditProfileCommand expectedUserEditProfileCommand = creatUserEditProfileCommand();
	assertEquals(expectedUserEditProfileCommand,
		actualUserEditProfileCommand);
    }

    @Test
    public void testConvertUserCommandToUserEditProfileCommand() {
	replayAllMocks();
	UserCommand userCommand = createUserCommand();
	UserEditProfileCommand actualUserEditProfileCommand = new UserEditProfileCommand(
		userCommand);

	UserEditProfileCommand expectedUserEditProfileCommand1 = creatUserEditProfileCommand();
	assertEquals(expectedUserEditProfileCommand1,
		actualUserEditProfileCommand);
    }

    @Test
    public void testCreateUser() {
	UserCommand userCommand = createUserCommandWithPassword();
	UserEntity userEntity = new UserEntity(userCommand);
	userEntity.setPassword(ENCODED_PASSWORD);

	expect(userDaoMock.save(userEntity)).andReturn(userEntity);
	expect(passwordEncoder.encodePassword(PASSWORD, "")).andReturn(
		ENCODED_PASSWORD);
	replayAllMocks();

	UserCommand actualUserCommand = userService.createUser(userCommand);
	UserCommand expectedUserCommand = createUserCommand();
	expectedUserCommand.setId(actualUserCommand.getId());
	assertEquals(expectedUserCommand, actualUserCommand);
    }

    @Test
    public void testUpdateUser() {
	UserEntity oldUserInDb = createUserEntity();
	UserCommand userToUpdate = createCustomUserCommand(NEW_USER_ID, PASSWORD, NEW_EMAIL, NEW_NAME);

	expect(userDaoMock.getEntityById(USER_ID)).andReturn(oldUserInDb);
	Capture<UserEntity> userToSaveCapture = new Capture<UserEntity>();
	expect(userDaoMock.save(capture(userToSaveCapture))).andReturn(
		oldUserInDb);
	replayAllMocks();

	UserCommand expectedCommand = createCustomUserCommand(USER_ID, null, NEW_EMAIL, NEW_NAME);
	assertEquals(expectedCommand,
		userService.updateUser(USER_ID, userToUpdate));

	UserEntity actualSavedEntity = userToSaveCapture.getValue();
	assertEquals(NEW_EMAIL, actualSavedEntity.getEmail());
	assertEquals(NEW_NAME, actualSavedEntity.getName());
	assertEquals(USER_ID, actualSavedEntity.getId());
	assertEquals(PASSWORD, actualSavedEntity.getPassword());
    }

    @Test
    public void testUpdateUserEditProfile() {
	UserEntity userEntityToChange = createUserEntity();
	expect(userDaoMock.getEntityById(USER_ID))
		.andReturn(userEntityToChange);
	Capture<UserEntity> savedUserEntityCapture = new Capture<UserEntity>();
	expect(userDaoMock.save(capture(savedUserEntityCapture))).andReturn(
		userEntityToChange);
	replayAllMocks();

	UserEditProfileCommand changedUserProfile = createCustomUserEditProfileCommand(NEW_USER_ID, NEW_NAME, NEW_EMAIL);
	UserEditProfileCommand actualUserEditProfile = userService.updateUser(
		USER_ID, changedUserProfile);

	UserEntity savedUserEntity = savedUserEntityCapture.getValue();
	assertEquals(USER_ID, savedUserEntity.getId());
	assertEquals(PASSWORD, savedUserEntity.getPassword());
	assertEquals(NEW_NAME, savedUserEntity.getName());
	assertEquals(NEW_EMAIL, savedUserEntity.getEmail());

	UserEditProfileCommand expectedUserProfile = createCustomUserEditProfileCommand(USER_ID, NEW_NAME, NEW_EMAIL);
	assertEquals(expectedUserProfile, actualUserEditProfile);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUserUpdateWhenUserDoesNotExist() {
	expect(userDaoMock.getEntityById(NEW_USER_ID)).andThrow(
		new EntityNotFoundException());
	replayAllMocks();
	userService.updateUser(NEW_USER_ID, createUserCommand());
    }

    @Test
    public void testGetUserById() {
	UserEntity userEntity = createUserEntity();
	expect(userDaoMock.getEntityById(USER_ID)).andReturn(userEntity);
	replayAllMocks();

	UserCommand actualUserCommand = userService.getUserById(USER_ID);
	UserCommand expectedUserCommand = createUserCommand();
	assertEquals(expectedUserCommand, actualUserCommand);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetUserByIdWhenUserDoesNotExist() {
	expect(userDaoMock.getEntityById(NEW_USER_ID)).andThrow(
		new EntityNotFoundException());
	replayAllMocks();

	userService.getUserById(NEW_USER_ID);
    }

    @Test
    public void testGetUserByUsername() {
        UserEntity userEntity = createUserEntity();
        expect(userDaoMock.getUserByEmail(USER_NAME)).andReturn(userEntity);
        replayAllMocks();
        UserCommand expectedUserCommand = createUserCommand();
        UserCommand actualUserCommand = userService.getUserByEmail(USER_NAME);
        assertEquals(expectedUserCommand, actualUserCommand);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetUserByUsernameWhenUserDoesNotExist() {
        expect(userDaoMock.getUserByEmail(FAKE_USER_NAME)).andThrow(new EntityNotFoundException());
        replayAllMocks();
        userService.getUserByEmail(FAKE_USER_NAME);
    }

    @After
    public void verifyAllMocks() {
	verify(userDaoMock, passwordEncoder);
    }

    private UserCommand createCustomUserCommand(int id, String password, String email, String name) {
	UserCommand command = new UserCommand();
	command.setId(id);
	command.setConfirmPassword(password);
	command.setPassword(password);
	command.setEmail(email);
	command.setName(name);
	return command;
    }

    private UserCommand createUserCommand() {
	UserCommand command = createCustomUserCommand(USER_ID, null, EMAIL, NAME);
	return command;
    }

    private UserEditProfileCommand creatUserEditProfileCommand() {
	return createCustomUserEditProfileCommand(USER_ID, NAME, EMAIL);
    }

    private UserEditProfileCommand createCustomUserEditProfileCommand(int id, String name, String email) {
	UserEditProfileCommand userEditProfileCommand = new UserEditProfileCommand();
	userEditProfileCommand.setId(id);
	userEditProfileCommand.setName(name);
	userEditProfileCommand.setEmail(email);
	return userEditProfileCommand;
    }

    private UserCommand createUserCommandWithPassword() {
	UserCommand res = createUserCommand();
	res.setPassword(PASSWORD);
	res.setConfirmPassword(PASSWORD);
	return res;
    }

    private UserEntity createUserEntity() {
	UserEntity userEntity = new UserEntity();
	userEntity.setId(USER_ID);
	userEntity.setPassword(PASSWORD);
	userEntity.setEmail(EMAIL);
	userEntity.setName(NAME);
	return userEntity;
    }
}
