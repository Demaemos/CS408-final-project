package com.tylerandrews.lcg.service;

import com.tylerandrews.lcg.model.JobPosting;
import com.tylerandrews.lcg.repository.JobApplicationRepository;
import com.tylerandrews.lcg.repository.JobPostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public JobPostingService(JobPostingRepository jobPostingRepository,
                             JobApplicationRepository jobApplicationRepository) {
        this.jobPostingRepository = jobPostingRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    public List<JobPosting> search(String department, String location) {
        return jobPostingRepository.searchOpenJobs(normalize(department), normalize(location));
    }

    public List<JobPosting> allJobs() {
        return jobPostingRepository.findAllByOrderByPostedAtDesc();
    }

    public Optional<JobPosting> findById(Long id) {
        return jobPostingRepository.findById(id);
    }

    public JobPosting save(JobPosting jobPosting) {
        return jobPostingRepository.save(jobPosting);
    }

    @Transactional
    public void delete(JobPosting jobPosting) {
        jobApplicationRepository.deleteByJob(jobPosting);
        jobPostingRepository.delete(jobPosting);
    }

    public List<String> departments() {
        return jobPostingRepository.findOpenDepartments();
    }

    public List<String> locations() {
        return jobPostingRepository.findOpenLocations();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
