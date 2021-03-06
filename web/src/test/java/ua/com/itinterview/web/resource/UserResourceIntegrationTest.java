package ua.com.itinterview.web.resource;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import ua.com.itinterview.dao.UserDao;
import ua.com.itinterview.entity.UserEntity;
import ua.com.itinterview.web.integration.BaseWebIntegrationTest;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserResourceIntegrationTest extends BaseWebIntegrationTest{

    @Autowired
    private UserDao userDao;

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-empty.xml")
    @ExpectedDatabase(
            value = "file:src/test/resources/dataset/UserResource/users-empty.xml",
            assertionMode = DatabaseAssertionMode.NON_STRICT
    )
    public void testRegisterUserWithInvalidConfirmPasswordEmail()
            throws Exception {
        mvc.perform(
                registerUser(NAME, INVALID_EMAIL, PASSWORD, PASSWORD_ANOTHER))
                .andExpect(model().hasErrors())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeErrorCount("userCommand", 2))
                .andExpect(model().attributeHasFieldErrors("userCommand", "email", "confirmPassword"))
                .andExpect(status().isOk()).andDo(print());
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-empty.xml")
    public void testMoveToRegisterIfAuthenticatedRequired() throws Exception {
        String[] authenticatedRequiredUrls = {"/user/0/view", "/user/0/edit"};
        for (String url : authenticatedRequiredUrls) {
            mvc.perform(get(url))
                    .andExpect(redirectedUrl("http://localhost/register"))
                    .andExpect(status().isMovedTemporarily());
        }
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-empty.xml")
    @ExpectedDatabase(
            value = "file:src/test/resources/dataset/UserResource/users-testRegisterUser.xml",
            assertionMode = DatabaseAssertionMode.NON_STRICT
    )
    public void testRegisterUser() throws Exception {
        ResultActions requestActions = mvc.perform(registerUser(NAME, EMAIL, PASSWORD, PASSWORD));
        String userId = String.valueOf(userDao.getUserByEmail(EMAIL).getId());
        requestActions.andExpect(model().hasNoErrors())
                .andExpect(redirectedUrl("/user/" + userId + "/view"))
                .andExpect(status().isMovedTemporarily());
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-initial.xml")
    public void testRegisterUserWithEmailAlreadyExists() throws Exception {
        mvc.perform(
                registerUser(NAME, EMAIL, PASSWORD, PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrors("userCommand", "email"));
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-initial.xml")
    public void testRedirectAfterLogin() throws Exception {
        UserEntity user = userDao.getUserByEmail(EMAIL);
        String userProfileUrl = "/user/" + user.getId() + "/view";
        mvc.perform(loginUser(EMAIL, PASSWORD))
                .andExpect(redirectedUrl(userProfileUrl))
                .andExpect(status().isMovedTemporarily());
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-initial.xml")
    public void testRedirectAfterLogout() throws Exception {
        ResultActions actions = mvc.perform(loginUser(EMAIL, PASSWORD));
        mvc.perform(logout(getHttpSession(actions)))
                .andExpect(redirectedUrl("/register"))
                .andExpect(status().isMovedTemporarily());
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-initial.xml")
    public void testReturnToTargetAfterSuccessLogin() throws Exception {
        UserEntity user = userDao.getUserByEmail(EMAIL);
        String userTargetUrl = "http://localhost/user/" + user.getId() + "/edit";

        ResultActions actions = mvc.perform(get(userTargetUrl))
                .andExpect(redirectedUrl("http://localhost/register"))
                .andExpect(status().isMovedTemporarily());

        mvc.perform(loginUser(EMAIL, PASSWORD)
                .session(getHttpSession(actions))).andExpect(redirectedUrl(userTargetUrl));
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-initial.xml")
    public void testEditProfileChangeOnlyName() throws Exception {
        UserEntity user = userDao.getUserByEmail(EMAIL);
        String userSaveProfileUrl = "/user/" + user.getId() + "/save";
        String userProfileUrl = "/user/" + user.getId() + "/view";
        ResultActions actions = mvc.perform(loginUser(EMAIL, PASSWORD));

        mvc.perform(post(userSaveProfileUrl).session(getHttpSession(actions))
                .param("name", NAME_ANOTHER)
                .param("email", EMAIL))
                .andExpect(model().hasNoErrors())
                .andExpect(redirectedUrl(userProfileUrl));

        UserEntity modifiedUser = userDao.getUserByEmail(EMAIL);
        assertEquals(NAME_ANOTHER, modifiedUser.getName());
        assertEquals(EMAIL, modifiedUser.getEmail());
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-initial.xml")
    public void testEditProfileChangeOnlyEmail() throws Exception {
        UserEntity user = userDao.getUserByEmail(EMAIL);
        String userSaveProfileUrl = "/user/" + user.getId() + "/save";
        String userProfileUrl = "/user/" + user.getId() + "/view";
        ResultActions actions = mvc.perform(loginUser(EMAIL, PASSWORD));

        mvc.perform(post(userSaveProfileUrl).session(getHttpSession(actions))
                .param("name", NAME)
                .param("email", EMAIL_ANOTHER))
                .andExpect(model().hasNoErrors())
                .andExpect(redirectedUrl(userProfileUrl));

        UserEntity modifiedUser = userDao.getUserByEmail(EMAIL_ANOTHER);
        assertEquals(NAME, modifiedUser.getName());
        assertEquals(EMAIL_ANOTHER, modifiedUser.getEmail());
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-initial.xml")
    public void testEditProfileChangeOnlySex() throws Exception {
        UserEntity user = userDao.getUserByEmail(EMAIL);
        String userSaveProfileUrl = "/user/" + user.getId() + "/save";
        String userProfileUrl = "/user/" + user.getId() + "/view";
        ResultActions actions = mvc.perform(loginUser(EMAIL, PASSWORD));

        mvc.perform(post(userSaveProfileUrl).session(getHttpSession(actions))
                .param("name", NAME)
                .param("email", EMAIL))
                .andExpect(model().hasNoErrors())
                .andExpect(redirectedUrl(userProfileUrl));

        UserEntity modifiedUser = userDao.getUserByEmail(EMAIL);
        assertEquals(NAME, modifiedUser.getName());
        assertEquals(EMAIL, modifiedUser.getEmail());
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-initial.xml")
    public void testEditProfileChangeAll() throws Exception {
        UserEntity user = userDao.getUserByEmail(EMAIL);
        String userSaveProfileUrl = "/user/" + user.getId() + "/save";
        String userProfileUrl = "/user/" + user.getId() + "/view";
        ResultActions actions = mvc.perform(loginUser(EMAIL, PASSWORD));

        mvc.perform(post(userSaveProfileUrl).session(getHttpSession(actions))
                .param("name", NAME_ANOTHER)
                .param("email", EMAIL_ANOTHER))
                .andExpect(model().hasNoErrors())
                .andExpect(redirectedUrl(userProfileUrl));

        UserEntity modifiedUser = userDao.getUserByEmail(EMAIL_ANOTHER);
        assertEquals(NAME_ANOTHER, modifiedUser.getName());
        assertEquals(EMAIL_ANOTHER, modifiedUser.getEmail());
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-initial.xml")
    public void testEditProfileInvalidEmail() throws Exception {
        UserEntity user = userDao.getUserByEmail(EMAIL);
        String userSaveProfileUrl = "/user/" + user.getId() + "/save";
        ResultActions actions = mvc.perform(loginUser(EMAIL, PASSWORD));

        mvc.perform(post(userSaveProfileUrl).session(getHttpSession(actions))
                .param("name", NAME)
                .param("email", INVALID_EMAIL))
                .andExpect(view().name("profile_page"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeErrorCount("userEditProfileCommand", 1))
                .andExpect(model().attributeHasFieldErrors("userEditProfileCommand", "email"))
                .andExpect(status().isOk()).andDo(print());

        UserEntity modifiedUser = userDao.getUserByEmail(EMAIL);
        assertEquals(NAME, modifiedUser.getName());
        assertEquals(EMAIL, modifiedUser.getEmail());
    }

    @Test
    @DatabaseSetup("file:src/test/resources/dataset/UserResource/users-initial.xml")
    public void testEditProfileAllWrong() throws Exception {
        UserEntity user = userDao.getUserByEmail(EMAIL);
        String userSaveProfileUrl = "/user/" + user.getId() + "/save";
        ResultActions actions = mvc.perform(loginUser(EMAIL, PASSWORD));

        mvc.perform(post(userSaveProfileUrl).session(getHttpSession(actions))
                .param("name", "")
                .param("email", ""))
                .andExpect(view().name("profile_page"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeErrorCount("userEditProfileCommand", 2))
                .andExpect(model().attributeHasFieldErrors("userEditProfileCommand", "name", "email"))
                .andExpect(status().isOk()).andDo(print());

        UserEntity modifiedUser = userDao.getUserByEmail(EMAIL);
        assertEquals(NAME, modifiedUser.getName());
        assertEquals(EMAIL, modifiedUser.getEmail());
    }
}
