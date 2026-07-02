package com.example.ecommerce.home;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slides")
public class HomeSlideController {

    private final HomeSlideService homeSlideService;

    public HomeSlideController(HomeSlideService homeSlideService) {
        this.homeSlideService = homeSlideService;
    }

    @GetMapping
    public List<HomeSlideDto> publicSlides() {
        return homeSlideService.getPublicSlides();
    }

    @GetMapping("/admin")
    public List<HomeSlideDto> adminSlides() {
        return homeSlideService.getAdminSlides();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HomeSlideDto create(@Valid @RequestBody HomeSlideRequest request) {
        return homeSlideService.create(request);
    }

    @PutMapping("/{id}")
    public HomeSlideDto update(@PathVariable Long id, @Valid @RequestBody HomeSlideRequest request) {
        return homeSlideService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        homeSlideService.delete(id);
    }
}
