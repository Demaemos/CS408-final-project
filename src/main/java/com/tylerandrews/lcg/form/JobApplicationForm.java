package com.tylerandrews.lcg.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public class JobApplicationForm {

    @NotBlank(message = "Please enter your name.")
    @Size(max = 120, message = "Name must be 120 characters or fewer.")
    private String applicantName;

    @NotBlank(message = "Please enter your email.")
    @Email(message = "Please enter a valid email address.")
    @Size(max = 255, message = "Email must be 255 characters or fewer.")
    private String email;

    @NotBlank(message = "Please add a short cover note.")
    @Size(max = 4000, message = "Cover note must be 4000 characters or fewer.")
    private String coverNote;

    @URL(message = "Please enter a valid URL.")
    @Size(max = 500, message = "Portfolio URL must be 500 characters or fewer.")
    private String portfolioUrl;

    @URL(message = "Please enter a valid URL.")
    @Size(max = 500, message = "Resume URL must be 500 characters or fewer.")
    private String resumeUrl;

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCoverNote() { return coverNote; }
    public void setCoverNote(String coverNote) { this.coverNote = coverNote; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl == null || portfolioUrl.isBlank() ? null : portfolioUrl;
    }
    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl == null || resumeUrl.isBlank() ? null : resumeUrl;
    }
}
