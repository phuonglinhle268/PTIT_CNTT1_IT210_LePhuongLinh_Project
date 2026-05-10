package org.example.java_web_project.service;

import lombok.RequiredArgsConstructor;
import org.example.java_web_project.config.VNPayConfig;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService {

    private final VNPayConfig config;

    /**
     * Tạo URL redirect đến cổng VNPay.
     *
     * @param bookingId  ID đơn đặt vé (dùng làm txnRef)
     * @param amount     Tổng tiền (VND)
     * @param ipAddress  IP của khách hàng
     * @return URL đầy đủ để redirect
     */
    public String createPaymentUrl(Integer bookingId, BigDecimal amount, String ipAddress) {
        String txnRef    = bookingId + "_" + System.currentTimeMillis();
        String orderInfo = "Thanh toan ve xem phim - Don " + bookingId;
        long   amountVnd = amount.longValue() * 100; // VNPay nhân 100

        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        Map<String, String> params = new TreeMap<>(); // TreeMap tự sort key
        params.put("vnp_Version",    config.getApiVersion());
        params.put("vnp_Command",    "pay");
        params.put("vnp_TmnCode",    config.getTmnCode());
        params.put("vnp_Amount",     String.valueOf(amountVnd));
        params.put("vnp_CurrCode",   "VND");
        params.put("vnp_TxnRef",     txnRef);
        params.put("vnp_OrderInfo",  orderInfo);
        params.put("vnp_OrderType",  "other");
        params.put("vnp_Locale",     "vn");
        params.put("vnp_ReturnUrl",  config.getReturnUrl());
        params.put("vnp_IpAddr",     ipAddress);
        params.put("vnp_CreateDate", createDate);

        // Build query string (đã sort theo key)
        StringBuilder hashData = new StringBuilder();
        StringBuilder query    = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String encodedKey   = URLEncoder.encode(entry.getKey(),   StandardCharsets.US_ASCII);
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII);

            hashData.append(encodedKey).append('=').append(encodedValue).append('&');
            query.append(encodedKey).append('=').append(encodedValue).append('&');
        }
        // Bỏ dấu & cuối
        hashData.deleteCharAt(hashData.length() - 1);
        query.deleteCharAt(query.length() - 1);

        String secureHash = hmacSha512(config.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return config.getPaymentUrl() + "?" + query;
    }

    /**
     * Xác minh callback từ VNPay.
     * So sánh vnp_SecureHash do VNPay gửi về với hash tự tính lại.
     *
     * @param params Toàn bộ query params từ VNPay callback
     * @return true nếu hợp lệ
     */
    public boolean verifyCallback(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null) return false;

        // Loại bỏ vnp_SecureHash khỏi map trước khi tính lại
        Map<String, String> verifyParams = new TreeMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : verifyParams.entrySet()) {
            String encodedKey   = URLEncoder.encode(entry.getKey(),   StandardCharsets.US_ASCII);
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII);
            hashData.append(encodedKey).append('=').append(encodedValue).append('&');
        }
        hashData.deleteCharAt(hashData.length() - 1);

        String calculatedHash = hmacSha512(config.getHashSecret(), hashData.toString());
        return calculatedHash.equalsIgnoreCase(vnpSecureHash);
    }

    /**
     * Lấy bookingId từ vnp_TxnRef (format: "bookingId_timestamp")
     */
    public Integer extractBookingId(String txnRef) {
        return Integer.parseInt(txnRef.split("_")[0]);
    }

    // ── Crypto ───────────────────────────────────────────────────────────────

    private String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo HMAC-SHA512", e);
        }
    }
}