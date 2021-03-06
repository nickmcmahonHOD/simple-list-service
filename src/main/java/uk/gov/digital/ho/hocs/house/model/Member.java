package uk.gov.digital.ho.hocs.house.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@NoArgsConstructor
@Entity
@Table(name = "members")
@Access(AccessType.FIELD)
@EqualsAndHashCode(of = {"displayName", "referenceName"})
public class Member implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id")
    private Long houseId;

    @Column(name = "display_name", nullable = false)
    @Getter
    private String displayName;

    @Column(name = "reference_name", nullable = false)
    @Getter
    private String referenceName;

    @Column(name = "deleted", nullable = false)
    @Getter
    @Setter
    private Boolean deleted = false;

    public Member(String displayName) {
        this(displayName, displayName);
    }

    public Member(String displayName, String referenceName){
        this.displayName = toListText(displayName);
        this.referenceName = toListValue(referenceName);
    }

    private static String toListText(String text) {
        text = text.startsWith("\"") ? text.substring(1) : text;
        text = text.endsWith("\"") ? text.substring(0, text.length() - 1) : text;
        return text;
    }

    private static String toListValue(String value) {
        return value.replaceAll(" ", "_")
                .replaceAll("[^a-zA-Z0-9_]+", "")
                .replaceAll("__", "_")
                .toUpperCase();
    }
}