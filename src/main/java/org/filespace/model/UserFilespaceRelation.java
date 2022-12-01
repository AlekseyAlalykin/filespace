package org.filespace.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "users_filespaces")
public class UserFilespaceRelation implements EntityImplementation {

    @NotNull
    @EmbeddedId
    private UserFilespaceKey key = new UserFilespaceKey();

    @NotNull
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @MapsId("filespaceId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filespace_id")
    private Filespace filespace;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",
            nullable = false,
            length = 15)
    private Role role;

    public UserFilespaceRelation() {
    }

    public UserFilespaceRelation(@NotNull UserFilespaceKey key, Role role) {
        this.key = key;
        this.role = role;
    }

    public UserFilespaceKey getKey() {
        return key;
    }

    public void setKey(UserFilespaceKey key) {
        this.key = key;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Filespace getFilespace() {
        return filespace;
    }

    public void setFilespace(Filespace filespace) {
        this.filespace = filespace;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
