package swcapstone.freitag.springsecurityjpa.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.UserDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Common {
    public static String SHA256(String str){
        String SHA = "";
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return SHA;
    }

    public static String makeValidAuthorizationToken(String userId) {
        int oneDay = 24 * 3600 * 1000;
        String jwtToken = JWT.create()
                .withSubject(userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + oneDay))
                .sign(Algorithm.HMAC512(JwtProperties.SECRET.getBytes()));
        return JwtProperties.TOKEN_PREFIX + jwtToken;
    }

    public static String makeExpiredAuthorizationToken(String userId) {
        String jwtToken = JWT.create()
                .withSubject(userId)
                .withExpiresAt(new Date(0))
                .sign(Algorithm.HMAC512(JwtProperties.SECRET.getBytes()));
        return JwtProperties.TOKEN_PREFIX + jwtToken;
    }

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

    public static ProjectEntity getFixtureCollectionProjectEntity() {
        ProjectEntity fixtureProjectEntity = makeEmptyProjectEntity();
        fixtureProjectEntity.setProjectName("동물 사진 수집");
        fixtureProjectEntity.setWorkType("collection");
        fixtureProjectEntity.setDataType("image");
        fixtureProjectEntity.setSubject("동물");
        fixtureProjectEntity.setWayContent("같은 종류의 동물만 있는 사진을 올려주세요.");
        fixtureProjectEntity.setConditionContent("다음 중 해당하는 동물이 무엇인지 선택해주세요. 한 사진에 여러 동물이 있으면 없음을 선택해주세요.");
        fixtureProjectEntity.setDescription("야생 동물 경보기 제작을 위한 프로젝트입니다.");
        fixtureProjectEntity.setTotalData(10);
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

    public static List<String> getFixtureClassList() {
        List<String> fixtureClassList = new ArrayList<>();
        fixtureClassList.add("까치");
        fixtureClassList.add("고양이");
        fixtureClassList.add("강아지");
        return fixtureClassList;
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
}
