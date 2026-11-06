package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.model.JobPosting;
import com.tylerandrews.lcg.service.JobPostingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CareerController {

    private final JobPostingService jobPostingService;

    public CareerController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
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
        return "job-detail";
    }
}
