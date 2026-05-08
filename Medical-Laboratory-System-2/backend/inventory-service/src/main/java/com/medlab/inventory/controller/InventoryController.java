package com.medlab.inventory.controller;

import com.medlab.inventory.dto.AdjustInventoryRequest;
import com.medlab.inventory.dto.CreateInventoryRequest;
import com.medlab.inventory.dto.InventoryItemResponse;
import com.medlab.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Manage lab supplies and stock levels")
public class InventoryController {

    private final InventoryService service;

    @GetMapping
    @Operation(summary = "View all inventory items and their stock levels")
    public ResponseEntity<List<InventoryItemResponse>> getAllItems() {
        return ResponseEntity.ok(service.getAllItems());
    }

    @PostMapping
    @Operation(
            summary = "Add a new inventory item",
            description = "Creates a new inventory item in the database. Only ADMIN can perform this action."
    )
    public ResponseEntity<InventoryItemResponse> createItem(
            @Valid @RequestBody CreateInventoryRequest req) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.createItem(req));
    }

    @PostMapping("/adjust")
    @Operation(
            summary = "Add or reduce stock for an inventory item",
            description = "Use positive quantityChange to add stock, negative to reduce"
    )
    public ResponseEntity<InventoryItemResponse> adjustStock(
            @Valid @RequestBody AdjustInventoryRequest req) {
        return ResponseEntity.ok(service.adjustStock(req));
    }
}