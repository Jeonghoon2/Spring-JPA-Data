package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

    @PersistenceContext
    private EntityManager em;

    public Member save(Member member){
        em.persist(member);
        return member;
    }

    /* 특정 삭제 */
    public void remove(Member member){
        em.remove(member);
    }

    /* 모두 조회 */
    public List<Member> findAll(){
        /* JPQL */
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    /* 특정 조회*/
    public Optional<Member> findById(Long id){
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public long count(){
        return em.createQuery("select count(m) from Member m", Long.class)
                .getSingleResult();
    }

    public Member find(Long id){
        return em.find(Member.class, id);
    }

    public List<Member> findByUsernameAndAgeGreaterThen(String username, int age){
        return em.createQuery("select m from Member m where m.username = :username and m.age > :age", Member.class)
                .setParameter("username",username).setParameter("age",age).getResultList();
    }

    public List<Member> findByUsername(String username){
        return em.createNamedQuery("Member.findByUsername", Member.class)
                .setParameter("username",username)
                .getResultList();
    }

    /* 순수 JPA 페이징 */
    public List<Member> findByPage(int age, int offset, int limit){
        return em.createQuery("select m from Member m where m.age = :age order by m.username desc ", Member.class)
                .setParameter("age",age)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long totalCount(int age){
        return em.createQuery("select count(m) from Member m where m.age = :age", Long.class)
                .setParameter("age", age)
                .getSingleResult();
    }

    /* 순수 JPA 벌크성 수정 쿼리 */
    public int bulkAgePlus(int age){
        return em.createQuery("update Member m set m.age = m.age + 1 " +
                        "where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate();
    }

}
