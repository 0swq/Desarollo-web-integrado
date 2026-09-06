package com.autopartes.service.impl;

import com.autopartes.dto.supplier.SupplierRequest;
import com.autopartes.dto.supplier.SupplierResponse;
import com.autopartes.exception.BusinessException;
import com.autopartes.exception.ResourceNotFoundException;
import com.autopartes.model.Supplier;
import com.autopartes.repository.SupplierRepository;
import com.autopartes.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllActiveSuppliers() {
        return supplierRepository.findByActivoTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
        return mapToResponse(supplier);
    }

    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByRuc(request.getRuc().trim())) {
            throw new BusinessException("Ya existe un proveedor registrado con el RUC: " + request.getRuc());
        }

        Supplier supplier = Supplier.builder()
                .ruc(request.getRuc().trim())
                .razonSocial(request.getRazonSocial().trim())
                .contactoNombre(request.getContactoNombre())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .direccion(request.getDireccion())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));

        if (!supplier.getRuc().equals(request.getRuc().trim())
                && supplierRepository.existsByRuc(request.getRuc().trim())) {
            throw new BusinessException("Ya existe otro proveedor con el RUC: " + request.getRuc());
        }

        supplier.setRuc(request.getRuc().trim());
        supplier.setRazonSocial(request.getRazonSocial().trim());
        supplier.setContactoNombre(request.getContactoNombre());
        supplier.setTelefono(request.getTelefono());
        supplier.setEmail(request.getEmail());
        supplier.setDireccion(request.getDireccion());
        if (request.getActivo() != null) {
            supplier.setActivo(request.getActivo());
        }

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
        supplier.setActivo(false);
        supplierRepository.save(supplier);
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .ruc(supplier.getRuc())
                .razonSocial(supplier.getRazonSocial())
                .contactoNombre(supplier.getContactoNombre())
                .telefono(supplier.getTelefono())
                .email(supplier.getEmail())
                .direccion(supplier.getDireccion())
                .activo(supplier.getActivo())
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}
