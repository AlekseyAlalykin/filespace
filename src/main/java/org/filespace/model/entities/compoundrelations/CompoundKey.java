package org.filespace.model.entities.compoundrelations;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CompoundKey implements Serializable {

    private Long genericId;

    private Long filespaceId;

    public CompoundKey() {
    }

    public CompoundKey(Long genericId, Long filespaceId) {
        this.genericId = genericId;
        this.filespaceId = filespaceId;
    }

    public Long getGenericId() {
        return genericId;
    }

    public void setGenericId(Long genericId) {
        this.genericId = genericId;
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
        if (!(o instanceof CompoundKey)) return false;
        CompoundKey that = (CompoundKey) o;
        return genericId.equals(that.genericId) &&
                filespaceId.equals(that.filespaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genericId, filespaceId);
    }
}
