package com.arsenik.minioservice.controller;

import com.arsenik.minioservice.config.minio.MinioProperties;
import com.arsenik.minioservice.service.MinioService;
import com.arsenik.minioservice.util.MimeUtil;
import org.apache.tika.io.IOUtils;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/minio")
@RestController
public class MinioController {
    private final Logger log = LoggerFactory.getLogger(MinioController.class);
    private final MinioService minioService;

    private final MinioProperties minioProperties;

    public MinioController(MinioService minioService, MinioProperties minioProperties) {
        this.minioService = minioService;
        this.minioProperties = minioProperties;
    }

    /* Bucket CRUD*/
    @PostMapping("/buckets/{bucketName}")
    public ResponseEntity<Void> addBucket(@PathVariable String bucketName) {
        log.debug("Rest request to add bucket: {}", bucketName);
        minioService.makeBucket(bucketName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/buckets/{bucketName}")
    public List<String> show(@PathVariable String bucketName) {
        log.debug("Rest request to get bucket: {}", bucketName);
        return minioService.listObjectNames(bucketName);
    }

    @DeleteMapping("/buckets/{bucketName}")
    public boolean delBucketName(@PathVariable String bucketName) {
        log.debug("Rest request to delete bucket: {}", bucketName);
        return minioService.removeBucket(bucketName);
    }

    @GetMapping("/buckets")
    public List<String> showBucketName() {
        log.debug("Rest request to get all buckets");
        return minioService.listBucketName();
    }

    @GetMapping("/showListObjectNameAndDownloadUrl/{bucketName}")
    public Map<String, String> showListObjectNameAndDownloadUrl(@PathVariable String bucketName) {
        log.debug("Rest request to show a list of object names and download links for bucket: {}", bucketName);
        Map<String, String> map = new HashMap<>();
        List<String> listObjectNames = minioService.listObjectNames(bucketName);
        String url = minioProperties.getDownloadUrl() + bucketName + "/";
        listObjectNames.forEach(System.out::println);
        for (String listObjectName : listObjectNames) {
            map.put(listObjectName, url + listObjectName);
        }
        return map;
    }

    /* Object CRUD*/
    @PostMapping("/buckets/{bucketName}/objects")
    public String uploadFile(MultipartFile file, @PathVariable String bucketName) {
        log.debug("Rest request to upload file: {} to bucket: {}", file.getOriginalFilename(), bucketName);
        String fileType = MimeUtil.getFileType(file);
        return minioService.putObject(file, bucketName, fileType);
    }

    @RequestMapping("/buckets/{bucketName}/objects/{objectName}")
    public byte[] download(HttpServletResponse response, @PathVariable("bucketName") String bucketName, @PathVariable("objectName") String objectName) {
        log.debug("Rest request to download Object: {} from bucket: {}", objectName, bucketName);
        byte[] bytes = null;
        InputStream in;
        try {
            in = minioService.downloadObject(bucketName, objectName);

            org.apache.tika.mime.MediaType mediaType;
            try {
                mediaType = MimeUtil.getMimeType(in, objectName);
            } catch (IOException e) {
                mediaType = MediaType.OCTET_STREAM;
            }

            response.setContentType(mediaType.getType());
            response.setHeader("Content-Disposition", "attachment; filename=" + objectName);
            bytes = IOUtils.toByteArray(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    @DeleteMapping("/buckets/{bucketName}/objects/{objectName}")
    public ResponseEntity<Void> delObject(@PathVariable("bucketName") String bucketName, @PathVariable("objectName") String objectName) {
        log.debug("Rest request to delete Object: {} from bucket: {}", objectName, bucketName);
        minioService.removeObject(bucketName, objectName);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @DeleteMapping("/buckets/{bucketName}/objects")
    public ResponseEntity<Void> delListObject(@PathVariable("bucketName") String bucketName, @RequestBody List<String> objectNameList) {
        log.debug("Rest request to delete objects: {} from bucket: {}", objectNameList, bucketName);
        minioService.removeListObject(bucketName, objectNameList);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
