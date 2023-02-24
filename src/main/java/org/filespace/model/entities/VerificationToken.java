package org.filespace.model.entities;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken extends Model{
    private static final int EXPIRATION = 60 * 24;

    @NotNull
    @Column(name = "token",
            length = 50,
            unique = true)
    private String token;

    @NotNull
    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @NotNull
    @Column(name = "issue_date")
    private LocalDate issueDate;

    @NotNull
    @Column(name = "issue_time")
    private LocalTime issueTime;

    @NotNull
    @Column(name = "token_type")
    private TokenType type;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed;

    @Column(name = "value",
            length = 60)
    private String value;

    public boolean isExpired(){
        LocalDateTime expiryDateTime = LocalDateTime.of(issueDate, issueTime);
        if (expiryDateTime.plusMinutes(EXPIRATION).isBefore(LocalDateTime.now()))
            return true;

        return false;
    }

    public VerificationToken() {

    }

    public VerificationToken(String token, User user, LocalDate issueDate, LocalTime issueTime, TokenType type) {
        this.token = token;
        this.user = user;
        this.issueDate = issueDate;
        this.issueTime = issueTime;
        this.type = type;

        isConfirmed = false;
    }

    public VerificationToken(String token, User user, LocalDate issueDate, LocalTime issueTime, TokenType type, String value) {
        this.token = token;
        this.user = user;
        this.issueDate = issueDate;
        this.issueTime = issueTime;
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

    public static int getEXPIRATION() {
        return EXPIRATION;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalTime getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(LocalTime issueTime) {
        this.issueTime = issueTime;
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
