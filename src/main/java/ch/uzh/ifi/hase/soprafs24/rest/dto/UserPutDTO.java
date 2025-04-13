package ch.uzh.ifi.hase.soprafs24.rest.dto;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPutDTO {
    private String username;
    private String name;
    private String password;
    private LocalDate birthday;
    private String timezone;
    private String profilePicture;
}
