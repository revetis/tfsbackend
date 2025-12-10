package com.example.settings.maindto;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorTemplate<T> {

    private UUID id;
    private boolean success;
    private int status;
    private Date timestamp;
    private String apiURL;
    private T errors;

    public static <T> ApiErrorTemplate<T> apiErrorTemplateGenerator(boolean success, int status, String apiURL,
            T errors) {
        ApiErrorTemplate<T> apiErrorTemplate = new ApiErrorTemplate<>();
        apiErrorTemplate.setId(UUID.randomUUID());
        apiErrorTemplate.setSuccess(success);
        apiErrorTemplate.setStatus(status);
        apiErrorTemplate.setTimestamp(new Date());
        apiErrorTemplate.setApiURL(apiURL);
        apiErrorTemplate.setErrors(errors);
        return apiErrorTemplate;
    }

}
