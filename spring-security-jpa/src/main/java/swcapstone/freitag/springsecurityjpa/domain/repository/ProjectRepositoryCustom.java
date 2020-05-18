package swcapstone.freitag.springsecurityjpa.domain.repository;

import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;

import java.util.List;

public interface ProjectRepositoryCustom {

    List<ProjectEntity> findDynamicQuery(String workType, String dataType, String subject, int difficulty);
}
