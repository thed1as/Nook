package com.library.service;

import com.library.minio.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Transactional
    public String uploadFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return properties.getUrl() + "/" + properties.getBucket() + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла", e);
        }
    }

    @Transactional
    public void deleteFile(String url) {
        try{
            String baseUrl = properties.getUrl() + "/" + properties.getBucket() + "/";
            if(!url.startsWith(baseUrl)) {
                throw new IllegalStateException("Incorrect URL");
            }
            String objectName = url.substring(baseUrl.length());

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName).build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error: ", e);
        }
    }
}
