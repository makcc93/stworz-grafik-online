package online.stworzgrafik.StworzGrafik.demo.controller;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.demo.DemoService;
import online.stworzgrafik.StworzGrafik.user.DTO.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/demo")
public class DemoController {
    private final DemoService demoService;

    @GetMapping
    ResponseEntity<AuthResponse> createDemoAccess(){
        return ResponseEntity.ok(demoService.createAccount());
    }
}
