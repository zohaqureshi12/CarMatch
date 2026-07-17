package com.carmatch.service;

import com.carmatch.exception.ImageUploadException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class ImageStorageService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

    @PostConstruct
    private void init() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadException("File is empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new ImageUploadException(
                    "Only JPEG, PNG, or WEBP images are allowed");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ImageUploadException("File must be under 5MB");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "carmatch/cars",
                            "resource_type", "image"
                    )
            );
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return;
        }
        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception ignored) {

        }
    }
    private String extractPublicId(String imageUrl) {
        String afterUpload = imageUrl.substring(imageUrl.indexOf("/upload/") + 8);
        // strip the version segment (e.g. "v123456/")
        String withoutVersion = afterUpload.replaceFirst("^v\\d+/", "");
        // strip the file extension
        return withoutVersion.substring(0, withoutVersion.lastIndexOf('.'));
    }
}