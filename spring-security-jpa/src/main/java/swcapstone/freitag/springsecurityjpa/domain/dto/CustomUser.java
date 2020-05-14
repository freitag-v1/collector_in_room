package swcapstone.freitag.springsecurityjpa.domain.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class CustomUser extends User {

    private User user;

    private String userName;
    private String userPhone;
    private String userEmail;
    private String userAffiliation;

    public CustomUser(User user, /*boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,*/
                      String userName, String userPhone, String userEmail, String userAffiliation) {
        super(user.getUsername(), user.getPassword(), user.getAuthorities());
        this.user = user;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userAffiliation = userAffiliation;
    }

}
