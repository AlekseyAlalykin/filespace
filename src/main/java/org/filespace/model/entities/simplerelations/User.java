package org.filespace.model.entities.simplerelations;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.filespace.model.entities.compoundrelations.UserFilespaceRelation;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends Model {

    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9]+(?:[_-][A-Za-z0-9]+)*$",
             message = "Illegal set of characters or spacing characters repetition")
    @Size(min = 4, max = 50,
            message = "Should be within range of 4 to 50 characters")
    @Column(name = "username",
            nullable = false,
            unique = true,
            length = 50)
    private String username;

    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 60, max = 60)
    @Column(name = "password",
            nullable = false,
            length = 60)
    private String password;

    @NotNull
    @Email(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$",
            message = "Is not a valid email")
    @Column(name = "email",
            nullable = false,
            length = 50,
            unique = true)
    private String email;

    @NotNull
    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @NotNull
    @Column(name = "enabled")
    private Boolean enabled;

    @JsonIgnore
    @OneToMany(mappedBy = "sender",
            fetch = FetchType.LAZY)
    private List<File> files;

    @JsonIgnore
    @OneToMany(mappedBy = "user",
            targetEntity = UserFilespaceRelation.class,
            fetch = FetchType.LAZY)
    private Set<UserFilespaceRelation> userFilespaceRelations;

    public User(){

    }

    public User(String username, String password, String email, LocalDate registrationDate) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.registrationDate = registrationDate;

        this.enabled = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public Set<UserFilespaceRelation> getUserFilespaceRelations() {
        return userFilespaceRelations;
    }

    public void setUserFilespaceRelations(Set<UserFilespaceRelation> userFilespaceRelations) {
        this.userFilespaceRelations = userFilespaceRelations;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
