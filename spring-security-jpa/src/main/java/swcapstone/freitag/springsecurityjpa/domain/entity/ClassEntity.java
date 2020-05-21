package swcapstone.freitag.springsecurityjpa.domain.entity;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity(name = "class_table")
public class ClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private int projectId;

    @Column(name = "class_name")
    private String className;

    @Builder
    public ClassEntity(Long id, int projectId, String className) {
        this.id = id;
        this.projectId = projectId;
        this.className = className;
    }
}
