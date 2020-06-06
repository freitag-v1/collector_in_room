package swcapstone.freitag.springsecurityjpa.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import java.util.List;

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
                .orderBy(NumberExpression.random().asc())
                .limit(2)
                .fetch();
    }

    private BooleanExpression eqValidationStatus(String validationStatus) {
        if (StringUtils.isEmpty(validationStatus)) {
            return null;
        }
        return problemEntity.validationStatus.eq(validationStatus);
    }
}
