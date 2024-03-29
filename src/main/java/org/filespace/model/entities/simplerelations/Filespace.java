package org.filespace.model.entities.simplerelations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.filespace.model.entities.compoundrelations.FileFilespaceRelation;
import org.filespace.model.entities.compoundrelations.UserFilespaceRelation;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "filespaces")
public class Filespace extends Model {

    @NotNull
    @Size(min = 3, max = 50,
            message = "Title should have from 3 up to 50 characters")
    @Column(name = "title",
            nullable = false,
            length = 50)
    private String title;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "files_filespaces",
            joinColumns = @JoinColumn(name = "filespace_id", nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "file_id", nullable = false, updatable = false))
    private List<File> files;

    @JsonIgnore
    @OneToMany(mappedBy = "filespace",
            targetEntity = UserFilespaceRelation.class,
            fetch = FetchType.LAZY)
    private Set<UserFilespaceRelation> userFilespaceRelations = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "filespace",
            targetEntity = FileFilespaceRelation.class,
            fetch = FetchType.LAZY)
    private Set<FileFilespaceRelation> fileFilespaceRelations;

    public Filespace(){

    }

    public Filespace(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Set<FileFilespaceRelation> getFileFilespaceRelations() {
        return fileFilespaceRelations;
    }

    public void setFileFilespaceRelations(Set<FileFilespaceRelation> fileFilespaceRelations) {
        this.fileFilespaceRelations = fileFilespaceRelations;
    }
}
