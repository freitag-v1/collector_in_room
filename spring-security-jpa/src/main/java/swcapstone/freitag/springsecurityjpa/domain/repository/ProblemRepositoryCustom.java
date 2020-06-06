package swcapstone.freitag.springsecurityjpa.domain.repository;

import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import java.util.List;

public interface ProblemRepositoryCustom {

    List<ProblemEntity> crossValidation(String validationStatus);
    List<ProblemEntity> labellingProblem(int projectId);
}
