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
import java.time.LocalDateTime;
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
    @Column(name = "attach_date_time",
            nullable = false)
    private LocalDateTime attachDateTime;

    public FileFilespaceRelation(@NotNull CompoundKey key, @NotNull LocalDateTime attachDateTime) {
        super(key);
        this.attachDateTime = attachDateTime;
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

    public LocalDateTime getAttachDateTime() {
        return attachDateTime;
    }

    public void setAttachDateTime(LocalDateTime attachDateTime) {
        this.attachDateTime = attachDateTime;
    }
}
