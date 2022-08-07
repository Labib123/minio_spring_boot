package com.arsenik.minioservice.service;

import com.arsenik.minioservice.config.minio.MinioProperties;
import com.arsenik.minioservice.exception.CustomErrorException;
import com.google.gson.JsonObject;
import io.micrometer.core.instrument.util.StringUtils;
import io.minio.messages.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MinioServiceImpl implements MinioService {
    private final MinioUtil minioUtil;
    private final MinioProperties minioProperties;
    private final Logger log = LoggerFactory.getLogger(MinioServiceImpl.class);

    public MinioServiceImpl(MinioUtil minioUtil,
                            MinioProperties minioProperties) {
        this.minioUtil = minioUtil;
        this.minioProperties = minioProperties;
    }

    @Override
    public boolean bucketExists(String bucketName) {
        log.debug("Request to check existence of bucket: {}", bucketName);
        return minioUtil.bucketExists(bucketName);
    }


    @Override
    public void makeBucket(String bucketName) {
        log.debug("Request to make bucket: {}", bucketName);
        minioUtil.makeBucket(bucketName);
    }

    @Override
    public List<String> listBucketName() {
        log.debug("Request to list all buckets name");
        return minioUtil.listBucketNames();
    }

    @Override
    public List<Bucket> listBuckets() {
        log.debug("Request to list all buckets");
        return minioUtil.listBuckets();
    }

    @Override
    public boolean removeBucket(String bucketName) {
        log.debug("Request to delete bucket: {}", bucketName);
        return minioUtil.removeBucket(bucketName);
    }


    @Override
    public List<String> listObjectNames(String bucketName) {
        log.debug("Request to list object names of bucket: {}", bucketName);
        return minioUtil.listObjectNames(bucketName);
    }


    @Override
    public String putObject(MultipartFile file, String bucketName, String fileType) {
        log.debug("Request to put file: {} of type: {} to bucket: {}",
            file.getOriginalFilename(),
            fileType, bucketName);
        try {
            bucketName = StringUtils.isNotBlank(bucketName) ? bucketName : minioProperties.getBucketName();
            if (!this.bucketExists(bucketName)) {
                this.makeBucket(bucketName);
            }
            String fileName = file.getOriginalFilename();

            String objectName = UUID.randomUUID().toString().replaceAll("-", "")
                + Objects.requireNonNull(fileName).substring(fileName.lastIndexOf("."));
            minioUtil.putObject(bucketName, file, objectName, fileType);
            return minioProperties.getEndpoint() + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("bucketName", bucketName);
            jsonObject.addProperty("fileName", file.getOriginalFilename());
            throw new CustomErrorException(HttpStatus.BAD_REQUEST, e.getMessage(), jsonObject);
        }
    }


    @Override
    public String putObject(String bucketName, InputStream inputStream, String contentType) {
        try {
            bucketName = StringUtils.isNotBlank(bucketName) ? bucketName : minioProperties.getBucketName();
            if (!this.bucketExists(bucketName)) {
                this.makeBucket(bucketName);
            }

            String objectName = UUID.randomUUID().toString().replaceAll("-", "");
            minioUtil.putObject(bucketName, objectName, inputStream, contentType);
            return minioProperties.getEndpoint() + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("bucketName", bucketName);
            throw new CustomErrorException(HttpStatus.BAD_REQUEST, e.getMessage(), jsonObject);
        }
    }


    @Override
    public InputStream downloadObject(String bucketName, String objectName) throws IOException {
        log.debug("Request to download object: {} from bucket: {}", objectName, bucketName);
        return minioUtil.getObject(bucketName, objectName);
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        log.debug("Request to remove object: {} from bucket: {}", objectName, bucketName);
        minioUtil.removeObject(bucketName, objectName);
    }

    @Override
    public void removeListObject(String bucketName, List<String> objectNameList) {
        log.debug("Request to remove objects: {}, from bucket: {}", objectNameList, bucketName);
        minioUtil.removeObject(bucketName, objectNameList);

    }

    @Override
    public String getObjectUrl(String bucketName, String objectName) {
        log.debug("Request to get URL of object: {} from bucket: {}", objectName, bucketName);
        return minioUtil.getObjectUrl(bucketName, objectName);
    }
}

