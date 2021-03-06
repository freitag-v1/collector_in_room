package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.api.OpenBankingClient;
import swcapstone.freitag.springsecurityjpa.api.SMSClient;
import swcapstone.freitag.springsecurityjpa.domain.*;
import swcapstone.freitag.springsecurityjpa.domain.dto.*;
import swcapstone.freitag.springsecurityjpa.domain.entity.UserEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProblemRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.UserRepository;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Service
public class UserService implements UserDetailsService {
    // UserDetailsService 는 데이터베이스의 유저정보를 불러오는 역할
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProblemRepository problemRepository;
    @Autowired
    private SMSClient smsClient;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void updateVisit(String userId, HttpServletResponse response) {
        int oneDay = 24 * 3600 * 1000;
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Optional<UserEntity> loginedUser = userRepository.findByUserId(userId);

        loginedUser.ifPresent(selectedUser -> {

            if(oneDay < currentTime.getTime() - selectedUser.getUserLastVisit().getTime()) {
                if(selectedUser.getUserVisit() < 30) {
                    selectedUser.setUserVisit(selectedUser.getUserVisit() + 1);
                    selectedUser.setUserLastVisit(currentTime);
                    selectedUser.setTotalPoint(selectedUser.getTotalPoint() + 100);
                    selectedUser.setPoint(selectedUser.getPoint() + 100);
                    response.setHeader("reward", "true");
                }
            }
        });
    }

    @Transactional
    public boolean signUp(HttpServletRequest request, HttpServletResponse response) {

        String userId = request.getParameter("userId");
        String userPassword = request.getParameter("userPassword");
        String userName = request.getParameter("userName");
        int userOpenBankingNum = 0;
        String userOpenBankingAccessToken = UUID.randomUUID().toString().replace("-", "");
        String userPhone = request.getParameter("userPhone");
        String userEmail = request.getParameter("userEmail");
        String userAffiliation = request.getParameter("userAffiliation");

        int userVisit = 0;
        Timestamp userLastVisit = new Timestamp(0);
        int totalPoint = 0;
        int point = 0;
        double userAccuracy = 0;

        UserDto userDto = new UserDto
                (userId, userPassword, userName, userOpenBankingNum, userOpenBankingAccessToken, userPhone, userEmail, userAffiliation
                        , userVisit, userLastVisit, totalPoint, point, userAccuracy);

        // System.out.println("암호화 전 비번: "+userDto.getUserPassword());
        // 비밀번호 암호화
        userDto.setUserPassword(passwordEncoder.encode(userDto.getUserPassword()));
        // System.out.println("암호화 후 비번: "+userDto.getUserPassword());

        // 이미 해당 userId가 있으면 회원가입 실패
        if(loadUserByUsername(userDto.getUserId()) != null) {
            // System.out.println("아이디 중복");
            return false;
        }
        userRepository.save(userDto.toEntity());
        // System.out.println("회원가입 성공! - DB 저장 성공");

        // 유저가 오픈뱅킹 등록시 사용할 고유한 state를 헤더로 전달
        response.addHeader("state", userOpenBankingAccessToken);
        return true;
    }

    // UserDetailsService 인터페이스에는 DB에서 유저 정보를 불러오는 중요한 메소드가 존재 - loadUserByUsername
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);

        if(userEntityWrapper.isPresent()) {

            UserEntity userEntity = userEntityWrapper.get();

            List<GrantedAuthority> authorityList = new ArrayList<>();
            // 로그인한 계정에게 권한 부여하기
            if(userEntity.getUserId().equals("woneyhoney")) {
                authorityList.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
            }
            else {
                authorityList.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
            }

            User user = new User(userEntity.getUserId(), userEntity.getUserPassword(), authorityList);
            return new CustomUser(user, userEntity.getUserName(), userEntity.getUserPhone(), userEntity.getUserEmail(), userEntity.getUserAffiliation(), userEntity.getPoint(), userEntity.getUserAccuracy());
        }

        return null;
    }

    public int getPoint(String userId) {

        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);

        if(!userEntityWrapper.isPresent())
            return -1;

        return userEntityWrapper.get().getPoint();
    }

    private int getTotalPoint(String userId) {

        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);

        if(!userEntityWrapper.isPresent())
            return -1;

        return userEntityWrapper.get().getTotalPoint();
    }

    // 포인트 결제
    @Transactional
    public boolean pointPayment(String userId, int cost, HttpServletResponse response) {

        int point = getPoint(userId);

        if(cost <= point) {
            Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);

            userEntityWrapper.ifPresent(selectUser -> {
                selectUser.setPoint(point - cost);
                userRepository.save(selectUser);
            });

            response.setHeader("payment", "success");
            return true;
        }

        response.setHeader("payment", "fail");
        return false;
    }

    // 계좌이체 결제
    @Transactional
    public boolean accountPayment(String userId, String memo, int cost, HttpServletResponse response) {

        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);

        String openbankingAccessToken = userEntityWrapper.get().getUserOpenBankingAccessToken();
        int openbankingNum = userEntityWrapper.get().getUserOpenBankingNum();
        if(openbankingNum == 0) {
            response.setHeader("state", openbankingAccessToken);
        } else if(cost < 0) {
            if(OpenBankingClient.getInstance().deposit(openbankingAccessToken, openbankingNum, memo, -cost)) {
                userEntityWrapper.ifPresent(selectedUser -> {
                    selectedUser.setPoint(selectedUser.getPoint() - cost);
                    userRepository.save(selectedUser);
                });
                response.setHeader("payment", "success");

                String msg = String.format("[방구석 수집가]\n등록하신 계좌로 %d원이 환급되었습니다.", cost);
                try {
                    smsClient.sendSMS(userId, msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } else if(cost > 0) {
            if(OpenBankingClient.getInstance().withdraw(openbankingAccessToken, openbankingNum, memo, cost)) {
                userEntityWrapper.ifPresent(selectedUser -> {
                    selectedUser.setPoint(selectedUser.getPoint() - cost);
                    userRepository.save(selectedUser);
                });
                response.setHeader("payment", "success");

                String msg = String.format("[방구석 수집가]\n등록하신 계좌로 %d원이 결제되었습니다.", cost);
                try {
                    smsClient.sendSMS(userId, msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } else {
            response.setHeader("payment", "success");
            return true;
        }
        response.setHeader("payment", "fail");
        return false;

    }

    @Transactional
    public void pointExchange(String userId, int amount, HttpServletResponse response) {
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        userEntityWrapper.ifPresent(selectUser -> {
            if(selectUser.getUserOpenBankingNum() == 0) {
                response.setHeader("state", selectUser.getUserOpenBankingAccessToken());
                response.setHeader("exchange", "fail");
                return;
            }
            if(amount <= selectUser.getPoint()) {
                if(OpenBankingClient.getInstance().deposit(selectUser.getUserOpenBankingAccessToken(), selectUser.getUserOpenBankingNum(), "테스트", amount)) {
                    selectUser.setPoint(selectUser.getPoint() - amount);
                    userRepository.save(selectUser);
                    response.setHeader("exchange", "success");
                    String msg = String.format("[방구석 수집가]\n등록하신 계좌로 %d원이 환전되었습니다.", amount);
                    try {
                        smsClient.sendSMS(userId, msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            response.setHeader("exchange", "fail");
        });
    }

    // 누적 포인트별 랭킹 갱신 기능
    public List<TotalPointRankUserDto> rankingUpdateByTotalPoint(HttpServletResponse response) {
        List<UserEntity> top5Users = userRepository.findTop5ByOrderByTotalPointDesc();

        if(top5Users == null) {
            response.setHeader("ranking", "fail");
            return null;
        }

        List<TotalPointRankUserDto> top5 = new ArrayList<>();
        for(UserEntity u : top5Users) {
            String userId = u.getUserId();
            int numOfProblems = (int) problemRepository.countByUserId(userId);
            int totalPoint = u.getTotalPoint();

            TotalPointRankUserDto rankUserDto = new TotalPointRankUserDto(userId, numOfProblems, totalPoint);
            top5.add(rankUserDto);
        }

        response.setHeader("ranking", "success");
        return top5;
    }

    // 정확도별 랭킹 갱신 기능
    public List<AccuracyRankUserDto> rankingUpdateByAccuracy(HttpServletResponse response) {
        List<UserEntity> top5Users = userRepository.findTop5ByOrderByUserAccuracyDesc();

        if(top5Users == null) {
            response.setHeader("ranking", "fail");
            return null;
        }

        List<AccuracyRankUserDto> top5 = new ArrayList<>();
        for(UserEntity u : top5Users) {
            String userId = u.getUserId();
            int numOfProblems = (int) problemRepository.countByUserId(userId);
            double userAccuracy = u.getUserAccuracy();

            AccuracyRankUserDto rankUserDto = new AccuracyRankUserDto(userId, numOfProblems, userAccuracy);
            top5.add(rankUserDto);
        }

        response.setHeader("ranking", "success");
        return top5;
    }
}
