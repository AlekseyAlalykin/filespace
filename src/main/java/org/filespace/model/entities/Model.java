package org.filespace.model.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.filespace.model.EntityImplementation;

import javax.persistence.*;

@MappedSuperclass
public class Model implements EntityImplementation {

    @Id
    @JsonAlias({"fileId", "userId", "filespaceId"})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    public Model() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Model)) return false;
        Model model = (Model) o;
        return id.equals(model.id);
    }

}
