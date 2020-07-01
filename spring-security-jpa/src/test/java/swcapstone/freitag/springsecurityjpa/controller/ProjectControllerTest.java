package swcapstone.freitag.springsecurityjpa.controller;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.http.client.utils.URIBuilder;
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
    private final int projectId = 1;
    private final String bucketName = "freitag-test";
    private final String prebuiltBucketName = "freitag-test-prebuilt";
    private final int prebuiltBucketSize = 15;
    private final String exampleFile = "example.jpg";
    private final String labellingData = "LabellingData";
    public static final int COST_PER_DATA = 50;

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
}