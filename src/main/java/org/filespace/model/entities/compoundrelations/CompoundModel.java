package org.filespace.model.entities.compoundrelations;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.EmbeddedId;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
public class CompoundModel implements Serializable {
    @JsonIgnore
    @NotNull
    @EmbeddedId
    protected CompoundKey key = new CompoundKey();

    public CompoundKey getKey() {
        return key;
    }

    public void setKey(CompoundKey key) {
        this.key = key;
    }

    public CompoundModel() {
    }

    public CompoundModel(@NotNull CompoundKey key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompoundModel that = (CompoundModel) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
