package org.filespace.model.entities.simplerelations;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.filespace.model.entities.compoundrelations.FileFilespaceRelation;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "files")
public class File extends Model {

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @JsonAlias({"filename"})
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
    @Column(name = "post_date_time",
            nullable = false)
    private LocalDateTime postDateTime;

    @NotNull
    @Min(0)
    @Column(name = "number_of_downloads",
            nullable = false)
    private Integer numberOfDownloads;

    @NotNull
    @Column(name = "description",
            nullable = false,
            length = 400)
    private String description;

    @JsonIgnore
    @NotNull
    @Column(name = "md5_hash",
            length = 32,
            nullable = false)
    private String md5Hash;

    @JsonIgnore
    @ManyToMany(mappedBy = "files",
            fetch = FetchType.LAZY)
    private List<Filespace> filespaces;

    @JsonIgnore
    @OneToMany(mappedBy = "file",
            targetEntity = FileFilespaceRelation.class,
            fetch = FetchType.LAZY)
    private Set<FileFilespaceRelation> fileFilespaceRelations;

    public File() {

    }

    public File(User sender, String fileName, Long size, LocalDateTime postDateTime, Integer numberOfDownloads, String description, String md5Hash) {
        this.sender = sender;
        this.fileName = fileName;
        this.size = size;
        this.postDateTime = postDateTime;
        this.numberOfDownloads = numberOfDownloads;
        this.description = description;
        this.md5Hash = md5Hash;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public LocalDateTime getPostDateTime() {
        return postDateTime;
    }

    public void setPostDateTime(LocalDateTime postDateTime) {
        this.postDateTime = postDateTime;
    }

    public Integer getNumberOfDownloads() {
        return numberOfDownloads;
    }

    public void setNumberOfDownloads(Integer numberOfDownloads) {
        this.numberOfDownloads = numberOfDownloads;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String comment) {
        this.description = comment;
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

    public List<Filespace> getFilespaces() {
        return filespaces;
    }

    public void setFilespaces(List<Filespace> filespaces) {
        this.filespaces = filespaces;
    }

    public Set<FileFilespaceRelation> getFileFilespaceRelations() {
        return fileFilespaceRelations;
    }

    public void setFileFilespaceRelations(Set<FileFilespaceRelation> fileFilespaceRelations) {
        this.fileFilespaceRelations = fileFilespaceRelations;
    }
}
