package swcapstone.freitag.springsecurityjpa.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static swcapstone.freitag.springsecurityjpa.domain.entity.QProblemEntity.problemEntity;

@Repository
@RequiredArgsConstructor
public class ProblemRepositoryImpl implements ProblemRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProblemEntity> userValidation(String validationStatus, int limit) {
        BooleanBuilder builder = new BooleanBuilder();

        return jpaQueryFactory
                .selectFrom(problemEntity)
                .where(eqValidationStatus(validationStatus), eqRightAnswer(true))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .fetch()
                .stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<ProblemEntity> crossValidations(String validationStatus, String level, int limit) {

        BooleanBuilder builder = new BooleanBuilder();

        return jpaQueryFactory
                .selectFrom(problemEntity)
                .where(eqValidationStatus(validationStatus), eqLevel(level))
                // .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                // referenceId가 서로 다른 문제 2개를 가져오기!
                .orderBy(problemEntity.referenceId.asc())
                .fetch()
                .stream()
                .filter(distinctByKey(p -> p.getReferenceId()))
                .sorted(new Comparator<ProblemEntity>() {
                    @Override
                    public int compare(ProblemEntity p1, ProblemEntity p2) {
                        return Long.compare(p1.getReferenceId(), p2.getReferenceId());
                    }
                })
                .limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<ProblemEntity> labellingProblem(int projectId, String validationStatus, int limit) {

        return jpaQueryFactory
                .selectFrom(problemEntity)
                .where(eqProjectId(projectId), eqValidationStatus(validationStatus))
                // .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .orderBy(problemEntity.problemId.asc())
                .fetch()
                .stream().limit(limit).collect(Collectors.toList());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new HashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
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

    private BooleanExpression eqLevel(String level) {
        if(StringUtils.isEmpty(level)) {
            return null;
        }
        return problemEntity.level.eq(level);
    }

    private BooleanExpression eqRightAnswer(Boolean rightAnswer) {
        if(StringUtils.isEmpty(rightAnswer)) {
            return null;
        }
        return problemEntity.rightAnswer.eq(rightAnswer);
    }
}
