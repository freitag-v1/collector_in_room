package swcapstone.freitag.springsecurityjpa.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import swcapstone.freitag.springsecurityjpa.domain.dto.UserDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

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
}
