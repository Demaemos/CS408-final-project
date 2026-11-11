package com.tylerandrews.lcg.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminJobForm {

    @NotBlank(message = "Title is required.")
    @Size(max = 200, message = "Title must be 200 characters or fewer.")
    private String title;

    @Size(max = 200, message = "Department must be 200 characters or fewer.")
    private String department;

    @Size(max = 200, message = "Location must be 200 characters or fewer.")
    private String location;

    @NotBlank(message = "Description is required.")
    @Size(max = 4000, message = "Description must be 4000 characters or fewer.")
    private String description;

    @NotBlank(message = "Requirements are required.")
    @Size(max = 2000, message = "Requirements must be 2000 characters or fewer.")
    private String requirements;

    private boolean open = true;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
}
