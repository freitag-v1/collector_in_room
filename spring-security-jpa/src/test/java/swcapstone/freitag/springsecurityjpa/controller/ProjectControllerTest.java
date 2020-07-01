package swcapstone.freitag.springsecurityjpa.controller;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.entity.ClassEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.utils.Repositories;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static swcapstone.freitag.springsecurityjpa.utils.Common.makeExpiredAuthorizationToken;
import static swcapstone.freitag.springsecurityjpa.utils.Common.makeValidAuthorizationToken;
import static swcapstone.freitag.springsecurityjpa.utils.Fixture.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
public
class ProjectControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Repositories repositories;
    @Autowired
    private ObjectStorageApiClient objectStorageApiClient;

    private final String requesterUserId = "requester";
    private final String workerUserId = "worker";
    private final String admin = "admin";
    private final int projectId = 1;
    private final String bucketName = "freitag-test";
    private final String prebuiltBucketName = "freitag-test-prebuilt";
    private final int prebuiltBucketSize = 15;
    private final String exampleFile = "example.jpg";
    private final String labellingData = "LabellingData";
    public static final int COST_PER_DATA = 50;
    public static final int COST_PER_DATA_NORMAL = 50;
    public static final int COST_PER_DATA_HARDER = 75;
    private static final int COST_PER_DATA_HARDEST = 100;

    @BeforeAll
    static void setupSharedFixture(@Autowired DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("ProjectControllerPrebuiltFixture.sql"));
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @BeforeEach
    void setUp() {
        objectStorageApiClient.deleteAllInBucket(bucketName);
    }

    @Test
    public void successfulProjectCreation() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);
        ProjectEntity fixtureProjectEntity = getFixtureImageCollectionProjectEntity();

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
        ProjectEntity fixtureProjectEntity = getFixtureImageCollectionProjectEntity();

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
        ProjectEntity fixtureProjectEntity = getFixtureImageCollectionProjectEntity();
        expectedAfterProjectCreation(requesterUserId, fixtureProjectEntity);
        fixtureProjectEntity.setProjectId(projectId);
        fixtureProjectEntity.setBucketName(bucketName);
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
        ProjectEntity fixtureProjectEntity = getFixtureImageCollectionProjectEntity();
        expectedAfterProjectCreation(requesterUserId, fixtureProjectEntity);
        fixtureProjectEntity.setProjectId(projectId);
        fixtureProjectEntity.setBucketName(bucketName);
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

    @Test
    public void successfulImageCollectionExampleUpload() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureImageCollectionProjectEntity();
        expectedAfterProjectCreation(requesterUserId, fixtureProjectEntity);
        fixtureProjectEntity.setProjectId(projectId);
        fixtureProjectEntity.setBucketName(bucketName);
        repositories.saveProjectEntity(fixtureProjectEntity);

        File fixtureExampleFile = new ClassPathResource(exampleFile).getFile();

        FileInputStream expectedExampleFile = new FileInputStream(fixtureExampleFile);
        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        expectedProjectEntity.setCost(fixtureProjectEntity.getTotalData() * COST_PER_DATA);
        expectedProjectEntity.setExampleContent(fixtureExampleFile.getName());

        // Exercise SUT
        ResultActions result = performUploadExample(authorization, fixtureProjectEntity, fixtureExampleFile);

        // Verify Outcome
        result.andExpect(header().string("example", "success"))
                .andExpect(header().exists("projectId"))
                .andExpect(header().exists("cost"))
                .andExpect(header().doesNotExist("bucketName"));

        S3ObjectInputStream actualExampleFile = objectStorageApiClient.getObject(bucketName, fixtureExampleFile.getName());
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertFileEquals(expectedExampleFile, actualExampleFile);
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void successfulImageClassificationExampleUpload() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureImageClassificationProjectEntity();
        expectedAfterProjectCreation(requesterUserId, fixtureProjectEntity);
        fixtureProjectEntity.setProjectId(projectId);
        fixtureProjectEntity.setBucketName(bucketName);
        repositories.saveProjectEntity(fixtureProjectEntity);

        File fixtureExampleFile = new ClassPathResource(exampleFile).getFile();

        FileInputStream expectedExampleFile = new FileInputStream(fixtureExampleFile);
        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        expectedProjectEntity.setExampleContent(fixtureExampleFile.getName());

        // Exercise SUT
        ResultActions result = performUploadExample(authorization, fixtureProjectEntity, fixtureExampleFile);

        // Verify Outcome
        result.andExpect(header().string("example", "success"))
                .andExpect(header().exists("projectId"))
                .andExpect(header().doesNotExist("cost"))
                .andExpect(header().exists("bucketName"));

        S3ObjectInputStream actualExampleFile = objectStorageApiClient.getObject(bucketName, fixtureExampleFile.getName());
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertFileEquals(expectedExampleFile, actualExampleFile);
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void successfulImageClassificationLabellingDataUpload() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureImageClassificationProjectEntity();
        expectedAfterImageClassificationProjectExampleUpload(requesterUserId, projectId, bucketName, exampleFile, fixtureProjectEntity);
        repositories.saveProjectEntity(fixtureProjectEntity);

        File[] fixtureLabellingFilesList = new ClassPathResource(labellingData).getFile().listFiles();

        Map<String, FileInputStream> expectedLabellingFilesList = new HashMap<>();
        for (File fixtureLabellingFile : fixtureLabellingFilesList) {
             expectedLabellingFilesList.put(fixtureLabellingFile.getName(), new FileInputStream(fixtureLabellingFile));
        }
        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        expectedProjectEntity.setTotalData(fixtureLabellingFilesList.length);
        expectedProjectEntity.setCost(fixtureLabellingFilesList.length * COST_PER_DATA);

        // Exercise SUT
        ResultActions result = performUploadLabellingData(authorization, fixtureProjectEntity, fixtureLabellingFilesList);

        // Verify Outcome
        result.andExpect(header().string("upload", "success"))
                .andExpect(header().exists("cost"));

        List<String> objectList = objectStorageApiClient.listObjects(bucketName);
        objectList.remove(exampleFile);
        Map<String, S3ObjectInputStream> actualLabellingDataList = new HashMap<>();
        for (String objectName : objectList) {
            actualLabellingDataList.put(objectName, objectStorageApiClient.getObject(bucketName, objectName));
        }
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertEquals(expectedLabellingFilesList.size(), actualLabellingDataList.size());
        for (String objectName : expectedLabellingFilesList.keySet()) {
            assertFileEquals(expectedLabellingFilesList.get(objectName), actualLabellingDataList.get(objectName));
        }
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void successfulImageCollectionProjectPaymentByAccount() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureImageCollectionProjectEntity();
        expectedAfterImageCollectionProjectExampleUpload(requesterUserId, projectId, bucketName, exampleFile, fixtureProjectEntity);
        repositories.saveProjectEntity(fixtureProjectEntity);

        repositories.deletaAllProblem();

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        expectedProjectEntity.setStatus("진행중");
        int expectedProblemListSize = fixtureProjectEntity.getTotalData();
        ProblemEntity expectedProblemEntity = expectedCollectionProblem(fixtureProjectEntity);

        // Exercise SUT
        ResultActions result = performPayByAccount(authorization, fixtureProjectEntity);

        // Verify Outcome
        result.andExpect(header().string("payment", "success"))
                .andExpect(header().doesNotExist("state"));

        List<ProblemEntity> actualProblemEntityList = repositories.getProblemEntityList(projectId);
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertEquals(expectedProblemListSize, actualProblemEntityList.size());
        for (ProblemEntity actualProblemEntity : actualProblemEntityList) {
            assertProblemEntityEquals(expectedProblemEntity, actualProblemEntity);
        }
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void successfulImageClassificationProjectPaymentByAccount() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureImageClassificationProjectEntity();
        expectedAfterImageClassificationProjectLabellingUpload(requesterUserId, projectId, prebuiltBucketName, prebuiltBucketSize, exampleFile, fixtureProjectEntity);
        repositories.saveProjectEntity(fixtureProjectEntity);

        repositories.deletaAllProblem();

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        expectedProjectEntity.setStatus("진행중");
        int expectedProblemListSize = fixtureProjectEntity.getTotalData();
        Map<String, ProblemEntity> expectedProblemEntityList = expectedLabellingProblemList(fixtureProjectEntity);

        // Exercise SUT
        ResultActions result = performPayByAccount(authorization, fixtureProjectEntity);

        // Verify Outcome
        result.andExpect(header().string("payment", "success"))
                .andExpect(header().doesNotExist("state"));

        List<ProblemEntity> actualProblemEntityList = repositories.getProblemEntityList(projectId);
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertEquals(expectedProblemListSize, actualProblemEntityList.size());
        for (ProblemEntity actualProblemEntity : actualProblemEntityList) {
            assertProblemEntityEquals(expectedProblemEntityList.get(actualProblemEntity.getObjectName()), actualProblemEntity);
        }
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void imageCollectionProjectPaymentByAccountWithoutRegisteringOpenBanking() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        UserEntity fixtureUserEntity = repositories.getFixtureUserEntity(requesterUserId);
        fixtureUserEntity.setUserOpenBankingAccessToken(UUID.randomUUID().toString().replace("-", ""));
        fixtureUserEntity.setUserOpenBankingNum(0);
        repositories.saveUserEntity(fixtureUserEntity);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureImageCollectionProjectEntity();
        expectedAfterImageCollectionProjectExampleUpload(requesterUserId, projectId, bucketName, exampleFile, fixtureProjectEntity);
        repositories.saveProjectEntity(fixtureProjectEntity);

        repositories.deletaAllProblem();

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        int expectedProblemListSize = 0;

        // Exercise SUT
        ResultActions result = performPayByAccount(authorization, fixtureProjectEntity);

        // Verify Outcome
        result.andExpect(header().string("payment", "fail"))
                .andExpect(header().exists("state"));

        List<ProblemEntity> actualProblemEntityList = repositories.getProblemEntityList(projectId);
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertEquals(expectedProblemListSize, actualProblemEntityList.size());
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void imageCollectionProjectPaymentByAccountButOpenBankingError() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        UserEntity fixtureUserEntity = repositories.getFixtureUserEntity(requesterUserId);
        fixtureUserEntity.setUserOpenBankingAccessToken(makeExpiredAuthorizationToken(requesterUserId));
        repositories.saveUserEntity(fixtureUserEntity);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureImageCollectionProjectEntity();
        expectedAfterImageCollectionProjectExampleUpload(requesterUserId, projectId, bucketName, exampleFile, fixtureProjectEntity);
        repositories.saveProjectEntity(fixtureProjectEntity);

        repositories.deletaAllProblem();

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        int expectedProblemListSize = 0;

        // Exercise SUT
        ResultActions result = performPayByAccount(authorization, fixtureProjectEntity);

        // Verify Outcome
        result.andExpect(header().string("payment", "fail"))
                .andExpect(header().doesNotExist("state"));

        List<ProblemEntity> actualProblemEntityList = repositories.getProblemEntityList(projectId);
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertEquals(expectedProblemListSize, actualProblemEntityList.size());
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void successfulImageCollectionProjectPaymentByPoint() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureImageCollectionProjectEntity();
        expectedAfterImageCollectionProjectExampleUpload(requesterUserId, projectId, bucketName, exampleFile, fixtureProjectEntity);
        repositories.saveProjectEntity(fixtureProjectEntity);

        repositories.deletaAllProblem();

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        expectedProjectEntity.setStatus("진행중");
        int expectedProblemListSize = fixtureProjectEntity.getTotalData();
        ProblemEntity expectedProblemEntity = expectedCollectionProblem(fixtureProjectEntity);

        // Exercise SUT
        ResultActions result = performPayByPoint(authorization, fixtureProjectEntity);

        // Verify Outcome
        result.andExpect(header().string("payment", "success"));

        List<ProblemEntity> actualProblemEntityList = repositories.getProblemEntityList(projectId);
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertEquals(expectedProblemListSize, actualProblemEntityList.size());
        for (ProblemEntity actualProblemEntity : actualProblemEntityList) {
            assertProblemEntityEquals(expectedProblemEntity, actualProblemEntity);
        }
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void successfulImageClassificationProjectPaymentByPoint() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureImageClassificationProjectEntity();
        expectedAfterImageClassificationProjectLabellingUpload(requesterUserId, projectId, prebuiltBucketName, prebuiltBucketSize, exampleFile, fixtureProjectEntity);
        repositories.saveProjectEntity(fixtureProjectEntity);

        repositories.deletaAllProblem();

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        expectedProjectEntity.setStatus("진행중");
        int expectedProblemListSize = fixtureProjectEntity.getTotalData();
        Map<String, ProblemEntity> expectedProblemEntityList = expectedLabellingProblemList(fixtureProjectEntity);

        // Exercise SUT
        ResultActions result = performPayByPoint(authorization, fixtureProjectEntity);

        // Verify Outcome
        result.andExpect(header().string("payment", "success"));

        List<ProblemEntity> actualProblemEntityList = repositories.getProblemEntityList(projectId);
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertEquals(expectedProblemListSize, actualProblemEntityList.size());
        for (ProblemEntity actualProblemEntity : actualProblemEntityList) {
            assertProblemEntityEquals(expectedProblemEntityList.get(actualProblemEntity.getObjectName()), actualProblemEntity);
        }
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void imageCollectionProjectPaymentByPointButNotEnoughPoint() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(requesterUserId);

        UserEntity fixtureUserEntity = repositories.getFixtureUserEntity(requesterUserId);
        fixtureUserEntity.setPoint(0);
        repositories.saveUserEntity(fixtureUserEntity);

        repositories.deletaAllProject();
        ProjectEntity fixtureProjectEntity = getFixtureImageCollectionProjectEntity();
        expectedAfterImageCollectionProjectExampleUpload(requesterUserId, projectId, bucketName, exampleFile, fixtureProjectEntity);
        repositories.saveProjectEntity(fixtureProjectEntity);

        repositories.deletaAllProblem();

        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);
        int expectedProblemListSize = 0;

        // Exercise SUT
        ResultActions result = performPayByPoint(authorization, fixtureProjectEntity);

        // Verify Outcome
        result.andExpect(header().string("payment", "fail"));

        List<ProblemEntity> actualProblemEntityList = repositories.getProblemEntityList(projectId);
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertEquals(expectedProblemListSize, actualProblemEntityList.size());
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void successfulGetMyProjectList() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);

        Map<Integer, ProjectEntity> expectedProjectEntityList = repositories.getProjectEntityList(admin);
        Map<Integer, List<ClassEntity>> expectedClassEntityList = new HashMap<>();
        for (Integer projectId : expectedProjectEntityList.keySet()) {
            expectedClassEntityList.put(projectId, repositories.getClassEntityList(projectId));
        }

        // Exercise SUT
        ResultActions result = performGetMyProjectList(authorization);

        // Verify Outcome
        byte[] contentAsByteArray = result.andReturn().getResponse().getContentAsByteArray();
        String contentAsString = new String(contentAsByteArray, "UTF-8");
        JSONArray actualProjectEntityList = new JSONArray(contentAsString);

        for (Object projectDtoWithClassDto : actualProjectEntityList) {
            JSONObject JSONProjectDtoWithClassDto = (JSONObject)projectDtoWithClassDto;
            JSONObject actualJSONProjectDto = JSONProjectDtoWithClassDto.getJSONObject("projectDto");
            JSONArray actualJSONClassNameList = JSONProjectDtoWithClassDto.getJSONArray("classNameList");
            int projectId = actualJSONProjectDto.getInt("projectId");

            assertProjectEquals(expectedProjectEntityList.get(projectId), actualJSONProjectDto);
            assertClassListEquals(expectedClassEntityList.get(projectId), actualJSONClassNameList);
        }

    }

    @Test
    public void getMyProjectListButEmpty() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(workerUserId);

        // Exercise SUT
        ResultActions result = performGetMyProjectList(authorization);

        // Verify Outcome
        result.andExpect(header().string("list", "none"));
    }

    @Test
    public void getMyProjectListWithoutLogin() throws Exception {
        // Setup Fixture
        String authorization = null;

        // Exercise SUT
        ResultActions result = performGetMyProjectList(authorization);

        // Verify Outcome
        result.andExpect(header().string("login", "fail"));
    }

    @Test
    public void successfulGetCrossValidationDetailWithClassification() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 2;

        makeWorkedProblemsValidated(projectId);

        int expectedValidationDetailListSize = 4;

        // Exercise SUT
        ResultActions result = performGetValidationDetail(authorization, projectId);

        // Verify Outcome
        String contentAsString = result.andReturn().getResponse().getContentAsString();

        JSONArray actualValidationDetailList = new JSONObject(contentAsString).getJSONArray("problems");
        assertEquals(expectedValidationDetailListSize, actualValidationDetailList.length());
    }

    @Test
    public void getCrossValidationDetailWithBoundingBoxButEmpty() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 4;

        int expectedValidationDetailListSize = 0;

        // Exercise SUT
        ResultActions result = performGetValidationDetail(authorization, projectId);

        // Verify Outcome
        String contentAsString = result.andReturn().getResponse().getContentAsString();

        JSONArray actualValidationDetailList = new JSONObject(contentAsString).getJSONArray("problems");
        assertEquals(expectedValidationDetailListSize, actualValidationDetailList.length());
    }

    @Test
    public void successfulGetCrossValidationDetailWithBoundingBox() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 4;

        makeWorkedProblemsValidated(projectId);

        int expectedValidationDetailListSize = 5;

        // Exercise SUT
        ResultActions result = performGetValidationDetail(authorization, projectId);

        // Verify Outcome
        String contentAsString = result.andReturn().getResponse().getContentAsString();

        JSONArray actualValidationDetailList = new JSONObject(contentAsString).getJSONArray("problems");
        assertEquals(expectedValidationDetailListSize, actualValidationDetailList.length());
    }

    @Test
    public void getCrossValidationDetailWithoutLogin() throws Exception {
        // Setup Fixture
        String authorization = null;
        int projectId = 2;

        // Exercise SUT
        ResultActions result = performGetValidationDetail(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("login", "fail"));
    }

    @Test
    public void successfulTerminateProjectWithHardestClassification() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 2;

        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        makeWorkedProblemsValidated(projectId);
        makeProjectDifficultyHardest(projectId);

        int expectedFinalCost = fixtureProjectEntity.getValidatedData() * COST_PER_DATA_HARDEST - fixtureProjectEntity.getCost();

        // Exercise SUT
        ResultActions result = performTerminateProject(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("project", "success"))
                .andExpect(header().exists("finalCost"));

        int actualFinalCost = Integer.parseInt(result.andReturn().getResponse().getHeader("finalCost"));

        assertEquals(expectedFinalCost, actualFinalCost);
    }

    @Test
    public void successfulTerminateProjectWithHarderClassification() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 2;

        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        makeWorkedProblemsValidated(projectId);
        makeProjectDifficultyHarder(projectId);

        int expectedFinalCost = fixtureProjectEntity.getValidatedData() * COST_PER_DATA_HARDER - fixtureProjectEntity.getCost();

        // Exercise SUT
        ResultActions result = performTerminateProject(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("project", "success"))
                .andExpect(header().exists("finalCost"));

        int actualFinalCost = Integer.parseInt(result.andReturn().getResponse().getHeader("finalCost"));

        assertEquals(expectedFinalCost, actualFinalCost);
    }

    @Test
    public void successfulTerminateProjectWithNormalBoundingBox() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 4;

        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        makeWorkedProblemsValidated(projectId);
        makeProjectDifficultyNormal(projectId);

        int expectedFinalCost = fixtureProjectEntity.getValidatedData() * COST_PER_DATA_NORMAL - fixtureProjectEntity.getCost();

        // Exercise SUT
        ResultActions result = performTerminateProject(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("project", "success"))
                .andExpect(header().exists("finalCost"));

        int actualFinalCost = Integer.parseInt(result.andReturn().getResponse().getHeader("finalCost"));

        assertEquals(expectedFinalCost, actualFinalCost);
    }

    @Test
    public void terminateProjectButAlreadyTerminated() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 4;

        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        fixtureProjectEntity.setStatus("결제완료");

        // Exercise SUT
        ResultActions result = performTerminateProject(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("project", "fail"));
    }

    @Test
    public void terminateProjectByOther() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(workerUserId);
        int projectId = 4;

        // Exercise SUT
        ResultActions result = performTerminateProject(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("project", "fail"));
    }

    @Test
    public void successfulTerminateProjectByAccountWithClassification() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 6;

        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        makeWorkedProblemsValidated(projectId);
        makeProjectDifficultyHardest(projectId);
        UserEntity fixtureUserEntity = repositories.getFixtureUserEntity(admin);

        int expectedFinalCost = fixtureProjectEntity.getValidatedData() * COST_PER_DATA_HARDEST - fixtureProjectEntity.getCost();
        UserEntity expectedUserEntity = copyUserEntity(fixtureUserEntity);
        expectedUserEntity.setPoint(expectedUserEntity.getPoint() - expectedFinalCost);

        // Exercise SUT
        ResultActions result = performTerminateByAccount(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("payment", "success"));

        UserEntity actualUserEntity = repositories.getUserEntity(admin);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void successfulTerminateProjectByAccountAndRefundWithBoundingBox() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 4;

        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        makeWorkedProblemsValidated(projectId);
        makeProjectDifficultyHardest(projectId);
        UserEntity fixtureUserEntity = repositories.getFixtureUserEntity(admin);

        int expectedFinalCost = fixtureProjectEntity.getValidatedData() * COST_PER_DATA_HARDEST - fixtureProjectEntity.getCost();
        UserEntity expectedUserEntity = copyUserEntity(fixtureUserEntity);
        expectedUserEntity.setPoint(expectedUserEntity.getPoint() - expectedFinalCost);

        // Exercise SUT
        ResultActions result = performTerminateByAccount(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("payment", "success"));

        UserEntity actualUserEntity = repositories.getUserEntity(admin);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void terminateProjectByAccountButAlreadyTerminated() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 2;

        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        fixtureProjectEntity.setStatus("결제완료");
        UserEntity fixtureUserEntity = repositories.getFixtureUserEntity(admin);

        UserEntity expectedUserEntity = copyUserEntity(fixtureUserEntity);

        // Exercise SUT
        ResultActions result = performTerminateByAccount(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("payment", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(admin);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void terminateProjectByAccountWithoutRegisteringOpenBanking() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 2;

        UserEntity fixtureUserEntity = repositories.getFixtureUserEntity(admin);
        fixtureUserEntity.setUserOpenBankingAccessToken(UUID.randomUUID().toString().replace("-", " "));
        fixtureUserEntity.setUserOpenBankingNum(0);
        repositories.saveUserEntity(fixtureUserEntity);

        UserEntity expectedUserEntity = copyUserEntity(fixtureUserEntity);
        ProjectEntity expectedProjectEntity = copyProjectEntity(repositories.getProjectEntity(projectId));

        // Exercise SUT
        ResultActions result = performTerminateByAccount(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("payment", "fail"))
                .andExpect(header().exists("state"));

        UserEntity actualUserEntity = repositories.getUserEntity(admin);
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void successfulTerminateProjectByPointWithCollection() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 1;

        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        makeWorkedProblemsValidated(projectId);
        makeProjectDifficultyNormal(projectId);
        UserEntity fixtureUserEntity = repositories.getFixtureUserEntity(admin);

        int expectedFinalCost = fixtureProjectEntity.getValidatedData() * COST_PER_DATA_NORMAL - fixtureProjectEntity.getCost();
        UserEntity expectedUserEntity = copyUserEntity(fixtureUserEntity);
        expectedUserEntity.setPoint(expectedUserEntity.getPoint() - expectedFinalCost);

        // Exercise SUT
        ResultActions result = performTerminateByPoint(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("payment", "success"));

        UserEntity actualUserEntity = repositories.getUserEntity(admin);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
    }

    @Test
    public void terminateProjectByPointButAlreadyTerminated() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 2;

        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        fixtureProjectEntity.setStatus("결제대기");

        UserEntity expectedUserEntity = repositories.getFixtureUserEntity(admin);
        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);

        // Exercise SUT
        ResultActions result = performTerminateByPoint(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("payment", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(admin);
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
    }

    @Test
    public void terminateProjectByPointButNotEnoughPoint() throws Exception {
        // Setup Fixture
        String authorization = makeValidAuthorizationToken(admin);
        int projectId = 6;

        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        makeWorkedProblemsValidated(projectId);
        makeProjectDifficultyHardest(projectId);
        UserEntity fixtureUserEntity = repositories.getFixtureUserEntity(admin);
        fixtureUserEntity.setPoint(0);

        UserEntity expectedUserEntity = repositories.getFixtureUserEntity(admin);
        ProjectEntity expectedProjectEntity = copyProjectEntity(fixtureProjectEntity);

        // Exercise SUT
        ResultActions result = performTerminateByPoint(authorization, projectId);

        // Verify Outcome
        result.andExpect(header().string("payment", "fail"));

        UserEntity actualUserEntity = repositories.getUserEntity(admin);
        ProjectEntity actualProjectEntity = repositories.getProjectEntity(projectId);

        assertUserPointEquals(expectedUserEntity, actualUserEntity);
        assertProjectEquals(expectedProjectEntity, actualProjectEntity);
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

    private ResultActions performUploadExample(String authorization, ProjectEntity fixtureProjectEntity, File fixtureExampleFile) throws Exception {
        String uri = new URIBuilder("/api/project/upload/example")
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", fixtureExampleFile.getName(), "", new FileInputStream(fixtureExampleFile));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(uri).file(mockMultipartFile);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        request.header("bucketName", fixtureProjectEntity.getBucketName());
        return mockMvc.perform(request);
    }

    private ResultActions performUploadLabellingData(String authorization, ProjectEntity fixtureProjectEntity, File[] fixtureLabellingFilesList) throws Exception {
        String uri = new URIBuilder("/api/project/upload/labelling")
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockMultipartHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(uri);
        for (File fixtureLabellingFile : fixtureLabellingFilesList) {
            MockMultipartFile mockMultipartFile = new MockMultipartFile("files", fixtureLabellingFile.getName(), "", new FileInputStream(fixtureLabellingFile));
            request.file(mockMultipartFile);
        }
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        request.header("bucketName", fixtureProjectEntity.getBucketName());
        return mockMvc.perform(request);
    }

    private ResultActions performPayByAccount(String authorization, ProjectEntity fixtureProjectEntity) throws Exception {
        String uri = new URIBuilder("/api/project/account/payment")
                .addParameter("projectId", String.valueOf(fixtureProjectEntity.getProjectId()))
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performPayByPoint(String authorization, ProjectEntity fixtureProjectEntity) throws Exception {
        String uri = new URIBuilder("/api/project/point/payment")
                .addParameter("projectId", String.valueOf(fixtureProjectEntity.getProjectId()))
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performGetMyProjectList(String authorization) throws Exception {
        String uri = new URIBuilder("/api/project/all")
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performGetValidationDetail(String authorization, int projectId) throws Exception {
        String uri = new URIBuilder("/api/project/crossvalidation")
                .addParameter("projectId", String.valueOf(projectId))
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performTerminateProject(String authorization, int projectId) throws Exception {
        String uri = new URIBuilder("/api/project/terminate")
                .addParameter("projectId", String.valueOf(projectId))
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performTerminateByAccount(String authorization, int projectId) throws Exception {
        String uri = new URIBuilder("/api/project/terminate/account")
                .addParameter("projectId", String.valueOf(projectId))
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(uri);
        if(authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performTerminateByPoint(String authorization, int projectId) throws Exception {
        String uri = new URIBuilder("/api/project/terminate/point")
                .addParameter("projectId", String.valueOf(projectId))
                .build().toString();
        uri = URLDecoder.decode(uri, "UTF-8");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(uri);
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

    private void assertProjectEquals(ProjectEntity expectedProejctEntity, JSONObject actualJSONProjectEntity) {
        assertEquals(expectedProejctEntity.getUserId(), actualJSONProjectEntity.getString("userId"));
        assertEquals(expectedProejctEntity.getProjectName(), actualJSONProjectEntity.getString("projectName"));
        assertEquals(expectedProejctEntity.getStatus(), actualJSONProjectEntity.getString("status"));
        assertEquals(expectedProejctEntity.getWorkType(), actualJSONProjectEntity.getString("workType"));
        assertEquals(expectedProejctEntity.getDataType(), actualJSONProjectEntity.getString("dataType"));
        assertEquals(expectedProejctEntity.getSubject(), actualJSONProjectEntity.getString("subject"));
        // 실수이므로 오차 감안
        assertTrue(0.01 > Math.abs(expectedProejctEntity.getDifficulty() - actualJSONProjectEntity.getDouble("difficulty")));
        assertEquals(expectedProejctEntity.getWayContent(), actualJSONProjectEntity.getString("wayContent"));
        assertEquals(expectedProejctEntity.getConditionContent(), actualJSONProjectEntity.getString("conditionContent"));
        assertEquals(expectedProejctEntity.getExampleContent(), actualJSONProjectEntity.getString("exampleContent"));
        assertEquals(expectedProejctEntity.getDescription(), actualJSONProjectEntity.getString("description"));
        assertEquals(expectedProejctEntity.getTotalData(), actualJSONProjectEntity.getInt("totalData"));
        assertEquals(expectedProejctEntity.getProgressData(), actualJSONProjectEntity.getInt("progressData"));
        assertEquals(expectedProejctEntity.getValidatedData(), actualJSONProjectEntity.getInt("validatedData"));
        assertEquals(expectedProejctEntity.getCost(), actualJSONProjectEntity.getInt("cost"));
    }

    private void assertClassListEquals(List<String> expectedClassList, List<ClassEntity> actualClassEntityList) {
        for(ClassEntity classEntity : actualClassEntityList) {
            assertTrue(expectedClassList.contains(classEntity.getClassName()));
        }
    }

    private void assertClassListEquals(List<ClassEntity> expectedClassEntityList, JSONArray actualJSONClassNameList) {
        List<String> expectedClassList = new ArrayList<>();
        for (ClassEntity classEntity : expectedClassEntityList) {
            expectedClassList.add(classEntity.getClassName());
        }
        for(Object JSONClassEntity : actualJSONClassNameList) {
            assertTrue(expectedClassList.contains(((JSONObject)JSONClassEntity).getString("className")));
        }
    }

    private void assertFileEquals(FileInputStream expectedExampleFile, S3ObjectInputStream actualExampleFile) {
        int expectedReadLength = 0;
        int actualReadLength = 0;
        do {
            byte[] expectedBytes = new byte[4096];
            byte[] actualBytes = new byte[4096];
            try {
                expectedReadLength = expectedExampleFile.read(expectedBytes);
                actualReadLength = actualExampleFile.read(actualBytes);
            } catch (IOException e) {
                fail(e);
            }
            assertEquals(expectedReadLength, actualReadLength);
            assertArrayEquals(expectedBytes, actualBytes);
        } while (expectedReadLength == -1 || actualReadLength == -1);
    }

    private void assertProblemEntityEquals(ProblemEntity expectedProblemEntity, ProblemEntity actualProblemEntity) {
        assertEquals(expectedProblemEntity.getProjectId(), actualProblemEntity.getProjectId());
        assertEquals(expectedProblemEntity.getReferenceId(), actualProblemEntity.getReferenceId());
        assertEquals(expectedProblemEntity.getBucketName(), actualProblemEntity.getBucketName());
        assertEquals(expectedProblemEntity.getObjectName(), actualProblemEntity.getObjectName());
        assertEquals(expectedProblemEntity.getValidationStatus(), actualProblemEntity.getValidationStatus());
    }

    private void assertUserPointEquals(UserEntity expectedUserEntity, UserEntity actualUserEntity) {
        assertEquals(expectedUserEntity.getTotalPoint(), actualUserEntity.getTotalPoint());
        assertEquals(expectedUserEntity.getPoint(), actualUserEntity.getPoint());
        assertEquals(expectedUserEntity.getUserVisit(), actualUserEntity.getUserVisit());
        // 오차 10초 허용
        assertTrue(10 * 1000 >= Math.abs(expectedUserEntity.getUserLastVisit().getTime() - actualUserEntity.getUserLastVisit().getTime()));
    }

    private Map<String, ProblemEntity> expectedLabellingProblemList(ProjectEntity fixtureProjectEntity) {
        Map<String, ProblemEntity> expectedProblemEntityList = new HashMap<>();
        List<String> objectList = objectStorageApiClient.listObjects(fixtureProjectEntity.getBucketName());
        for (String objectName : objectList) {
            if(!objectName.equals(fixtureProjectEntity.getExampleContent())) {
                ProblemEntity expectedProblemEntity = makeEmptyProblemEntity();
                expectedProblemEntity.setProjectId(fixtureProjectEntity.getProjectId());
                expectedProblemEntity.setReferenceId(-1);
                expectedProblemEntity.setBucketName(fixtureProjectEntity.getBucketName());
                expectedProblemEntity.setObjectName(objectName);
                expectedProblemEntity.setValidationStatus("작업전");
                expectedProblemEntityList.put(objectName, expectedProblemEntity);
            }
        }
        return expectedProblemEntityList;
    }

    private void makeWorkedProblemsValidated(int projectId) {
        int fixtureValidateData = 0;
        List<ProblemEntity> fixtureProblemEntityList = repositories.getProblemEntityList(projectId);
        for (ProblemEntity fixtureProblemEntity : fixtureProblemEntityList) {
            if(fixtureProblemEntity.getReferenceId() == -1 && fixtureProblemEntity.getValidationStatus().equals("작업후")) {
                fixtureValidateData++;
                fixtureProblemEntity.setFinalAnswer(fixtureProblemEntity.getAnswer());
                fixtureProblemEntity.setValidationStatus("검증완료");
                repositories.saveProblemEntity(fixtureProblemEntity);
            } else if(fixtureProblemEntity.getValidationStatus().equals("검증완료")) {
                fixtureValidateData++;
            }
        }
        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        fixtureProjectEntity.setValidatedData(fixtureValidateData);
        repositories.saveProjectEntity(fixtureProjectEntity);
    }

    private void makeProjectDifficultyHardest(int projectId) {
        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        fixtureProjectEntity.setDifficulty(getDifficulty(fixtureProjectEntity, 4.5f));
        repositories.saveProjectEntity(fixtureProjectEntity);
    }

    private void makeProjectDifficultyHarder(int projectId) {
        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        fixtureProjectEntity.setDifficulty(getDifficulty(fixtureProjectEntity, 3.5f));
        repositories.saveProjectEntity(fixtureProjectEntity);
    }

    private void makeProjectDifficultyNormal(int projectId) {
        ProjectEntity fixtureProjectEntity = repositories.getProjectEntity(projectId);
        fixtureProjectEntity.setDifficulty(getDifficulty(fixtureProjectEntity, 2));
        repositories.saveProjectEntity(fixtureProjectEntity);
    }

    private float getDifficulty(ProjectEntity fixtureProjectEntity, float target) {
        int validationData = fixtureProjectEntity.getValidatedData();
        return (6 - target) * validationData / 5;
    }
}