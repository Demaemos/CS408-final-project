package com.tylerandrews.lcg;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "<h1>Hello World!</h1><p>LCG app is live on EC2.</p>";
    }
}