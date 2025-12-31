package com.example.apps.shipments.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeliverReturnRequest {

    @NotNull
    private Boolean isReturn;

    @NotNull
    private Boolean willAccept;

    @NotNull
    private String providerServiceCode;

    @NotNull
    private Integer count;

    @NotNull
    @Valid
    private GeliverRecipientAddressRequest senderAddress;
}
