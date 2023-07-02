package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() throws Exception{
        //given
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        //when
        Member findMember = memberRepository.findById(savedMember.getId()).get();

        //then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() throws Exception{
        Member member1 = new Member("memberA");
        Member member2 = new Member("memberB");

        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        /* 리스트 조회 검증 */
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        /* 카운트 검증 */
        long afterCount = memberRepository.count();
        assertThat(afterCount).isEqualTo(2);

        /* 맴버 삭제 검증 */
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long beforeCount = memberRepository.count();
        assertThat(beforeCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen() throws Exception{
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(members.size()).isEqualTo(1);
        assertThat(members.get(0)).isEqualTo(m2);

    }

    @Test
    public void findUserNameList(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> userNameList = memberRepository.findUserNameList();
        for (String s : userNameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto(){

        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();

        System.out.println("memberDto = " + memberDto);

    }


    @Test
    public void findByNames(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> byNames = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member byName : byNames) {

            System.out.println("byName = " + byName);
        }
    }

    @Test
    public void returnType(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        /* 컬렉션을 반환 할때 Null이 아닌 Empty List가 반환됨 */
        List<Member> aaa = memberRepository.findListByUsername("AAA");

        /* 단건 조회시 Null을 반환*/
        Member aaa1 = memberRepository.findMemberByUsername("AAA");
    }

    /* 스프링 데이터 JPA 페이징 정렬*/
    @Test
    public void paging(){
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));
        memberRepository.save(new Member("member6",10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        for (Member member : content) {
            System.out.println("member = " + member);
        }

        assertThat(content.size()).isEqualTo(3);
        assertThat(totalElements).isEqualTo(6);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    /* 벌크성 수정 쿼리 테스트 */
    /* 벌크 연산후 항상 영속성 컨텍스트를 날려버려야 한다. */
    @Test
    public void bulkUpdate(){
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",20));
        memberRepository.save(new Member("member3",21));
        memberRepository.save(new Member("member4",40));
        memberRepository.save(new Member("member5",50));

        int resultCount = memberRepository.bulkAgeUpdate(20);

        /* 이 방안 대신 Modify 어노테이션에 clearAutomatically = true 로 설정*/
        em.clear();

        assertThat(resultCount).isEqualTo(4);
    }

    @Test
    public void findMemberLazy(){
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(new Member("member1",10, teamA));
        memberRepository.save(new Member("member2",20, teamB));

        em.flush();
        em.clear();

        //when
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            /* 아무 방안을 사용하지 않고 그냥 Team을 조회 시 */
            /* Team은 member를 조회할 때 Lazy로 설정했서 Proxy 데이터가 저장 된다.*/
            /* 그렇기 때문에 Team에 관련된 데이터를 조회 할 시 N+1 문제 발생 */
            System.out.println("member.team.class = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    /* Hint & Lock */
    @Test
    public void queryHint(){
        // given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");
        em.flush();

    }
    @Test
    public void lock(){
        // given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        List<Member> member11 = memberRepository.findLockByUsername("member1");

    }


}