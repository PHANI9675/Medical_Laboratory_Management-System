package com.medlab.inventory.service;

import com.medlab.inventory.dto.LabTestRequest;
import com.medlab.inventory.dto.LabTestResponse;
import com.medlab.inventory.entity.LabTest;
import com.medlab.inventory.exception.DuplicateResourceException;
import com.medlab.inventory.exception.ResourceNotFoundException;
import com.medlab.inventory.repository.LabTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabTestService {

    private final LabTestRepository repo;

    // ── Get All Tests ────────────────────────────────────────────────────────

    public List<LabTestResponse> getAllTests() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    // ── Get Test By ID ───────────────────────────────────────────────────────

    public LabTestResponse getTestById(Long id) {
        LabTest t = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test not found with id: " + id));
        return toResponse(t);
    }

    // ── Create Test ──────────────────────────────────────────────────────────

    public LabTestResponse createTest(LabTestRequest req) {
        if (repo.existsByCode(req.getCode())) {
            throw new DuplicateResourceException(
                    "Test with code '" + req.getCode() + "' already exists");
        }
        LabTest t = new LabTest();
        t.setCode(req.getCode());
        t.setName(req.getName());
        t.setPrice(req.getPrice());
        t.setTurnaroundHours(req.getTurnaroundHours());
        t.setDescription(req.getDescription());
        return toResponse(repo.save(t));
    }

    // ── Update Test ──────────────────────────────────────────────────────────

    public LabTestResponse updateTest(Long id, LabTestRequest req) {
        LabTest t = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test not found with id: " + id));
        t.setName(req.getName());
        t.setPrice(req.getPrice());
        t.setTurnaroundHours(req.getTurnaroundHours());
        t.setDescription(req.getDescription());
        return toResponse(repo.save(t));
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private LabTestResponse toResponse(LabTest t) {
        LabTestResponse r = new LabTestResponse();
        r.setId(t.getId());
        r.setCode(t.getCode());
        r.setName(t.getName());
        r.setPrice(t.getPrice());
        r.setTurnaroundHours(t.getTurnaroundHours());
        r.setDescription(t.getDescription());
        return r;
    }
}