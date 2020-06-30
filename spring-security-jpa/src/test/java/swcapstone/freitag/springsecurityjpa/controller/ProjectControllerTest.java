package swcapstone.freitag.springsecurityjpa.controller;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.BeforeAll;
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
import swcapstone.freitag.springsecurityjpa.domain.entity.ClassEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.utils.Repositories;

import javax.sql.DataSource;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static swcapstone.freitag.springsecurityjpa.utils.Common.makeValidAuthorizationToken;
import static swcapstone.freitag.springsecurityjpa.utils.Fixture.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class ProjectControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Repositories repositories;

    private final String requesterUserId = "requester";
    private final String workerUserId = "worker";

    @BeforeAll
    static void setupSharedFixture(@Autowired DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("ProjectControllerPrebuiltFixture.sql"));
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Test
    public void successfulProjectCreation() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);
        ProjectEntity fixtureProjectEntity = getFixtureCollectionProjectEntity();

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        expectedAfterProjectCreation(requesterUserId, expectedProjectEntity);

        // Exercise SUT
        ResultActions result = performCreateProject(authorization, fixtureProjectEntity);

        // Verify Outcome
        result.andExpect(header().string("create", "success"))
                .andExpect(header().exists("projectId"));

        int receivedProjectId = Integer.parseInt(result.andReturn().getResponse().getHeader("projectId"));

        ProjectEntity actualProjectEntity = repositories.getProjectEntity(receivedProjectId);

        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void projectCreationWithoutLogin() throws Exception {
        // Setup Fixture
        String authorization = null;
        ProjectEntity fixtureProjectEntity = getFixtureCollectionProjectEntity();

        // Exercise SUT
        ResultActions result = performCreateProject(authorization, fixtureProjectEntity);

        // Verify Outcome
        result.andExpect(header().doesNotExist("create"))
                .andExpect(header().doesNotExist("projectId"));
    }

    @Test
    public void successfulClassCreation() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureCollectionProjectEntity();
        expectedAfterProjectCreation(requesterUserId, fixtureProjectEntity);
        fixtureProjectEntity.setProjectId(1);
        fixtureProjectEntity.setBucketName("test");
        repositories.saveProjectEntity(fixtureProjectEntity);

        repositories.deletaAllClass();
        List<String> fixtureClassList = getFixtureClassList();

        List<String> expectedClassList = fixtureClassList;

        // Exercise SUT
        ResultActions result = performCreateClass(authorization, fixtureClassList);

        // Verify Outcome
        result.andExpect(header().string("class", "success"))
                .andExpect(header().exists("bucketName"));

        List<ClassEntity> actualClassEntityList = repositories.getClassEntityList(fixtureProjectEntity.getProjectId());

        assertClassListEquals(expectedClassList, actualClassEntityList);
    }

    @Test
    public void classCreationWithoutInput() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureCollectionProjectEntity();
        expectedAfterProjectCreation(requesterUserId, fixtureProjectEntity);
        fixtureProjectEntity.setProjectId(1);
        fixtureProjectEntity.setBucketName("test");
        repositories.saveProjectEntity(fixtureProjectEntity);

        repositories.deletaAllClass();
        List<String> fixtureClassList = new ArrayList<>();

        // Exercise SUT
        ResultActions result = performCreateClass(authorization, fixtureClassList);

        // Verify Outcome
        result.andExpect(header().string("class", "fail"))
                .andExpect(header().doesNotExist("bucketName"));

        List<ClassEntity> actualClassEntityList = repositories.getClassEntityList(fixtureProjectEntity.getProjectId());

        assertTrue(actualClassEntityList.isEmpty());
    }

    private ResultActions performCreateProject(String authorization, ProjectEntity fixtureProjectEntity) throws Exception {
        String uri = new URIBuilder("/api/project/create")
                .addParameter("projectName", fixtureProjectEntity.getProjectName())
                .addParameter("workType", fixtureProjectEntity.getWorkType())
                .addParameter("dataType", fixtureProjectEntity.getDataType())
                .addParameter("subject", fixtureProjectEntity.getSubject())
                .addParameter("wayContent", fixtureProjectEntity.getWayContent())
                .addParameter("conditionContent", fixtureProjectEntity.getConditionContent())
                .addParameter("description", fixtureProjectEntity.getDescription())
                .addParameter("totalData", String.valueOf(fixtureProjectEntity.getTotalData()))
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performCreateClass(String authorization, List<String> fixtureClassList) throws Exception {
        URIBuilder uriBuilder = new URIBuilder("/api/project/class");
        for(String fixtureClass : fixtureClassList) {
            uriBuilder.addParameter("className", fixtureClass);
        }
        uriBuilder.addParameter("projectId", "1");
        String uri = uriBuilder.build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private void assertProjectEquals(ProjectEntity expectedProejctEntity, ProjectEntity actualProjectEntity) {
        assertEquals(expectedProejctEntity.getUserId(), actualProjectEntity.getUserId());
        assertEquals(expectedProejctEntity.getProjectName(), actualProjectEntity.getProjectName());
        assertEquals(expectedProejctEntity.getStatus(), actualProjectEntity.getStatus());
        assertEquals(expectedProejctEntity.getWorkType(), actualProjectEntity.getWorkType());
        assertEquals(expectedProejctEntity.getDataType(), actualProjectEntity.getDataType());
        assertEquals(expectedProejctEntity.getSubject(), actualProjectEntity.getSubject());
        assertEquals(expectedProejctEntity.getDifficulty(), actualProjectEntity.getDifficulty());
        assertEquals(expectedProejctEntity.getWayContent(), actualProjectEntity.getWayContent());
        assertEquals(expectedProejctEntity.getConditionContent(), actualProjectEntity.getConditionContent());
        assertEquals(expectedProejctEntity.getExampleContent(), actualProjectEntity.getExampleContent());
        assertEquals(expectedProejctEntity.getDescription(), actualProjectEntity.getDescription());
        assertEquals(expectedProejctEntity.getTotalData(), actualProjectEntity.getTotalData());
        assertEquals(expectedProejctEntity.getProgressData(), actualProjectEntity.getProgressData());
        assertEquals(expectedProejctEntity.getValidatedData(), actualProjectEntity.getValidatedData());
        assertEquals(expectedProejctEntity.getCost(), actualProjectEntity.getCost());
    }

    private void assertClassListEquals(List<String> expectedClassList, List<ClassEntity> actualClassEntityList) {
        for(ClassEntity classEntity : actualClassEntityList) {
            assertTrue(expectedClassList.contains(classEntity.getClassName()));
        }
    }
}