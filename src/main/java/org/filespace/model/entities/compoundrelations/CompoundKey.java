package org.filespace.model.entities.compoundrelations;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CompoundKey implements Serializable {

    private Integer genericId;

    private Integer filespaceId;

    public CompoundKey() {
    }

    public CompoundKey(Integer genericId, Integer filespaceId) {
        this.genericId = genericId;
        this.filespaceId = filespaceId;
    }

    public Integer getGenericId() {
        return genericId;
    }

    public void setGenericId(Integer genericId) {
        this.genericId = genericId;
    }

    public Integer getFilespaceId() {
        return filespaceId;
    }

    public void setFilespaceId(Integer filespaceId) {
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
