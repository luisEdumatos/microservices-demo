package br.com.products.api.modules.supplier.service;

import br.com.products.api.config.exception.ValidationException;
import br.com.products.api.modules.supplier.dto.SupplierRequest;
import br.com.products.api.modules.supplier.dto.SupplierResponse;
import br.com.products.api.modules.supplier.model.Supplier;
import br.com.products.api.modules.supplier.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public SupplierResponse save(SupplierRequest request) {
        validateSupplierNameInformed(request);
        var supplier = supplierRepository.save(Supplier.of(request));
        return SupplierResponse.of(supplier);
    }

    private void validateSupplierNameInformed(SupplierRequest request) {
        if (isEmpty(request.getName())) {
            throw new ValidationException("The supplier's name was not informed.");
        }
    }
}
