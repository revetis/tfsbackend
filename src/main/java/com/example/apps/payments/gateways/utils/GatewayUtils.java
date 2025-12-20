package com.example.apps.payments.gateways.utils;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.Arrays;
import com.example.tfs.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GatewayUtils {

    @Autowired
    private ApplicationProperties applicationProperties;

    public static String generateConversationId(String orderNumber) {
        return String.format("%s-%d-%s", orderNumber, System.currentTimeMillis(),
                UUID.randomUUID().toString().substring(0, 8));
    }

    public boolean isSignatureValid(String signature, Object... params) {
        try {
            String dataToEncrypt = Arrays.stream(params)
                    .map(this::formatParam)
                    .collect(Collectors.joining(":"));

            log.info("Data to encrypt for signature validation: {}", dataToEncrypt);

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    applicationProperties.getIYZICO_SECRET_KEY().getBytes(), "HmacSHA256");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);

            byte[] encryptedBytes = mac.doFinal(dataToEncrypt.getBytes());
            String calculatedSignature = Hex.encodeHexString(encryptedBytes);

            return calculatedSignature.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("Error during signature validation: {}", e.getMessage());
            return false;
        }
    }

    private String formatParam(Object param) {
        if (param == null)
            return "";

        if (param instanceof BigDecimal) {
            return ((BigDecimal) param).stripTrailingZeros().toPlainString();
        }

        String strParam = param.toString();
        if (strParam.contains(".") && isNumeric(strParam)) {
            try {
                return new BigDecimal(strParam).stripTrailingZeros().toPlainString();
            } catch (Exception e) {
                return strParam;
            }
        }
        return strParam;
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}