package com.tylerandrews.lcg.service;

import com.tylerandrews.lcg.form.JobApplicationForm;
import com.tylerandrews.lcg.model.JobApplication;
import com.tylerandrews.lcg.model.JobPosting;
import com.tylerandrews.lcg.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class JobApplicationService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private final JobApplicationRepository jobApplicationRepository;
    private final Random random = new SecureRandom();

    public JobApplicationService(JobApplicationRepository jobApplicationRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
    }

    public List<JobApplication> findByJob(JobPosting job) {
        return jobApplicationRepository.findByJobOrderBySubmittedAtDesc(job);
    }

    public List<JobApplication> findAll() {
        return jobApplicationRepository.findAllByOrderBySubmittedAtDesc();
    }

    public Optional<JobApplication> findById(Long id) {
        return jobApplicationRepository.findById(id);
    }

    public Optional<JobApplication> lookup(JobPosting job, String email, String referenceCode) {
        if (email == null || referenceCode == null) {
            return Optional.empty();
        }
        String normalizedEmail = email.trim();
        String normalizedCode = referenceCode.trim();
        if (normalizedEmail.isEmpty() || normalizedCode.isEmpty()) {
            return Optional.empty();
        }
        return jobApplicationRepository.findByJobAndEmailAndReferenceCode(job, normalizedEmail, normalizedCode);
    }

    public Optional<JobApplication> lookupByEmailAndCode(String email, String referenceCode) {
        if (email == null || referenceCode == null) {
            return Optional.empty();
        }
        String normalizedEmail = email.trim();
        String normalizedCode = referenceCode.trim();
        if (normalizedEmail.isEmpty() || normalizedCode.isEmpty()) {
            return Optional.empty();
        }
        return jobApplicationRepository.findByEmailAndReferenceCode(normalizedEmail, normalizedCode);
    }

    public JobApplication submit(JobPosting job, JobApplicationForm form) {
        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setApplicantName(form.getApplicantName().trim());
        application.setEmail(form.getEmail().trim());
        application.setCoverNote(form.getCoverNote() == null ? null : form.getCoverNote().trim());
        application.setPortfolioUrl(normalizeUrl(form.getPortfolioUrl()));
        application.setResumeUrl(normalizeUrl(form.getResumeUrl()));
        application.setReferenceCode(generateUniqueReferenceCode());
        return jobApplicationRepository.save(application);
    }

    public JobApplication updateStatus(JobApplication application, JobApplication.Status status) {
        application.setStatus(status);
        return jobApplicationRepository.save(application);
    }

    public void withdraw(JobApplication application) {
        jobApplicationRepository.delete(application);
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return url.trim();
    }

    private String generateUniqueReferenceCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            code = "LCG-" + sb;
        } while (jobApplicationRepository.findByReferenceCode(code).isPresent());
        return code;
    }
}
