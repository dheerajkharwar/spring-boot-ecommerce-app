package com.example.ecommerce.home;

public record HomeSlideDto(
        Long id,
        String title,
        String subtitle,
        String imageUrl,
        int position,
        boolean active
) {

    public static HomeSlideDto from(HomeSlide slide) {
        return new HomeSlideDto(
                slide.getId(),
                slide.getTitle(),
                slide.getSubtitle(),
                slide.getImageUrl(),
                slide.getPosition(),
                slide.isActive()
        );
    }
}
