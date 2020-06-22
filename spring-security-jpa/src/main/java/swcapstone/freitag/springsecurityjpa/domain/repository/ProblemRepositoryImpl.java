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
    public List<ProblemEntity> validations(String validationStatus, int limit) {

        BooleanBuilder builder = new BooleanBuilder();

        return jpaQueryFactory
                .selectFrom(problemEntity)
                .where(eqValidationStatus(validationStatus))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .fetch()
                .stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<ProblemEntity> labellingProblem(int projectId, String validationStatus, int limit) {

        return jpaQueryFactory
                .selectFrom(problemEntity)
                .where(eqProjectId(projectId), eqValidationStatus(validationStatus))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .fetch()
                .stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public long countRightProblems(String userId, String validationStatus) {

        return jpaQueryFactory
                .selectFrom(problemEntity)
                .where(eqUserId(userId), eqValidationStatus(validationStatus))
                .where(problemEntity.answer.eq(problemEntity.finalAnswer))
                .fetchCount();
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

    private BooleanExpression eqUserId(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        return problemEntity.userId.eq(userId);
    }
}
