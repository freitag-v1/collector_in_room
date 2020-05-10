package swcapstone.freitag.springsecurityjpa.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;

@Getter
@Setter
public class CustomUser extends User {

    private User user;

    private String userName;
    private int userOpenBankingNum;
    private String userOpenBankingAccessToken;
    private String userPhone;
    private String userEmail;
    private String userAffiliation;

    public CustomUser(User user, /*boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,*/
                      String userName, int userOpenBankingNum, String userOpenBankingAccessToken,
                      String userPhone, String userEmail, String userAffiliation) {
        super(user.getUsername(), user.getPassword(), user.getAuthorities());
        this.user = user;
        this.userName = userName;
        this.userOpenBankingNum = userOpenBankingNum;
        this.userOpenBankingAccessToken = userOpenBankingAccessToken;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userAffiliation = userAffiliation;
    }

}
