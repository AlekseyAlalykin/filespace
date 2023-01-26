package org.filespace.model.compoundrelations;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserFilespaceKey implements Serializable {

    @Column(name = "user_id",
            nullable = false,
            updatable = false)
    private Long userId;

    @Column(name = "filespace_id",
            nullable = false,
            updatable = false)
    private Long filespaceId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFilespaceId() {
        return filespaceId;
    }

    public void setFilespaceId(Long filespaceId) {
        this.filespaceId = filespaceId;
    }

    public UserFilespaceKey() {
    }

    public UserFilespaceKey(Long userId, Long filespaceId) {
        this.userId = userId;
        this.filespaceId = filespaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFilespaceKey)) return false;
        UserFilespaceKey that = (UserFilespaceKey) o;
        return getUserId().equals(that.getUserId()) &&
                getFilespaceId().equals(that.getFilespaceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getFilespaceId());
    }
}
