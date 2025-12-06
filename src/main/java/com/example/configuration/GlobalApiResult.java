package com.example.configuration;

import com.example.exception.ApiError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalApiResult<T> {
    private UUID id;

    private Date date;

    private ApiError<T> errors;

    private T data;

    public static <T> GlobalApiResult<T> generate(ApiError<T> errors, T Data){
        GlobalApiResult<T> globalApiResult = new GlobalApiResult<>();

        globalApiResult.setId(UUID.randomUUID());

        globalApiResult.setDate(new Date());

        globalApiResult.setData(Data);

        globalApiResult.setErrors(errors);

        return globalApiResult;
    }
}
