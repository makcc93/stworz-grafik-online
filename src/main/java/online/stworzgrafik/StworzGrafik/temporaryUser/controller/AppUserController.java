package online.stworzgrafik.StworzGrafik.temporaryUser.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUserService;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.ChangePasswordRequest;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.CreateUserRequest;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.SetEnabledRequest;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('ADMIN')")
public class AppUserController {
    private final AppUserService appUserService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll(){
        return ResponseEntity.ok(appUserService.findAll());
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody @Valid CreateUserRequest createUserRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(appUserService.create(createUserRequest));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                               @RequestBody @Valid ChangePasswordRequest request) {
        appUserService.changePassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<Void> setEnabled(@PathVariable Long id,
                                           @RequestBody @Valid SetEnabledRequest request) {
        appUserService.setEnabled(id, request.enabled());
        return ResponseEntity.noContent().build();
    }
}
