package com.civicfix.demo; // ⚠️ CHECK THIS LINE: If your folder is com.example.demo, change this!

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

// --- DATA MODELS ---

class Report {
    public String id;
    public String type;
    public String dept;
    public String status; 
    public String location;
    public int severity;
    public long daysIgnored;

    public Report(String type, String dept, String status, String loc, int severity, int daysAgo) {
        this.id = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.type = type;
        this.dept = dept;
        this.status = status;
        this.location = loc;
        this.severity = severity;
        this.daysIgnored = daysAgo;
    }
}

class Officer {
    public String name;
    public String role;
    public String dept;
    public String phone;
    public String email;

    public Officer(String n, String r, String d, String p, String e) {
        this.name = n; this.role = r; this.dept = d; this.phone = p; this.email = e;
    }
}

@Controller
public class IssueController {

    // --- MEMORY DATABASES ---
    private static List<Report> reportList = new ArrayList<>();
    private static List<Officer> officerList = new ArrayList<>();
    private static Map<String, String> userDatabase = new HashMap<>();

    public IssueController() {
        // 1. SETUP DEFAULT USERS
        userDatabase.put("admin", "admin123");
        userDatabase.put("user", "user123");

        // 2. FAKE REPORTS
        if(reportList.isEmpty()) {
            reportList.add(new Report("Deep Pothole", "Roads Dept", "Pending", "Madhapur, Hyd", 95, 12));
            reportList.add(new Report("Garbage Dump", "Sanitation", "Pending", "Kondapur, Hyd", 65, 3));
            reportList.add(new Report("Water Leakage", "Water Board", "Resolved", "Gachibowli, Hyd", 0, 0));
        }

        // 3. FAKE OFFICERS (7 Useful Contacts)
        if(officerList.isEmpty()) {
            officerList.add(new Officer("Ramesh Gupta", "Zonal Commissioner", "General Admin", "+91 9876543210", "ramesh.g@ghmc.gov.in"));
            officerList.add(new Officer("Sita Reddy", "Field Inspector", "Sanitation", "+91 9812345678", "sita.r@ghmc.gov.in"));
            officerList.add(new Officer("Vikram Singh", "Chief Engineer", "Roads", "+91 9988776655", "vikram.s@ghmc.gov.in"));
            officerList.add(new Officer("Dr. Anjali Rao", "Health Officer", "Public Health", "+91 8899776655", "health.ho@ghmc.gov.in"));
            officerList.add(new Officer("Control Room", "Emergency Response", "Disaster Mgmt", "100", "control@police.gov.in"));
            officerList.add(new Officer("Traffic Helpline", "Traffic Control", "Traffic Dept", "+91 40 23456789", "traffic@hyd.gov.in"));
            officerList.add(new Officer("IT Support", "System Admin", "Technical Team", "+91 9000012345", "support@civicfix.ai"));
        }
    }

    // --- NAVIGATION ROUTES ---
    @GetMapping("/") public String root() { return "redirect:/login"; }
    @GetMapping("/login") public String loginPage() { return "login"; }
    @GetMapping("/register") public String registerPage() { return "register"; }
    @GetMapping("/logout") public String logout() { return "redirect:/login"; }
    @GetMapping("/home") public String homePage() { return "index"; }

    // --- NEW: USER REPORTS PAGE ---
    @GetMapping("/my-reports")
    public String userReports(Model model) {
        model.addAttribute("reports", reportList); 
        return "my_reports";
    }

    // --- ADMIN DASHBOARD ---
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        long total = reportList.size();
        long critical = reportList.stream()
            .filter(r -> r.severity > 50 && !r.status.equals("Resolved"))
            .count();

        model.addAttribute("reports", reportList);
        model.addAttribute("officers", officerList);
        model.addAttribute("total", total);
        model.addAttribute("critical", critical);
        
        return "admin";
    }

    // --- ADMIN ACTIONS ---
    @GetMapping("/resolve/{id}")
    public String resolveIssue(@PathVariable String id) {
        for(Report r : reportList) {
            if(r.id.equals(id)) {
                r.status = "Resolved";
                r.severity = 0;
                r.daysIgnored = 0;
            }
        }
        return "redirect:/admin";
    }

    @GetMapping("/escalate/{id}")
    public String escalateIssue(@PathVariable String id) {
        for(Report r : reportList) {
            if(r.id.equals(id)) {
                r.status = "ESCALATED ⚠️";
                r.severity = 100;
                r.dept = "Zonal Commissioner (Urgent)";
            }
        }
        return "redirect:/admin";
    }

    @GetMapping("/delete/{id}")
    public String deleteIssue(@PathVariable String id) {
        reportList.removeIf(r -> r.id.equals(id));
        return "redirect:/admin";
    }

    // --- AUTHENTICATION ---
    @PostMapping("/auth")
    public String authenticate(@RequestParam String username, @RequestParam String password, Model model) {
        if(userDatabase.containsKey(username) && userDatabase.get(username).equals(password)) {
            if(username.equals("admin")) return "redirect:/admin";
            return "redirect:/home";
        }
        model.addAttribute("error", "Invalid Username or Password");
        return "login";
    }
    
    @PostMapping("/register-user")
    public String registerUser(@RequestParam String username, @RequestParam String password) {
        userDatabase.put(username, password);
        return "redirect:/login";
    }

    // --- REPORTING LOGIC ---
    @PostMapping("/report")
    public String submitReport(
            @RequestParam("image") MultipartFile file, 
            @RequestParam(value = "category", defaultValue = "General Issue") String category, 
            @RequestParam(value="description", required=false) String desc,
            @RequestParam(value="lat", defaultValue="0.0") String lat, 
            @RequestParam(value="lon", defaultValue="0.0") String lon, 
            Model model) {
        
        String issue = (category.equals("Other") && desc != null && !desc.isEmpty()) ? desc : category;
        String dept = "General Admin";
        int severity = 50;

        if(issue.contains("Pothole") || issue.contains("Road")) { dept = "Roads Dept"; severity = 90; }
        else if(issue.contains("Garbage") || issue.contains("Trash")) { dept = "Sanitation"; severity = 65; }
        else if(issue.contains("Water") || issue.contains("Leak")) { dept = "Water Board"; severity = 85; }
        else if(issue.contains("Electricity") || issue.contains("Light")) { dept = "Electricity Dept"; severity = 75; }

        reportList.add(0, new Report(issue, dept, "Pending", lat + "," + lon, severity, 0));
        
        model.addAttribute("issue", issue);
        model.addAttribute("dept", dept);
        model.addAttribute("severity", severity);
        
        return "success"; 
    }
}