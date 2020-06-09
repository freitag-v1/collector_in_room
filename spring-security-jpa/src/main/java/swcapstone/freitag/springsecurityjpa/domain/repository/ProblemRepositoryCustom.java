package swcapstone.freitag.springsecurityjpa.domain.repository;

import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import java.util.List;

public interface ProblemRepositoryCustom {

    List<ProblemEntity> validations(String validationStatus, int limit);
    List<ProblemEntity> labellingProblem(int projectId, String validationStatus, int limit);
}
