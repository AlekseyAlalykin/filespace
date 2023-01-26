package org.filespace.model.compoundrelations;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FileFilespaceKey implements Serializable {

    @Column(name = "file_id",
            nullable = false,
            updatable = false)
    private Long fileId;

    @Column(name = "filespace_id",
            nullable = false,
            updatable = false)
    private Long filespaceId;

    public FileFilespaceKey() {
    }

    public FileFilespaceKey(Long fileId, Long filespaceId) {
        this.fileId = fileId;
        this.filespaceId = filespaceId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getFilespaceId() {
        return filespaceId;
    }

    public void setFilespaceId(Long filespaceId) {
        this.filespaceId = filespaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileFilespaceKey)) return false;
        FileFilespaceKey that = (FileFilespaceKey) o;
        return fileId.equals(that.fileId) &&
                filespaceId.equals(that.filespaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, filespaceId);
    }
}
