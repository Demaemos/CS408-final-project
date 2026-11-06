package com.tylerandrews.lcg.repository;
 
import com.tylerandrews.lcg.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
 
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findAllByOrderByPostedAtDesc();

    @Query("""
        select j from JobPosting j
        where j.isOpen = true
          and (:department is null or lower(j.department) = lower(:department))
          and (:location is null or lower(j.location) = lower(:location))
        order by j.postedAt desc
        """)
    List<JobPosting> searchOpenJobs(@Param("department") String department,
                                    @Param("location") String location);

    @Query("select distinct j.department from JobPosting j where j.isOpen = true and j.department is not null and j.department <> '' order by j.department")
    List<String> findOpenDepartments();

    @Query("select distinct j.location from JobPosting j where j.isOpen = true and j.location is not null and j.location <> '' order by j.location")
    List<String> findOpenLocations();
}
 