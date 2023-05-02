package org.filespace.model.entities.compoundrelations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.filespace.model.entities.simplerelations.Filespace;
import org.filespace.model.entities.simplerelations.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "users_filespaces")
public class UserFilespaceRelation implements Serializable {
    @JsonIgnore
    @NotNull
    @EmbeddedId
    //private UserFilespaceKey key = new UserFilespaceKey();
    private CompoundKey key = new CompoundKey();

    //@JsonIgnore
    @NotNull
    @MapsId("genericId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @NotNull
    @MapsId("filespaceId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filespace_id")
    private Filespace filespace;

    @Column(name = "allow_download",
            nullable = false)
    private Boolean allowDownload;

    @Column(name = "allow_upload",
            nullable = false)
    private Boolean allowUpload;

    @Column(name = "allow_deletion",
            nullable = false)
    private Boolean allowDeletion;

    @Column(name = "allow_user_management",
            nullable = false)
    private Boolean allowUserManagement;

    @Column(name = "allow_filespace_management",
            nullable = false)
    private Boolean allowFilespaceManagement;

    @Column(name = "join_date",
            nullable = false)
    private LocalDate joinDate;

    @Column(name = "join_time",
            nullable = false)
    private LocalTime joinTime;

    public UserFilespaceRelation() {
    }

    public CompoundKey getKey() {
        return key;
    }

    public void setKey(CompoundKey key) {
        this.key = key;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Filespace getFilespace() {
        return filespace;
    }

    public void setFilespace(Filespace filespace) {
        this.filespace = filespace;
    }

    public Boolean allowDownload() {
        return allowDownload;
    }

    public void setAllowDownload(Boolean allowDownload) {
        this.allowDownload = allowDownload;
    }

    public Boolean allowUpload() {
        return allowUpload;
    }

    public void setAllowUpload(Boolean allowUpload) {
        this.allowUpload = allowUpload;
    }

    public Boolean allowDeletion() {
        return allowDeletion;
    }

    public void setAllowDeletion(Boolean allowDeletion) {
        this.allowDeletion = allowDeletion;
    }

    public Boolean allowUserManagement() {
        return allowUserManagement;
    }

    public void setAllowUserManagement(Boolean allowManagement) {
        this.allowUserManagement = allowManagement;
    }

    public Boolean allowFilespaceManagement() {
        return allowFilespaceManagement;
    }

    public void setAllowFilespaceManagement(Boolean allowFilespaceManagement) {
        this.allowFilespaceManagement = allowFilespaceManagement;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public LocalTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalTime joinTime) {
        this.joinTime = joinTime;
    }
}
