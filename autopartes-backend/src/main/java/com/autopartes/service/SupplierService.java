package com.autopartes.service;

import com.autopartes.dto.supplier.SupplierRequest;
import com.autopartes.dto.supplier.SupplierResponse;

import java.util.List;

public interface SupplierService {
    List<SupplierResponse> getAllActiveSuppliers();
    List<SupplierResponse> getAllSuppliers();
    SupplierResponse getSupplierById(Long id);
    SupplierResponse createSupplier(SupplierRequest request);
    SupplierResponse updateSupplier(Long id, SupplierRequest request);
    void deleteSupplier(Long id);
}
