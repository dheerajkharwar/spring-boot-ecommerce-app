package com.example.ecommerce.home;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeSlideRepository extends JpaRepository<HomeSlide, Long> {

    List<HomeSlide> findByActiveTrueOrderByPositionAsc();

    List<HomeSlide> findAllByOrderByPositionAsc();
}
