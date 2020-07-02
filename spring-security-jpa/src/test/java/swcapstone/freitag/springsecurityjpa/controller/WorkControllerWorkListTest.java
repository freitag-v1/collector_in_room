package swcapstone.freitag.springsecurityjpa.controller;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.utils.Common;
import swcapstone.freitag.springsecurityjpa.utils.Fixture;
import swcapstone.freitag.springsecurityjpa.utils.Repositories;

import javax.sql.DataSource;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static swcapstone.freitag.springsecurityjpa.utils.Common.makeValidAuthorizationToken;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
public class WorkControllerWorkListTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Repositories repositories;

    @BeforeAll
    static void setupSharedFixture(@Autowired DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("ProjectAndWorkControllerPrebuiltFixture.sql"));
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Test
    public void successfulMyWorkList1() throws Exception {
        // Setup Fixture
        String userId = "high_user1";
        String authorization = makeValidAuthorizationToken(userId);

        int expectedWorkListSize = 4;

        // Exercise SUT
        ResultActions result = performCreateProject(authorization);

        // Verify Outcome
        result.andExpect(header().string("workList", "success"));

        String contentAsString = result.andReturn().getResponse().getContentAsString();
        JSONArray actualWorkList = new JSONArray(contentAsString);

        assertEquals(expectedWorkListSize, actualWorkList.length());
    }

    @Test
    public void successfulMyWorkList2() throws Exception {
        // Setup Fixture
        String userId = "high_user2";
        String authorization = makeValidAuthorizationToken(userId);

        int expectedWorkListSize = 12;

        // Exercise SUT
        ResultActions result = performCreateProject(authorization);

        // Verify Outcome
        result.andExpect(header().string("workList", "success"));

        String contentAsString = result.andReturn().getResponse().getContentAsString();
        JSONArray actualWorkList = new JSONArray(contentAsString);

        assertEquals(expectedWorkListSize, actualWorkList.length());
    }

    @Test
    public void successfulMyWorkList3() throws Exception {
        // Setup Fixture
        String userId = "middle_user1";
        String authorization = makeValidAuthorizationToken(userId);

        int expectedWorkListSize = 5;

        // Exercise SUT
        ResultActions result = performCreateProject(authorization);

        // Verify Outcome
        result.andExpect(header().string("workList", "success"));

        String contentAsString = result.andReturn().getResponse().getContentAsString();
        JSONArray actualWorkList = new JSONArray(contentAsString);

        assertEquals(expectedWorkListSize, actualWorkList.length());
    }

    @Test
    public void successfulMyWorkList4() throws Exception {
        // Setup Fixture
        String userId = "middle_user2";
        String authorization = makeValidAuthorizationToken(userId);

        int expectedWorkListSize = 4;

        // Exercise SUT
        ResultActions result = performCreateProject(authorization);

        // Verify Outcome
        result.andExpect(header().string("workList", "success"));

        String contentAsString = result.andReturn().getResponse().getContentAsString();
        JSONArray actualWorkList = new JSONArray(contentAsString);

        assertEquals(expectedWorkListSize, actualWorkList.length());
    }

    @Test
    public void successfulMyWorkList5() throws Exception {
        // Setup Fixture
        String userId = "low_user1";
        String authorization = makeValidAuthorizationToken(userId);

        int expectedWorkListSize = 4;

        // Exercise SUT
        ResultActions result = performCreateProject(authorization);

        // Verify Outcome
        result.andExpect(header().string("workList", "success"));

        String contentAsString = result.andReturn().getResponse().getContentAsString();
        JSONArray actualWorkList = new JSONArray(contentAsString);

        assertEquals(expectedWorkListSize, actualWorkList.length());
    }

    @Test
    public void myWorkListByNewbie() throws Exception {
        // Setup Fixture
        String userId = "newbie_user";
        String authorization = makeValidAuthorizationToken(userId);
        String userPassword = Common.SHA256("1234");
        UserEntity fixtureUserEntity = Fixture.makeEmptyUserEntity();
        repositories.saveUserEntity(fixtureUserEntity);

        // Exercise SUT
        ResultActions result = performCreateProject(authorization);

        // Verify Outcome
        result.andExpect(header().string("workList", "fail"));
    }

    @Test
    public void myWorkListWithoutLogin() throws Exception {
        // Setup Fixture
        String authorization = null;

        // Exercise SUT
        ResultActions result = performCreateProject(authorization);

        // Verify Outcome
        result.andExpect(header().string("workList", "fail"));
    }

    private ResultActions performCreateProject(String authorization) throws Exception {
        String uri = new URIBuilder("/api/work/all")
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }
}