package swcapstone.freitag.springsecurityjpa.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;

import java.util.List;

import static swcapstone.freitag.springsecurityjpa.domain.entity.QProjectEntity.*;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProjectEntity> projectSearch(String workType, String dataType, String subject, int difficulty) {

        BooleanBuilder builder = new BooleanBuilder();

        // Querydsl의 where에 조건문을 쓰되 파라미터가 비어있다면, 조건절에서 생략
        return jpaQueryFactory
                .selectFrom(projectEntity)
                .where(eqWorkType(workType),
                        eqDataType(dataType),
                        eqSubject(subject),
                        eqDifficulty(difficulty))
                .fetch();

    }

    @Override
    public List<ProjectEntity> labellingProjectSearch(String workType, String dataType) {

        BooleanBuilder builder = new BooleanBuilder();

        return jpaQueryFactory
                .selectFrom(projectEntity)
                .where(eqWorkType(workType),
                        eqDataType(dataType))
                .orderBy(NumberExpression.random().asc())
                .limit(2)
                .fetch();
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

    private BooleanExpression eqDifficulty(int difficulty) {
        if (StringUtils.isEmpty(difficulty)) {
            return null;
        }

        if( difficulty == -1) {
            return null;
        }
        return projectEntity.difficulty.eq(difficulty);
    }
}
