package com.tylerandrews.lcg;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "applications")
public class JobApplication {
 
    public enum Status { NEW, REVIEWING, INTERVIEW, REJECTED, HIRED }
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id")
    private JobPosting job;
 
    @Column(nullable = false)
    private String applicantName;
 
    @Column(nullable = false)
    private String email;
 
    @Column(length = 4000)
    private String coverNote;
 
    private String portfolioUrl;
    private String resumeUrl;
 
    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;
 
    @Column(nullable = false, unique = true)
    private String referenceCode;
 
    private LocalDateTime submittedAt = LocalDateTime.now();
 
    public Long getId() { return id; }
    public JobPosting getJob() { return job; }
    public void setJob(JobPosting job) { this.job = job; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCoverNote() { return coverNote; }
    public void setCoverNote(String coverNote) { this.coverNote = coverNote; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getReferenceCode() { return referenceCode; }
    public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
 