package com.tylerandrews.lcg.repository;
 
import com.tylerandrews.lcg.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
}
 