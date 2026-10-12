package com.example.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Random;
import org.springframework.data.domain.Sort;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final Random rand = new Random();

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product newProduct, String vendorName) {
        String requestProductName = newProduct.getProductName();
        BigDecimal requestProductPrice = newProduct.getProductPrice();
        int requestProductQuantity = newProduct.getProductQuantity();
        String generatedSKU = generateProductSKU();
        Long requestedWarehouseLocation = newProduct.getWarehouseId();

        if (!productRepository.findByProductNameIgnoreCase(requestProductName).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product already exists");
        }

        newProduct.setProductName(requestProductName);
        newProduct.setProductPrice(requestProductPrice);
        newProduct.setProductQuantity(requestProductQuantity);
        newProduct.setProductSKU(generatedSKU);
        newProduct.setVendorName(vendorName);
        newProduct.setWarehouseId(requestedWarehouseLocation);
        productRepository.save(newProduct);
        return newProduct;

    }

    public String generateProductSKU() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();

    }

    public Product updateProduct(Product newProduct) {
        Long requestId = newProduct.getId();

        Product existingProduct = productRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        existingProduct.setProductName(newProduct.getProductName());
        existingProduct.setProductPrice(newProduct.getProductPrice());
        existingProduct.setProductQuantity(newProduct.getProductQuantity());
        existingProduct.setVendorNotes(newProduct.getVendorNotes());
        existingProduct.setWarehouseId(newProduct.getWarehouseId());

        return productRepository.save(existingProduct);
    }

    public List<Product> getProducts() {
        return productRepository.findAll(Sort.by(Product::getId));
    }

    public List<Product> searchProductsByName(String productName) {
        return productRepository.findByProductNameContainingIgnoreCase(productName == null ? "" : productName.trim());
    }

    public boolean checkProductExists(String productName) {
        return productRepository.existsByProductNameIgnoreCase(productName);
    }

    public boolean checkProductExistsById(Long productId) {
        return productRepository.existsById(productId);
    }

    public Product searchProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product does not exist!"));
    }

    public void updateProductQuantity(Long productId) {

        Product updatedProduct = searchProductById(productId);
        int currentQuantity = updatedProduct.getProductQuantity();
        if (currentQuantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is out of stock");
        }

        int newQuantity = currentQuantity - 1;

        updatedProduct.setProductQuantity(newQuantity);
        productRepository.save(updatedProduct);
    }

    public boolean productInStock(Long productId) {
        Product checkProduct = searchProductById(productId);
        int stock = checkProduct.getProductQuantity();
        if (stock > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void deleteProduct(Long productId) {

        try {
            productRepository.deleteById(productId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product does not exist!");
        }
    }

    public List<Product> getFilteredProducts(String filter, String businessName) {
        List<Product> filteredProducts = new ArrayList<>();

        switch (filter) {
            case "priceAsc":
                filteredProducts = productRepository.findAll(Sort.by(Sort.Direction.ASC, "productPrice"));
                break;
            case "priceDesc":
                filteredProducts = productRepository.findAll(Sort.by(Sort.Direction.DESC, "productPrice"));
                break;
            case "outOfStockProductsFilter":
                filteredProducts = productRepository.findOutOfStockProducts();
                break;
            case "SKUAsc":
                filteredProducts = productRepository.findAll(Sort.by(Sort.Direction.ASC, "productSKU"));
                break;
            case "SKUDesc":
                filteredProducts = productRepository.findAll(Sort.by(Sort.Direction.DESC, "productSKU"));
                break;
            case "vendorAsc":
                filteredProducts = productRepository.findAll(Sort.by(Sort.Direction.ASC, "vendorName"));
                break;
            case "vendorDesc":
                filteredProducts = productRepository.findAll(Sort.by(Sort.Direction.DESC, "vendorName"));
                break;
            case "outOfStockCurrentVendor":
                filteredProducts = productRepository.outOfStockProductsCurrentVendor(
                        businessName, Sort.by(Sort.Direction.ASC, "productName"));
                break;

            default:
                filteredProducts = productRepository.findAll();

        }

        return filteredProducts;
    }

}
