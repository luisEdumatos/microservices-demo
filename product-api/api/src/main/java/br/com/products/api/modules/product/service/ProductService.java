package br.com.products.api.modules.product.service;

import br.com.products.api.config.exception.SuccessResponse;
import br.com.products.api.config.exception.ValidationException;
import br.com.products.api.modules.category.service.CategoryService;
import br.com.products.api.modules.product.dto.*;
import br.com.products.api.modules.product.model.Product;
import br.com.products.api.modules.product.repository.ProductRepository;
import br.com.products.api.modules.sales.client.SalesClient;
import br.com.products.api.modules.sales.dto.SalesConfirmationDTO;
import br.com.products.api.modules.sales.enums.SalesStatus;
import br.com.products.api.modules.sales.rabbitmq.SalesConfirmationSender;
import br.com.products.api.modules.supplier.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SalesConfirmationSender salesConfirmationSender;
    @Autowired
    private SalesClient salesClient;

    public List<ProductResponse> findAll() {
        return productRepository
                .findAll()
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public Product findById(Integer id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException("There's no product for the given ID."));
    }

    public List<ProductResponse> findByName(String name) {
        if (isEmpty(name)) {
            throw new ValidationException("The Product's name must be informed. ");
        }

        return productRepository
                .findByNameIgnoreCaseContaining(name)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findByCategoryId(Integer categoryId) {
        if (isEmpty(categoryId)) {
            throw new ValidationException("The Product's category Id must be informed. ");
        }

        return productRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findBySupplierId(Integer supplierId) {
        if (isEmpty(supplierId)) {
            throw new ValidationException("The Product's supplier Id must be informed. ");
        }

        return productRepository
                .findBySupplierId(supplierId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }


    public ProductResponse save(ProductRequest request) {
        validateProductDataInformed(request);
        validateCategoryAndSupplierIdInformed(request);
        var category = categoryService.findById(request.getCategoryId());
        var supplier = supplierService.findById(request.getSupplierId());
        var product = productRepository.save(Product.of(request, supplier, category));
        return ProductResponse.of(product);
    }

    public ProductResponse update(ProductRequest request, Integer id) {
        validateProductDataInformed(request);
        findById(id);
        var category = categoryService.findById(request.getCategoryId());
        var supplier = supplierService.findById(request.getSupplierId());
        var product = Product.of(request, supplier, category);
        product.setId(id);
        productRepository.save(product);
        return ProductResponse.of(product);
    }

    private void validateProductDataInformed(ProductRequest request) {
        if (isEmpty(request.getName())) {
            throw new ValidationException("The product's name was not informed.");
        }
        if (isEmpty(request.getQuantityAvailable())) {
            throw new ValidationException("The product's quantity was not informed.");
        }
        if (request.getQuantityAvailable() <= 0) {
            throw new ValidationException("The quantity should not be less or equal to zero.");
        }
    }

    private void validateCategoryAndSupplierIdInformed(ProductRequest request) {
        if (isEmpty(request.getCategoryId())) {
            throw new ValidationException("The category ID was not informed.");
        }
        if (isEmpty(request.getSupplierId())) {
            throw new ValidationException("The supplier ID was not informed.");
        }
    }

    public SuccessResponse delete(Integer id) {
        productRepository.deleteById(id);
        return SuccessResponse.create("The product was deleted");
    }

    public void updateProductStock(ProductStockDTO product) {
        try {
            validateStockUpdateData(product);
            updateStock(product);
        } catch (Exception ex) {
            log.error("Error while trying to update stock for message with error: {}", ex.getMessage(), ex);
            salesConfirmationSender.sendSalesConfirmationMessage(new SalesConfirmationDTO(product.getSalesId(), SalesStatus.REJECTED));
        }
    }

    @Transactional
    private void updateStock(ProductStockDTO product) {
        var productsForUpdate = new ArrayList<Product>();
        product.getProducts()
                .forEach(salesProduct -> {
                    var existingProduct = findById(salesProduct.getProductId());
                    if (salesProduct.getQuantity() > existingProduct.getQuantityAvailable()) {
                        throw new ValidationException(String.format("The product %s is out of stock.", existingProduct.getId()));
                    }
                    existingProduct.updateStock(salesProduct.getQuantity());
                    productsForUpdate.add(existingProduct);
                });
        if (!isEmpty(productsForUpdate)) {
            productRepository.saveAll(productsForUpdate);
            var approvedMessage = new SalesConfirmationDTO(product.getSalesId(), SalesStatus.APPROVED);
            salesConfirmationSender.sendSalesConfirmationMessage(approvedMessage);
        }
    }

    private void validateStockUpdateData(ProductStockDTO product) {
        if (isEmpty(product) || isEmpty(product.getSalesId())) {
            throw new ValidationException("The product data and the sales ID must be informed.");
        }
        if (isEmpty(product.getProducts())) {
            throw new ValidationException("The sales' products must be informed.");
        }

        product.getProducts()
                .forEach(salesProduct -> {
                    if (isEmpty(salesProduct.getQuantity()) || isEmpty(salesProduct.getProductId())) {
                        throw new ValidationException("The productID and the quantity must be informed.");
                    }
                });
    }

    public Boolean existsByCategoryId(Integer categoryId) {
        return productRepository.existsByCategoryId(categoryId);
    }

    public Boolean existsBySupplierId(Integer supplierId) {
        return productRepository.existsBySupplierId(supplierId);
    }

    public ProductSalesResponse findProductSales(Integer id) {
        var product = findById(id);
        try {
            var sales = salesClient
                    .findSalesByProductId(product.getId())
                    .orElseThrow(() -> new ValidationException("The sales was not found by this product. "));
            return ProductSalesResponse.of(product, sales.getSalesId());
        } catch (Exception ex) {
            throw new ValidationException("There was an error trying to get the product's sales. ");
        }
    }

    public SuccessResponse checkProductsStock(ProductCheckStockRequest request) {
        if (isEmpty(request) || isEmpty(request.getProducts())) {
            throw new ValidationException("The request data and products must be informed. ");
        }
        request.getProducts()
                .forEach(this::validateStock);

        return SuccessResponse.create("The stock is ok!");
    }

    private void validateStock(ProductQuantityDTO productQuantity) {
        if (isEmpty(productQuantity.getProductId()) || isEmpty(productQuantity.getQuantity())) {
            throw  new ValidationException("Product ID and quantity must be informed. ");
        }
        var product = findById(productQuantity.getProductId());
        if (productQuantity.getQuantity() > product.getQuantityAvailable()) {
            throw new ValidationException(String.format("The product %s is out of stock", product.getId()));
        }
    }
}
