package org.filespace.model.compoundrelations;

import javax.persistence.*;
import org.filespace.model.EntityImplementation;
import org.filespace.model.entities.File;
import org.filespace.model.entities.Filespace;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "files_filespaces")
public class FileFilespaceRelation implements EntityImplementation {

    @NotNull
    @EmbeddedId
    private FileFilespaceKey fileFilespaceKey = new FileFilespaceKey();

    @MapsId("fileId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @MapsId("filespaceId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filespace_id")
    private Filespace filespace;

    @NotNull
    @Column(name = "attach_date",
            nullable = false)
    private LocalDate attachDate;

    @NotNull
    @Column(name = "attach_time",
            nullable = false)
    private LocalTime attachTime;

    public FileFilespaceRelation(@NotNull FileFilespaceKey fileFilespaceKey, @NotNull LocalDate attachDate, @NotNull LocalTime attachTime) {
        this.fileFilespaceKey = fileFilespaceKey;
        this.attachDate = attachDate;
        this.attachTime = attachTime;
    }

    public FileFilespaceRelation() {
    }

    public FileFilespaceKey getFileFilespaceKey() {
        return fileFilespaceKey;
    }

    public void setFileFilespaceKey(FileFilespaceKey fileFilespaceKey) {
        this.fileFilespaceKey = fileFilespaceKey;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Filespace getFilespace() {
        return filespace;
    }

    public void setFilespace(Filespace filespace) {
        this.filespace = filespace;
    }

    public LocalDate getAttachDate() {
        return attachDate;
    }

    public void setAttachDate(LocalDate attachDate) {
        this.attachDate = attachDate;
    }

    public LocalTime getAttachTime() {
        return attachTime;
    }

    public void setAttachTime(LocalTime attachTime) {
        this.attachTime = attachTime;
    }
}