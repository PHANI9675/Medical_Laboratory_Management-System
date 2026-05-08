package com.medlab.inventory.service;

import com.medlab.inventory.client.NotificationClient;
import com.medlab.inventory.dto.AdjustInventoryRequest;
import com.medlab.inventory.dto.CreateInventoryRequest;
import com.medlab.inventory.dto.InventoryItemResponse;
import com.medlab.inventory.dto.LowStockNotificationRequest;
import com.medlab.inventory.entity.InventoryItem;
import com.medlab.inventory.exception.DuplicateResourceException;
import com.medlab.inventory.exception.InsufficientStockException;
import com.medlab.inventory.exception.ResourceNotFoundException;
import com.medlab.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryItemRepository repo;
    private final NotificationClient notificationClient;

    @Value("${inventory.notification.admin-username}")
    private String adminUsername;

    // ── Get All Items ────────────────────────────────────────────────────────

    public List<InventoryItemResponse> getAllItems() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    // ── Create New Item ──────────────────────────────────────────────────────

    public InventoryItemResponse createItem(CreateInventoryRequest req) {
        if (repo.existsByItemName(req.getItemName())) {
            throw new DuplicateResourceException(
                    "Item '" + req.getItemName() + "' already exists");
        }
        InventoryItem item = new InventoryItem();
        item.setItemName(req.getItemName());
        item.setQuantity(req.getQuantity());
        item.setUnit(req.getUnit());
        item.setDescription(req.getDescription());
        item.setLowStockThreshold(
                req.getLowStockThreshold() != null ? req.getLowStockThreshold() : 10
        );
        return toResponse(repo.save(item));
    }

    // ── Adjust Stock ─────────────────────────────────────────────────────────

    public InventoryItemResponse adjustStock(AdjustInventoryRequest req) {
        InventoryItem item = repo.findById(req.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory item not found with id: " + req.getItemId()));

        int newQty = item.getQuantity() + req.getQuantityChange();
        if (newQty < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock. Current quantity: " + item.getQuantity());
        }

        item.setQuantity(newQty);
        InventoryItem saved = repo.save(item);

        int threshold = saved.getLowStockThreshold() != null ? saved.getLowStockThreshold() : 10;
        if (newQty <= threshold) {
            sendLowStockAlert(saved, newQty, threshold);
        }

        return toResponse(saved);
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private void sendLowStockAlert(InventoryItem item, int currentQty, int threshold) {
        try {
            String message = String.format(
                    "Low stock alert: '%s' has only %d %s remaining (threshold: %d). Please reorder.",
                    item.getItemName(),
                    currentQty,
                    item.getUnit() != null ? item.getUnit() : "units",
                    threshold
            );
            LowStockNotificationRequest notification =
                    new LowStockNotificationRequest(adminUsername, message, "LOW_STOCK_ALERT");

            notificationClient.sendNotification(notification);
            log.warn("LOW_STOCK_ALERT sent to admin '{}': item='{}' qty={} threshold={}",
                    adminUsername, item.getItemName(), currentQty, threshold);

        } catch (Exception ex) {
            log.error("Failed to send low-stock alert for item='{}': {}",
                    item.getItemName(), ex.getMessage());
        }
    }

    private InventoryItemResponse toResponse(InventoryItem i) {
        InventoryItemResponse r = new InventoryItemResponse();
        r.setId(i.getId());
        r.setItemName(i.getItemName());
        r.setQuantity(i.getQuantity());
        r.setUnit(i.getUnit());
        r.setDescription(i.getDescription());
        r.setLowStockThreshold(i.getLowStockThreshold());
        int threshold = i.getLowStockThreshold() != null ? i.getLowStockThreshold() : 10;
        r.setLowStock(i.getQuantity() <= threshold);
        return r;
    }
}