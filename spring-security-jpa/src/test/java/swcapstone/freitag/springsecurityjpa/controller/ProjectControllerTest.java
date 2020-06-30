package swcapstone.freitag.springsecurityjpa.controller;

import org.apache.http.client.utils.URIBuilder;
import org.junit.Before;
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
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.utils.Repositories;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static swcapstone.freitag.springsecurityjpa.utils.Common.*;

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
        ProjectEntity fixtureProjectEntity = makeEmptyProjectEntity();
        fixtureProjectEntity.setProjectName("동물 사진 수집");
        fixtureProjectEntity.setWorkType("collection");
        fixtureProjectEntity.setDataType("image");
        fixtureProjectEntity.setSubject("동물");
        fixtureProjectEntity.setWayContent("같은 종류의 동물만 있는 사진을 올려주세요.");
        fixtureProjectEntity.setConditionContent("다음 중 해당하는 동물이 무엇인지 선택해주세요. 한 사진에 여러 동물이 있으면 없음을 선택해주세요.");
        fixtureProjectEntity.setDescription("야생 동물 경보기 제작을 위한 프로젝트입니다.");
        fixtureProjectEntity.setTotalData(10);

        ProjectEntity expectedProejctEntity = copyProjectEntity(fixtureProjectEntity);
        expectedProejctEntity.setUserId(requesterUserId);
        expectedProejctEntity.setStatus("없음");
        expectedProejctEntity.setDifficulty(0);
        expectedProejctEntity.setExampleContent("없음");
        expectedProejctEntity.setProgressData(0);
        expectedProejctEntity.setValidatedData(0);
        expectedProejctEntity.setCost(0);

        // Exercise SUT
        ResultActions result = performCreateProject(authorization, fixtureProjectEntity);

        // Verify Outcome
        result.andExpect(header().string("create", "success"))
                .andExpect(header().exists("projectId"));

        int receivedProjectId = Integer.parseInt(result.andReturn().getResponse().getHeader("projectId"));

        ProjectEntity actualProjectEntity = repositories.getProjectEntity(receivedProjectId);

        assertProjectEquals(expectedProejctEntity, actualProjectEntity);
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
        System.err.println(uri);
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
}