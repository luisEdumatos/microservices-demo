package br.com.products.api.modules.product.model;

import br.com.products.api.modules.category.dto.CategoryResponse;
import br.com.products.api.modules.category.model.Category;
import br.com.products.api.modules.product.dto.ProductRequest;
import br.com.products.api.modules.supplier.dto.SupplierResponse;
import br.com.products.api.modules.supplier.model.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PRODUCT")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "FK_SUPPLIER", nullable = false)
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "FK_CATEGORY", nullable = false)
    private Category category;

    @Column(name = "QUANTITY_AVAILABLE", nullable = false)
    private Integer quantityAvailable;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public static Product of(ProductRequest request, SupplierResponse supplierResponse, CategoryResponse categoryResponse) {
        Supplier supplier = new Supplier();
        supplier.setId(supplierResponse.getId());
        supplier.setName(supplierResponse.getName());

        Category category = new Category();
        category.setId(categoryResponse.getId());
        category.setDescription(category.getDescription());

        return Product
                .builder()
                .name(request.getName())
                .quantityAvailable(request.getQuantityAvailable())
                .supplier(supplier)
                .category(category)
                .build();
    }
}
