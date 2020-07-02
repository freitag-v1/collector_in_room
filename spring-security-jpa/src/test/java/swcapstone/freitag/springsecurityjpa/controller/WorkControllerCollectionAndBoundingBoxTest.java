package swcapstone.freitag.springsecurityjpa.controller;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.utils.Repositories;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static swcapstone.freitag.springsecurityjpa.utils.Common.makeValidAuthorizationToken;
import static swcapstone.freitag.springsecurityjpa.utils.Fixture.copyProjectEntity;
import static swcapstone.freitag.springsecurityjpa.utils.Fixture.makeEmptyProblemEntity;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
public class WorkControllerCollectionAndBoundingBoxTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Repositories repositories;

    private static final String worker = "worker";

    private static final String data = "data";
    private static final String bucketName = "freitag-test";

    private static final int collectionProject = 1;
    private static final int boundingBoxProject = 2;
    private static final int boundingBoxHistoryId = 1;
    private static final int boundingBoxProblemId = 11;

    @BeforeAll
    static void setupSharedFixture(@Autowired DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("WorktControllerCollectionAndBoundingBoxPrebuiltFixture.sql"));
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Test
    public void successfulCollection() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(worker);

        File fixtureCollectionFile = new ClassPathResource(data).getFile().listFiles()[0];
        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(collectionProject);

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        expectedProjectEntity.setProgressData(fixtureProjectEntity.getProgressData() + 1);

        // Exercise SUT
        ResultActions result = performCollection(authorization, fixtureCollectionFile, collectionProject, bucketName);

        // Verify Outcome
        result.andExpect(header().string("upload", "success"));

        ProjectEntity actualProjectEntity = repositories.getProjectEntity(collectionProject);
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void collectionTooMuch() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(worker);

        List<File> fixtureCollectionFileList = Arrays.asList(new ClassPathResource(data).getFile().listFiles());
        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(collectionProject);

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);

        // Exercise SUT
        ResultActions result = performCollection(authorization, fixtureCollectionFileList, collectionProject, bucketName);

        // Verify Outcome
        result.andExpect(header().string("upload", "fail"));

        ProjectEntity actualProjectEntity = repositories.getProjectEntity(collectionProject);
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void collectionAlreadyDone() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(worker);

        File fixtureCollectionFile = new ClassPathResource(data).getFile().listFiles()[0];
        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(collectionProject);
        fixtureProjectEntity.setProgressData(fixtureProjectEntity.getTotalData());

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);

        // Exercise SUT
        ResultActions result = performCollection(authorization, fixtureCollectionFile, collectionProject, bucketName);

        // Verify Outcome
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(collectionProject);
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void successfulBoundingBoxStart() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(worker);

        // Exercise SUT
        ResultActions result = performBoundingBoxStart(authorization, boundingBoxProject);

        // Verify Outcome
        result.andExpect(header().string("problems", "success"));
    }

    @Test
    public void successfulOneClassOneBoundingBox() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(worker);

        Map<String, String> fixtureBoundingBox = new HashMap<>();
        fixtureBoundingBox.put("1", makeRandomBoundingBox());

        String expectedValidationStatus = "작업후";

        // Exercise SUT
        ResultActions result = performBoundingBox(authorization, boundingBoxProject, boundingBoxHistoryId, boundingBoxProblemId, fixtureBoundingBox);

        // Verify Outcome
        result.andExpect(header().string("answer", "success"));

        ProblemEntity actualProblemEntity = repositories.getProblemEntity(boundingBoxProblemId);
        assertEquals(expectedValidationStatus, actualProblemEntity.getValidationStatus());
    }

    @Test
    public void successfulOneClassTwoBoundingBox() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(worker);

        Map<String, String> fixtureBoundingBox = new HashMap<>();
        fixtureBoundingBox.put("1", makeRandomBoundingBox() + "&" + makeRandomBoundingBox());

        String expectedValidationStatus = "작업후";

        // Exercise SUT
        ResultActions result = performBoundingBox(authorization, boundingBoxProject, boundingBoxHistoryId, boundingBoxProblemId, fixtureBoundingBox);

        // Verify Outcome
        result.andExpect(header().string("answer", "success"));

        ProblemEntity actualProblemEntity = repositories.getProblemEntity(boundingBoxProblemId);
        assertEquals(expectedValidationStatus, actualProblemEntity.getValidationStatus());
    }

    @Test
    public void successfulTwoClassOneBoundingBox() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(worker);

        Map<String, String> fixtureBoundingBox = new HashMap<>();
        fixtureBoundingBox.put("1", makeRandomBoundingBox());
        fixtureBoundingBox.put("2", makeRandomBoundingBox());

        String expectedValidationStatus = "작업후";

        // Exercise SUT
        ResultActions result = performBoundingBox(authorization, boundingBoxProject, boundingBoxHistoryId, boundingBoxProblemId, fixtureBoundingBox);

        // Verify Outcome
        result.andExpect(header().string("answer", "success"));

        ProblemEntity actualProblemEntity = repositories.getProblemEntity(boundingBoxProblemId);
        assertEquals(expectedValidationStatus, actualProblemEntity.getValidationStatus());
    }

    @Test
    public void successfulTwoClassTwoBoundingBox() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(worker);

        Map<String, String> fixtureBoundingBox = new HashMap<>();
        fixtureBoundingBox.put("1", makeRandomBoundingBox() + "&" + makeRandomBoundingBox());
        fixtureBoundingBox.put("2", makeRandomBoundingBox() + "&" + makeRandomBoundingBox());

        String expectedValidationStatus = "작업후";

        // Exercise SUT
        ResultActions result = performBoundingBox(authorization, boundingBoxProject, boundingBoxHistoryId, boundingBoxProblemId, fixtureBoundingBox);

        // Verify Outcome
        result.andExpect(header().string("answer", "success"));

        ProblemEntity actualProblemEntity = repositories.getProblemEntity(boundingBoxProblemId);
        assertEquals(expectedValidationStatus, actualProblemEntity.getValidationStatus());
    }

    @Test
    public void successfulCancle() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(worker);

        String expectedValidationStatus = "작업전";

        // Exercise SUT
        ResultActions result = performCancleWork(authorization, boundingBoxHistoryId);

        // Verify Outcome
        ProblemEntity actualProblemEntity = repositories.getProblemEntity(boundingBoxProblemId);
        assertEquals(expectedValidationStatus, actualProblemEntity.getValidationStatus());
    }

    private ResultActions performCancleWork(String authorization, int historyId) throws Exception {
        String uri = new URIBuilder("/api/work/cancel")
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockMultipartHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(uri);
        request.header("workHistory", historyId);
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performCollection(String authorization, File fixtureCollectionFile, int projectId, String bucketName) throws Exception {
        String uri = new URIBuilder("/api/work/collection")
                .addParameter("className", "없음")
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockMultipartHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(uri);
        request.file(new MockMultipartFile("files", fixtureCollectionFile.getName(), "", new FileInputStream(fixtureCollectionFile)));
        request.header("projectId", projectId);
        request.header("bucketName", bucketName);
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performCollection(String authorization, List<File> fixtureCollectionFileList, int projectId, String bucketName) throws Exception {
        String uri = new URIBuilder("/api/work/collection")
                .addParameter("className", "없음")
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockMultipartHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(uri);
        for (File fixtureCollectionFile : fixtureCollectionFileList) {
            request.file(new MockMultipartFile("files", fixtureCollectionFile.getName(), "", new FileInputStream(fixtureCollectionFile)));
        }
        request.header("projectId", projectId);
        request.header("bucketName", bucketName);
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performBoundingBoxStart(String authorization, int projectId) throws Exception {
        String uri = new URIBuilder("/api/work/boundingbox/start")
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(uri);
        request.header("projectId", projectId);
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performBoundingBox(String authorization, int projectId, int historyId, int problemId, Map<String, String> fixtureBoundingBox) throws Exception {
        String uri = new URIBuilder("/api/work/boundingbox")
                .addParameter("problemId", String.valueOf(problemId))
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(uri);
        request.header("projectId", projectId);
        request.header("historyId", historyId);
        request.content(new JSONObject(fixtureBoundingBox).toString());
        request.contentType("application/json");
        if (authorization != null) {
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
        assertEquals(expectedProejctEntity.getDifficulty(), actualProjectEntity.getDifficulty(), 0.01);
        assertEquals(expectedProejctEntity.getWayContent(), actualProjectEntity.getWayContent());
        assertEquals(expectedProejctEntity.getConditionContent(), actualProjectEntity.getConditionContent());
        assertEquals(expectedProejctEntity.getExampleContent(), actualProjectEntity.getExampleContent());
        assertEquals(expectedProejctEntity.getDescription(), actualProjectEntity.getDescription());
        assertEquals(expectedProejctEntity.getTotalData(), actualProjectEntity.getTotalData());
        assertEquals(expectedProejctEntity.getProgressData(), actualProjectEntity.getProgressData());
        assertEquals(expectedProejctEntity.getValidatedData(), actualProjectEntity.getValidatedData());
        assertEquals(expectedProejctEntity.getCost(), actualProjectEntity.getCost());
    }

    private String makeRandomBoundingBox() {
        StringBuilder fixtureRandomBoundingBoxBuilder = new StringBuilder();
        fixtureRandomBoundingBoxBuilder.append(String.format("%.2f", Math.random()));
        for(int i = 0; i < 3; i++) {
            fixtureRandomBoundingBoxBuilder.append(" ");
            fixtureRandomBoundingBoxBuilder.append(String.format("%.2f", Math.random()));
        }
        return fixtureRandomBoundingBoxBuilder.toString();
    }
}