package uk.gov.digital.ho.hocs.lists.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.lists.dto.DataListRecord;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@Entity
@Table(name = "lists")
@Access(AccessType.FIELD)
@EqualsAndHashCode(of = {"name"})
public class DataList implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @Getter
    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name ="list_id", referencedColumnName = "id")
    @Getter
    @Setter
    private Set<DataListEntity> entities = new HashSet<>();

    @Column(name = "deleted", nullable = false)
    @Getter
    @Setter
    private Boolean deleted = false;

    public DataList(DataListRecord dlr) {
        this.name = dlr.getName();
        if(dlr.getEntities() != null) {
            this.entities = dlr.getEntities().stream().map(DataListEntity::new).collect(Collectors.toSet());
        }
    }
}