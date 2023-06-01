package org.filespace.model.entities.simplerelations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Properties;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken extends Model{

    public static Integer EXPIRATION;

    static {
        try {
            Properties props = new Properties();
            props.load(new ClassPathResource("application.properties").getInputStream());
            EXPIRATION = Integer.parseInt(props.getProperty("token-expiration"));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @NotNull
    @Column(name = "token",
            length = 36,
            unique = true)
    private String token;

    @NotNull
    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @NotNull
    @Column(name = "issue_date_time",
            nullable = false)
    private LocalDateTime issueDateTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type",
            length = 12,
            nullable = false)
    private TokenType type;

    @Column(name = "is_confirmed",
            nullable = false)
    private Boolean isConfirmed;

    @Column(name = "value",
            length = 60)
    private String value;

    public boolean isExpired(){
        if (issueDateTime.plusMinutes(EXPIRATION).isBefore(LocalDateTime.now()))
            return true;

        return false;
    }

    public VerificationToken() {

    }

    public VerificationToken(String token, User user, LocalDateTime issueDateTime, TokenType type) {
        this.token = token;
        this.user = user;
        this.issueDateTime = issueDateTime;
        this.type = type;

        isConfirmed = false;
    }

    public VerificationToken(String token, User user, LocalDateTime issueDateTime, TokenType type, String value) {
        this.token = token;
        this.user = user;
        this.issueDateTime = issueDateTime;
        this.type = type;
        this.value = value;

        isConfirmed = false;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getEXPIRATION() {
        return EXPIRATION;
    }

    public LocalDateTime getIssueDateTime() {
        return issueDateTime;
    }

    public void setIssueDateTime(LocalDateTime issueDateTime) {
        this.issueDateTime = issueDateTime;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public Boolean isConfirmed() {
        if (isConfirmed == null)
            return false;
        return isConfirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        isConfirmed = confirmed;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

