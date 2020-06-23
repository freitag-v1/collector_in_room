package swcapstone.freitag.springsecurityjpa.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;

import java.util.List;
import java.util.stream.Collectors;

import static swcapstone.freitag.springsecurityjpa.domain.entity.QProjectEntity.*;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProjectEntity> projectSearch(String workType, String dataType, String subject) {

        BooleanBuilder builder = new BooleanBuilder();

        // Querydsl의 where에 조건문을 쓰되 파라미터가 비어있다면, 조건절에서 생략
        return jpaQueryFactory
                .selectFrom(projectEntity)
                .where(eqWorkType(workType),
                        eqDataType(dataType),
                        eqSubject(subject),
                        eqStatus("진행중")
                        )
                .fetch();

    }

    @Override
    public List<ProjectEntity> labellingProjectSearch(String workType, String dataType, int limit) {

        BooleanBuilder builder = new BooleanBuilder();

        return jpaQueryFactory
                .selectFrom(projectEntity)
                .where(eqWorkType(workType),
                        eqDataType(dataType),
                        eqStatus("진행중"))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .fetch()
                .stream().limit(limit).collect(Collectors.toList());
    }

    private BooleanExpression eqWorkType(String workType) {
        if (StringUtils.isEmpty(workType)) {
            return null;
        }
        return projectEntity.workType.eq(workType);
    }

    private BooleanExpression eqDataType(String dataType) {
        if (StringUtils.isEmpty(dataType)) {
            return null;
        }
        return projectEntity.dataType.eq(dataType);
    }

    private BooleanExpression eqSubject(String subject) {
        if (StringUtils.isEmpty(subject)) {
            return null;
        }
        return projectEntity.subject.eq(subject);
    }

    private BooleanExpression eqStatus(String status) {
        if (StringUtils.isEmpty(status)) {
            return null;
        }
        return projectEntity.status.eq(status);
    }
}
