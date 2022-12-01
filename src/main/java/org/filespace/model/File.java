package org.filespace.model;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "files")
public class File extends Model {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @NotNull
    @Size(min = 1)
    @Column(name = "file_name",
            nullable = false,
            length = 255)
    private String fileName;

    @NotNull
    @Min(0)
    @Column(name = "size",
            nullable = false)
    private Long size;

    @NotNull
    @Column(name = "post_date",
            nullable = false)
    private LocalDate postDate;

    @NotNull
    @Column(name = "post_time",
            nullable = false)
    private LocalTime postTime;

    @NotNull
    @Min(0)
    @Column(name = "number_of_downloads",
            nullable = false)
    private Integer numberOfDownloads;

    @NotNull
    @Column(name = "comment",
            nullable = false,
            length = 200)
    private String comment;

    @NotNull
    @Column(name = "md5_hash",
            nullable = false)
    private String md5Hash;

    @ManyToMany(mappedBy = "files",
            fetch = FetchType.LAZY)
    private List<Filespace> filespaces;

    public File() {

    }

    public File(User sender, String fileName, Long size, LocalDate postDate, LocalTime postTime, Integer numberOfDownloads, String comment, String md5Hash) {
        this.sender = sender;
        this.fileName = fileName;
        this.size = size;
        this.postDate = postDate;
        this.postTime = postTime;
        this.numberOfDownloads = numberOfDownloads;
        this.comment = comment;
        this.md5Hash = md5Hash;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public LocalDate getPostDate() {
        return postDate;
    }

    public void setPostDate(LocalDate postDate) {
        this.postDate = postDate;
    }

    public LocalTime getPostTime() {
        return postTime;
    }

    public void setPostTime(LocalTime postTime) {
        this.postTime = postTime;
    }

    public Integer getNumberOfDownloads() {
        return numberOfDownloads;
    }

    public void setNumberOfDownloads(Integer numberOfDownloads) {
        this.numberOfDownloads = numberOfDownloads;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }


}
