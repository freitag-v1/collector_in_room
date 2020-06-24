package swcapstone.freitag.springsecurityjpa.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserRepository userRepository;

    // 마이페이지 수정
    @Transactional
    public void updateUserInfo(HttpServletRequest request, String userId) {

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

            userRepository.save(selectUser);
        });
    }

}
