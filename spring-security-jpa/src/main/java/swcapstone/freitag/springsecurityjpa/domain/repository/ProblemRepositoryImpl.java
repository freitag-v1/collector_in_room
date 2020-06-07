package swcapstone.freitag.springsecurityjpa.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import java.util.List;
import java.util.stream.Collectors;

import static swcapstone.freitag.springsecurityjpa.domain.entity.QProblemEntity.problemEntity;

@Repository
@RequiredArgsConstructor
public class ProblemRepositoryImpl implements ProblemRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProblemEntity> crossValidation(String validationStatus) {

        BooleanBuilder builder = new BooleanBuilder();

        return jpaQueryFactory
                .selectFrom(problemEntity)
                .where(eqValidationStatus(validationStatus))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .fetch()
                .stream().limit(2).collect(Collectors.toList());
    }

    @Override
    public List<ProblemEntity> labellingProblem(int projectId, String validationStatus) {

        BooleanBuilder builder = new BooleanBuilder();

        return jpaQueryFactory
                .selectFrom(problemEntity)
                .where(eqProjectId(projectId), eqValidationStatus(validationStatus))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .fetch()
                .stream().limit(2).collect(Collectors.toList());
    }

    private BooleanExpression eqValidationStatus(String validationStatus) {
        if (StringUtils.isEmpty(validationStatus)) {
            return null;
        }
        return problemEntity.validationStatus.eq(validationStatus);
    }

    private BooleanExpression eqProjectId(int projectId) {
        if (StringUtils.isEmpty(projectId)) {
            return null;
        }
        return problemEntity.projectId.eq(projectId);
    }
}
