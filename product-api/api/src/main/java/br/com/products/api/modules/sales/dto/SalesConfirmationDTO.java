package br.com.products.api.modules.sales.dto;

import br.com.products.api.modules.sales.enums.SalesStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesConfirmationDTO {

    private String salesId;
    private SalesStatus status;

}
