package com.example.ecommerce.home;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.common.ResourceNotFoundException;

@Service
public class HomeSlideService {

    private final HomeSlideRepository homeSlideRepository;

    public HomeSlideService(HomeSlideRepository homeSlideRepository) {
        this.homeSlideRepository = homeSlideRepository;
    }

    @Transactional(readOnly = true)
    public List<HomeSlideDto> getPublicSlides() {
        return homeSlideRepository.findByActiveTrueOrderByPositionAsc().stream()
                .limit(5)
                .map(HomeSlideDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HomeSlideDto> getAdminSlides() {
        return homeSlideRepository.findAllByOrderByPositionAsc().stream()
                .map(HomeSlideDto::from)
                .toList();
    }

    @Transactional
    public HomeSlideDto create(HomeSlideRequest request) {
        if (homeSlideRepository.count() >= 5) {
            throw new IllegalArgumentException("Homepage slider can contain up to 5 images");
        }
        HomeSlide slide = new HomeSlide();
        applyRequest(slide, request);
        return HomeSlideDto.from(homeSlideRepository.save(slide));
    }

    @Transactional
    public HomeSlideDto update(Long id, HomeSlideRequest request) {
        HomeSlide slide = homeSlideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slide not found: " + id));
        applyRequest(slide, request);
        return HomeSlideDto.from(homeSlideRepository.save(slide));
    }

    @Transactional
    public void delete(Long id) {
        if (!homeSlideRepository.existsById(id)) {
            throw new ResourceNotFoundException("Slide not found: " + id);
        }
        homeSlideRepository.deleteById(id);
    }

    @Transactional
    public void seedSlidesIfEmpty(List<HomeSlide> slides) {
        if (homeSlideRepository.count() > 0) {
            return;
        }
        homeSlideRepository.saveAll(slides.stream().limit(5).toList());
    }

    private void applyRequest(HomeSlide slide, HomeSlideRequest request) {
        slide.setTitle(request.title().trim());
        slide.setSubtitle(request.subtitle().trim());
        slide.setImageUrl(request.imageUrl().trim());
        slide.setPosition(request.position());
        slide.setActive(request.active());
    }
}
