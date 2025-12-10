package com.example.settings.maindto;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiTemplate<T, D> {

    private UUID id;
    private boolean success;
    private int status;
    private Date timestamp;
    private String apiURL;
    private T errors;
    private D data;

    public static <T, D> ApiTemplate<T, D> apiTemplateGenerator(boolean success, int status, String apiURL, T errors,
            D data) {
        ApiTemplate<T, D> apiTemplate = new ApiTemplate<>();
        apiTemplate.setId(UUID.randomUUID());
        apiTemplate.setSuccess(success);
        apiTemplate.setStatus(status);
        apiTemplate.setTimestamp(new Date());
        apiTemplate.setApiURL(apiURL);
        apiTemplate.setErrors(errors);
        apiTemplate.setData(data);
        return apiTemplate;

    }

}
