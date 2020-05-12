package swcapstone.freitag.springsecurityjpa.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@AllArgsConstructor
@Service
public class MyPageService {

    private UserRepository userRepository;

    // 마이페이지 수정
    @Transactional
    public void updateUserInfo(HttpServletRequest request, String userId) {
        // JpaRepository는 save 메서드들 통해 DB에 엔티티 정보를 저장
        // save 메서드는 단순히 새 엔티티를 DB에 추가하는 것이 아니고 엔티티의 상태에 따라 다른 동작방식
        // JPA는 엔티티 매니저Entity Manager가 엔티티가 변경이 일어나면 이를 자동 감지하여 데이터베이스에 반영

        String userName = request.getParameter("userName");
        String userPhone = request.getParameter("userPhone");
        String userEmail = request.getParameter("userEmail");
        String userAffiliation = request.getParameter("userAffiliation");

        // 마이페이지 수정
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(selectUser -> {
            selectUser.setUserName(userName);
            selectUser.setUserPhone(userPhone);
            selectUser.setUserEmail(userEmail);
            selectUser.setUserAffiliation(userAffiliation);
        });
    }

    // 사용자 방문일, 전체 포인트, 현재 포인트 정보를 헤더에 저장
    // 이 3개 필드는 사용자가 마이페이지에서 수정할 수 없음
    public void getUpdateProhibitedUserInfo(String userId, HttpServletResponse response) {
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);

        if(userEntityWrapper.isPresent()) {
            // get() 메소드를 사용하면 Optional 객체에 저장된 값에 접근
            UserEntity userEntity = userEntityWrapper.get();

            int userVist = userEntity.getUserVisit();
            response.setHeader("userVisit", String.valueOf(userVist));

            int totalPoint = userEntity.getTotalPoint();
            response.setHeader("totalPoint", String.valueOf(totalPoint));

            int point = userEntity.getPoint();
            response.setHeader("point", String.valueOf(point));
        }
    }

}
