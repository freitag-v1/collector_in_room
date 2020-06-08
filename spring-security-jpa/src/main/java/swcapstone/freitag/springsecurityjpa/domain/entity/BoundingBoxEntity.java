package swcapstone.freitag.springsecurityjpa.domain.entity;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity(name = "bounding_box_table")
public class BoundingBoxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id")
    private int problemId;

    @Column(name = "class_name")
    private String className;

    @Column(name = "coordinates")
    private String coordinates;

    @Builder
    public BoundingBoxEntity(Long id, int problemId, String className, String coordinates) {
        this.id = id;
        this.problemId = problemId;
        this.className = className;
        this.coordinates = coordinates;
    }
}