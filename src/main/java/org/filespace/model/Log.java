package org.filespace.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "logs")
public class Log extends Model {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @NotNull
    @Column(name = "action",
            nullable = false,
            length = 20)
    private String action;

    @NotNull
    @Column(name = "action_date",
            nullable = false)
    private LocalDate actionDate;

    @NotNull
    @Column(name = "action_time",
            nullable = false)
    private LocalTime actionTime;

    public Log() {
    }

    public Log(User user, File file, String action, LocalDate actionDate, LocalTime actionTime) {
        this.user = user;
        this.file = file;
        this.action = action;
        this.actionDate = actionDate;
        this.actionTime = actionTime;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDate getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDate actionDate) {
        this.actionDate = actionDate;
    }

    public LocalTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(LocalTime actionTime) {
        this.actionTime = actionTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }


}
