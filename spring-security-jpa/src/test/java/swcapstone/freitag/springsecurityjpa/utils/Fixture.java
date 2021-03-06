package swcapstone.freitag.springsecurityjpa.utils;

import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.UserDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

import static swcapstone.freitag.springsecurityjpa.controller.ProjectControllerTest.COST_PER_DATA;

public class Fixture {
    public static UserEntity makeEmptyUserEntity() {
        return new UserDto().toEntity();
    }

    public static UserEntity copyUserEntity(UserEntity originalUserEntity) {
        UserEntity copiedUserEntity = makeEmptyUserEntity();
        copiedUserEntity.setId(originalUserEntity.getId());
        copiedUserEntity.setUserId(originalUserEntity.getUserId());
        copiedUserEntity.setUserPassword(originalUserEntity.getUserPassword());
        copiedUserEntity.setUserName(originalUserEntity.getUserName());
        copiedUserEntity.setUserPhone(originalUserEntity.getUserPhone());
        copiedUserEntity.setUserEmail(originalUserEntity.getUserEmail());
        copiedUserEntity.setUserAffiliation(originalUserEntity.getUserAffiliation());
        copiedUserEntity.setUserVisit(originalUserEntity.getUserVisit());
        copiedUserEntity.setUserLastVisit(originalUserEntity.getUserLastVisit());
        copiedUserEntity.setTotalPoint(originalUserEntity.getTotalPoint());
        copiedUserEntity.setPoint(originalUserEntity.getPoint());
        copiedUserEntity.setUserOpenBankingAccessToken(originalUserEntity.getUserOpenBankingAccessToken());
        copiedUserEntity.setUserOpenBankingNum(originalUserEntity.getUserOpenBankingNum());
        return copiedUserEntity;
    }

    public static ProjectEntity makeEmptyProjectEntity() {
        return new ProjectDto().toEntity();
    }

    public static ProjectEntity copyProjectEntity(ProjectEntity originalProjectEntity) {
        ProjectEntity copiedProjectEntity = makeEmptyProjectEntity();
        copiedProjectEntity.setId(originalProjectEntity.getId());
        copiedProjectEntity.setProjectId(originalProjectEntity.getProjectId());
        copiedProjectEntity.setUserId(originalProjectEntity.getUserId());
        copiedProjectEntity.setProjectName(originalProjectEntity.getProjectName());
        copiedProjectEntity.setBucketName(originalProjectEntity.getBucketName());
        copiedProjectEntity.setStatus(originalProjectEntity.getStatus());
        copiedProjectEntity.setWorkType(originalProjectEntity.getWorkType());
        copiedProjectEntity.setDataType(originalProjectEntity.getDataType());
        copiedProjectEntity.setSubject(originalProjectEntity.getSubject());
        copiedProjectEntity.setDifficulty(originalProjectEntity.getDifficulty());
        copiedProjectEntity.setWayContent(originalProjectEntity.getWayContent());
        copiedProjectEntity.setConditionContent(originalProjectEntity.getConditionContent());
        copiedProjectEntity.setExampleContent(originalProjectEntity.getExampleContent());
        copiedProjectEntity.setDescription(originalProjectEntity.getDescription());
        copiedProjectEntity.setTotalData(originalProjectEntity.getTotalData());
        copiedProjectEntity.setProgressData(originalProjectEntity.getProgressData());
        copiedProjectEntity.setValidatedData(originalProjectEntity.getValidatedData());
        copiedProjectEntity.setCost(originalProjectEntity.getCost());
        return copiedProjectEntity;
    }

    private static ProjectEntity getFixtureProjectEntity() {
        ProjectEntity fixtureProjectEntity;
        fixtureProjectEntity = makeEmptyProjectEntity();
        fixtureProjectEntity.setSubject("동물");
        fixtureProjectEntity.setWayContent("같은 종류의 동물만 있는 사진을 올려주세요.");
        fixtureProjectEntity.setConditionContent("다음 중 해당하는 동물이 무엇인지 선택해주세요. 한 사진에 여러 동물이 있으면 없음을 선택해주세요.");
        fixtureProjectEntity.setDescription("야생 동물 경보기 제작을 위한 프로젝트입니다.");
        fixtureProjectEntity.setTotalData(15);
        return fixtureProjectEntity;
    }

    public static ProjectEntity getFixtureImageCollectionProjectEntity() {
        ProjectEntity fixtureProjectEntity = getFixtureProjectEntity();
        fixtureProjectEntity.setProjectName("동물 사진 수집");
        fixtureProjectEntity.setWorkType("collection");
        fixtureProjectEntity.setDataType("image");
        return fixtureProjectEntity;
    }

    public static ProjectEntity getFixtureImageClassificationProjectEntity() {
        ProjectEntity fixtureProjectEntity = getFixtureProjectEntity();
        fixtureProjectEntity.setProjectName("동물 사진 분류");
        fixtureProjectEntity.setWorkType("labelling");
        fixtureProjectEntity.setDataType("classification");
        return fixtureProjectEntity;
    }

    public static void expectedAfterProjectCreation(String requesterUserId, ProjectEntity expectedProejctEntity) {
        expectedProejctEntity.setUserId(requesterUserId);
        expectedProejctEntity.setStatus("없음");
        expectedProejctEntity.setDifficulty(0);
        expectedProejctEntity.setExampleContent("없음");
        expectedProejctEntity.setProgressData(0);
        expectedProejctEntity.setValidatedData(0);
        expectedProejctEntity.setCost(0);
    }

    public static void expectedAfterImageCollectionProjectExampleUpload(String requesterUserId, int projectId, String bucketName, String exampleFile, ProjectEntity fixtureProjectEntity) {
        expectedAfterProjectCreation(requesterUserId, fixtureProjectEntity);
        fixtureProjectEntity.setProjectId(projectId);
        fixtureProjectEntity.setBucketName(bucketName);
        fixtureProjectEntity.setCost(fixtureProjectEntity.getTotalData() * COST_PER_DATA);
        fixtureProjectEntity.setExampleContent(exampleFile);
    }

    public static void expectedAfterImageClassificationProjectExampleUpload(String requesterUserId, int projectId, String bucketName, String exampleFile, ProjectEntity fixtureProjectEntity) {
        expectedAfterProjectCreation(requesterUserId, fixtureProjectEntity);
        fixtureProjectEntity.setProjectId(projectId);
        fixtureProjectEntity.setBucketName(bucketName);
        fixtureProjectEntity.setExampleContent(exampleFile);
    }

    public static void expectedAfterImageClassificationProjectLabellingUpload(String requesterUserId, int projectId, String prebuiltBucketName, int prebuiltBucketSize, String exampleFile, ProjectEntity fixtureProjectEntity) {
        expectedAfterImageClassificationProjectExampleUpload(requesterUserId, projectId, prebuiltBucketName, exampleFile, fixtureProjectEntity);
        fixtureProjectEntity.setTotalData(prebuiltBucketSize);
        fixtureProjectEntity.setCost(prebuiltBucketSize * COST_PER_DATA);
    }

    public static List<String> getFixtureClassList() {
        List<String> fixtureClassList = new ArrayList<>();
        fixtureClassList.add("까치");
        fixtureClassList.add("고양이");
        fixtureClassList.add("강아지");
        return fixtureClassList;
    }

    public static ProblemEntity makeEmptyProblemEntity() {
        return new ProblemDto().toEntity();
    }

    public static ProblemEntity expectedCollectionProblem(ProjectEntity fixtureProjectEntity) {
        ProblemEntity expectedProblemEntity = makeEmptyProblemEntity();
        expectedProblemEntity.setProjectId(fixtureProjectEntity.getProjectId());
        expectedProblemEntity.setReferenceId(-1);
        expectedProblemEntity.setBucketName(fixtureProjectEntity.getBucketName());
        expectedProblemEntity.setObjectName("");
        expectedProblemEntity.setValidationStatus("작업전");
        return expectedProblemEntity;
    }
}
