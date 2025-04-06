package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;


public class ManageProfileDTO {
    private String username;
    private String name;
    //private String password;
    private LocalDate birthday;
    private String timezone;
    private String profilePicture;




    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    /*public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }*/
    
}
