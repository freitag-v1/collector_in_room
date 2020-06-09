package swcapstone.freitag.springsecurityjpa.domain.repository;

import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;

import java.util.List;

public interface ProjectRepositoryCustom {

    List<ProjectEntity> projectSearch(String workType, String dataType, String subject, int difficulty);
    List<ProjectEntity> labellingProjectSearch(String workType, String dataType, int limit);
}
