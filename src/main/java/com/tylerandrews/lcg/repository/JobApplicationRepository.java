package com.tylerandrews.lcg.repository;

import com.tylerandrews.lcg.model.JobApplication;
import com.tylerandrews.lcg.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByJobOrderBySubmittedAtDesc(JobPosting job);

    List<JobApplication> findAllByOrderBySubmittedAtDesc();

    @Query("select a from JobApplication a where a.job = :job and lower(a.email) = lower(:email) and a.referenceCode = :referenceCode")
    Optional<JobApplication> findByJobAndEmailAndReferenceCode(@Param("job") JobPosting job,
                                                             @Param("email") String email,
                                                             @Param("referenceCode") String referenceCode);

    @Query("select a from JobApplication a where lower(a.email) = lower(:email) and a.referenceCode = :referenceCode")
    Optional<JobApplication> findByEmailAndReferenceCode(@Param("email") String email,
                                                       @Param("referenceCode") String referenceCode);

    Optional<JobApplication> findByReferenceCode(String referenceCode);
}
