package org.filespace.model.entities.compoundrelations;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.filespace.model.entities.simplerelations.File;
import org.filespace.model.entities.simplerelations.Filespace;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "files_filespaces")
public class FileFilespaceRelation extends CompoundModel {
    //@JsonIgnore
    @MapsId("genericId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @JsonIgnore
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

    public FileFilespaceRelation(@NotNull CompoundKey key, @NotNull LocalDate attachDate, @NotNull LocalTime attachTime) {
        super(key);
        this.attachDate = attachDate;
        this.attachTime = attachTime;
    }

    public FileFilespaceRelation() {
    }

    public CompoundKey getKey() {
        return key;
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
