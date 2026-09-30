package com.tylerandrews.lcg; // adjust to match your actual base package
 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
 
// Drop this class anywhere in your Spring Boot source tree (adjust the
// package line above to match). It only needs to return HTTP 200 so
// deploy.sh's health check has a real endpoint to hit.
@RestController
public class HealthController {
 
    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}
 