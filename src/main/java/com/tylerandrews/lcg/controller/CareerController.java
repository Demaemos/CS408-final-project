package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.form.JobApplicationForm;
import com.tylerandrews.lcg.model.JobApplication;
import com.tylerandrews.lcg.model.JobPosting;
import com.tylerandrews.lcg.service.JobApplicationService;
import com.tylerandrews.lcg.service.JobPostingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class CareerController {

    private final JobPostingService jobPostingService;
    private final JobApplicationService jobApplicationService;

    public CareerController(JobPostingService jobPostingService,
                           JobApplicationService jobApplicationService) {
        this.jobPostingService = jobPostingService;
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping("/careers")
    public String careers(@RequestParam(required = false) String department,
                          @RequestParam(required = false) String location,
                          Model model) {
        model.addAttribute("jobs", jobPostingService.search(department, location));
        model.addAttribute("departments", jobPostingService.departments());
        model.addAttribute("locations", jobPostingService.locations());
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedLocation", location);
        return "careers";
    }

    @GetMapping("/careers/{id}")
    public String detail(@PathVariable Long id, Model model) {
        JobPosting job = jobPostingService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        model.addAttribute("job", job);
        model.addAttribute("jobApplications", jobApplicationService.findByJob(job));
        if (!model.containsAttribute("applicationForm")) {
            model.addAttribute("applicationForm", new JobApplicationForm());
        }
        return "job-detail";
    }

    @PostMapping("/careers/{id}/apply")
    public String apply(@PathVariable Long id,
                        @Valid @ModelAttribute("applicationForm") JobApplicationForm form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        JobPosting job = jobPostingService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (result.hasErrors()) {
            model.addAttribute("job", job);
            model.addAttribute("jobApplications", jobApplicationService.findByJob(job));
            return "job-detail";
        }

        JobApplication application = jobApplicationService.submit(job, form);
        redirect.addFlashAttribute("applicationSuccess", "Application submitted. Your reference code is " + application.getReferenceCode() + ".");
        redirect.addFlashAttribute("applicationReference", application.getReferenceCode());
        return "redirect:/careers/" + id;
    }

    @PostMapping({"/careers/{id}/lookup", "/careers/{id}/check"})
    public String lookup(@PathVariable Long id,
                         @RequestParam String email,
                         @RequestParam String referenceCode,
                         RedirectAttributes redirect) {
        JobPosting job = jobPostingService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        Optional<JobApplication> application = jobApplicationService.lookup(job, email, referenceCode);
        if (application.isPresent()) {
            redirect.addFlashAttribute("lookupApplication", application.get());
            redirect.addFlashAttribute("lookupSuccess", true);
            redirect.addFlashAttribute("lookupMessage", "We found your application.");
        } else {
            redirect.addFlashAttribute("lookupError", "No application matched that email and reference code.");
        }
        return "redirect:/careers/" + id;
    }

    @PostMapping("/careers/{id}/withdraw")
    public String withdraw(@PathVariable Long id,
                           @RequestParam String email,
                           @RequestParam String referenceCode,
                           RedirectAttributes redirect) {
        JobPosting job = jobPostingService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        Optional<JobApplication> application = jobApplicationService.lookup(job, email, referenceCode);
        if (application.isEmpty()) {
            redirect.addFlashAttribute("withdrawError", "We could not find that application to withdraw.");
            return "redirect:/careers/" + id;
        }

        jobApplicationService.withdraw(application.get());
        redirect.addFlashAttribute("withdrawSuccess", "Your application has been withdrawn.");
        return "redirect:/careers/" + id;
    }
}
