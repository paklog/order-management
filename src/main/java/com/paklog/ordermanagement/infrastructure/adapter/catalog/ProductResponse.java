package com.paklog.ordermanagement.infrastructure.adapter.catalog;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Product Catalog Service product response.
 * Maps to Product Catalog Service GET /products/{sku} response.
 *
 * This is a simplified version - full Product has dimensions and attributes.
 */
public class ProductResponse {

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("category")
    private String category;

    public ProductResponse() {
    }

    public ProductResponse(String sku, String title, BigDecimal price, Boolean active, String category) {
        this.sku = sku;
        this.title = title;
        this.price = price;
        this.active = active;
        this.category = category;
    }

    // Getters and Setters

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
