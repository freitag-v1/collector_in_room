package swcapstone.freitag.springsecurityjpa.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.UserDto;
import swcapstone.freitag.springsecurityjpa.domain.UserRepository;

@AllArgsConstructor
@Service
public class MyPageService {

    private UserRepository userRepository;

    // 마이페이지 수정
    @Transactional
    public String updateUserInfo(UserDto userDto) {
        // JpaRepository는 save 메서드들 통해 DB에 엔티티 정보를 저장
        // save 메서드는 단순히 새 엔티티를 DB에 추가하는 것이 아니고 엔티티의 상태에 따라 다른 동작방식
        // JPA는 엔티티 매니저Entity Manager가 엔티티가 변경이 일어나면 이를 자동 감지하여 데이터베이스에 반영
        return userRepository.save(userDto.toEntity()).getUserId();
    }
}
