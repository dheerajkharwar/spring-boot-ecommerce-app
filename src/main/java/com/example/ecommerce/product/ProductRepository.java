package com.example.ecommerce.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    List<Product> findByFeaturedTrueOrderByRatingDesc();

    List<Product> findByStockLessThanEqualOrderByStockAsc(int stock);

    @Query("""
            select p from Product p
            where (:category is null or p.category = :category)
              and (:query is null or lower(p.name) like lower(concat('%', :query, '%'))
                   or lower(p.brand) like lower(concat('%', :query, '%'))
                   or lower(p.description) like lower(concat('%', :query, '%')))
            order by p.featured desc, p.rating desc, p.name asc
            """)
    List<Product> search(Category category, String query);
}
