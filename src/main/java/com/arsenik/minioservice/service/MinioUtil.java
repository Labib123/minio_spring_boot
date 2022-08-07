package com.arsenik.minioservice.service;

import com.arsenik.minioservice.exception.CustomErrorException;
import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MinioUtil {

    private final MinioClient minioClient;

    private final com.arsenik.minioservice.config.minio.MinioProperties minioProperties;

    public MinioUtil(MinioClient minioClient, com.arsenik.minioservice.config.minio.MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    /**
     * Check if the bucket exists  * * @param bucketName  Bucket name  * @return
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), bucketName);
        }
    }

    /**
     * Create buckets  * * @param bucketName  Bucket name
     */
    public void makeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        try {
            if (!flag) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());

            } else {
                String errMsg = "bucket is already exist!";
                throw new CustomErrorException(HttpStatus.BAD_REQUEST, errMsg, bucketName);
            }
        } catch (ServerException
            | InsufficientDataException
            | ErrorResponseException
            | IOException
            | NoSuchAlgorithmException
            | InvalidKeyException
            | InvalidResponseException
            | XmlParserException
            | InternalException e) {
            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), bucketName);
        }
    }

    /**
     * List all bucket names  * * @return
     */
    public List<String> listBucketNames() {
        List<Bucket> bucketList = listBuckets();
        List<String> bucketListName = new ArrayList<>();
        for (Bucket bucket : bucketList) {
            bucketListName.add(bucket.name());
        }
        return bucketListName;
    }

    /**
     * List all buckets  * * @return
     */
    public List<Bucket> listBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    /**
     * Delete buckets  * * @param bucketName  Bucket name  * @return
     */
    public boolean removeBucket(String bucketName) {
        bucketIsExist(bucketName);
        Iterable<Result<Item>> myObjects = listObjects(bucketName);
        boolean dataExist = Iterables.size(myObjects) > 0;
        if (dataExist) {
            String errMsg = "Bucket is not empty; only Empty Buckets can be deleted";
            throw new CustomErrorException(HttpStatus.BAD_REQUEST, errMsg, bucketName);
        }
        //  Delete buckets , Be careful , Only when the bucket is empty can it be deleted successfully .
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        } catch (ServerException
            | InsufficientDataException
            | ErrorResponseException
            | IOException
            | NoSuchAlgorithmException
            | InvalidKeyException
            | InvalidResponseException
            | XmlParserException
            | InternalException e) {

            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), bucketName);
        }
        return !bucketExists(bucketName);
    }

    /**
     * List all object names in the bucket  * * @param bucketName  Bucket name  * @return
     */
    public List<String> listObjectNames(String bucketName) {
        List<String> listObjectNames = new ArrayList<>();
        bucketIsExist(bucketName);
        try {
            Iterable<Result<Item>> myObjects = listObjects(bucketName);
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                listObjectNames.add(item.objectName());
            }
        } catch (Exception e) {
            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), bucketName);
        }
        return listObjectNames;
    }


    /**
     * List all objects in the bucket  * * @param bucketName  Bucket name  * @return
     */
    public Iterable<Result<Item>> listObjects(String bucketName) {
        bucketIsExist(bucketName);
        return minioClient.listObjects(
            ListObjectsArgs.builder().bucket(bucketName).build());

    }

    /**
     * Upload files  * * @param bucketName * @param multipartFile
     */
    public void putObject(String bucketName, MultipartFile multipartFile, String filename, String fileType) {
        try {
            InputStream inputStream = new ByteArrayInputStream(multipartFile.getBytes());
            minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(filename).stream(
                        inputStream, -1, minioProperties.getFileSize())
                    .contentType(fileType)
                    .build());
        } catch (Exception e) {
            String stringBuilder = "unable to upload to bucket: " +
                bucketName +
                "file: " +
                filename +
                ", file type: " +
                fileType;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("bucketName", bucketName);
            jsonObject.addProperty("fileName", filename);
            jsonObject.addProperty("fileType", fileType);
            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, stringBuilder, jsonObject);
        }
    }


    /**
     * File access path  * * @param bucketName  Bucket name  * @param objectName  The name of the object in the bucket  * @return
     */
    public String getObjectUrl(String bucketName, String objectName) {
        bucketIsExist(bucketName);
        try {
            String url = "";
            url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(2, TimeUnit.MINUTES)
                    .build());
            return url;
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("bucketName", bucketName);
            jsonObject.addProperty("objectName", objectName);
            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), jsonObject);
        }
    }

    private void bucketIsExist(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (!flag) {
            throw new CustomErrorException(HttpStatus.NOT_FOUND, "bucket: " + bucketName + " not found", bucketName);
        }
    }


    /**
     * Delete an object  * * @param bucketName  Bucket name  * @param objectName  The name of the object in the bucket
     */
    public void removeObject(String bucketName, String objectName) {
        bucketIsExist(bucketName);
        StatObjectResponse statObject = statObject(bucketName, objectName);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bucketName", bucketName);
        jsonObject.addProperty("objectName", objectName);
        if (statObject == null) {
            String errMsg = "Object: " + objectName + " does not exist in bucket: " + bucketName;
            throw new CustomErrorException(HttpStatus.BAD_REQUEST, errMsg, jsonObject);
        }
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (ServerException
            | InsufficientDataException
            | ErrorResponseException
            | IOException
            | NoSuchAlgorithmException
            | InvalidKeyException
            | InvalidResponseException
            | XmlParserException
            | InternalException e) {

            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), jsonObject);
        }
    }

    /**
     * Get a file object as a stream  * * @param bucketName  Bucket name  * @param objectName  The name of the object in the bucket  * @return
     */
    public InputStream getObject(String bucketName, String objectName) {
        StatObjectResponse statObject = statObject(bucketName, objectName);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bucketName", bucketName);
        jsonObject.addProperty("objectName", objectName);
        try {

            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (ServerException
            | InsufficientDataException
            | ErrorResponseException
            | IOException
            | NoSuchAlgorithmException
            | InvalidKeyException
            | InvalidResponseException
            | XmlParserException
            | InternalException e) {

            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), jsonObject);
        }
    }

    /**
     * Get the metadata of the object  * * @param bucketName  Bucket name  * @param objectName  The name of the object in the bucket  * @return
     */
    public StatObjectResponse statObject(String bucketName, String objectName) {
        bucketIsExist(bucketName);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bucketName", bucketName);
        jsonObject.addProperty("objectName", objectName);
        try {
            StatObjectResponse statObject = minioClient.statObject(
                StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            if (!(statObject != null && statObject.size() > 0)) {
                String errMsg = "object: " + objectName + "does not exist in bucket: " + bucketName;
                throw new CustomErrorException(HttpStatus.BAD_REQUEST, errMsg, jsonObject);
            }
            return statObject;
        } catch (ServerException
            | InsufficientDataException
            | ErrorResponseException
            | IOException
            | NoSuchAlgorithmException
            | InvalidKeyException
            | InvalidResponseException
            | XmlParserException
            | InternalException e) {
            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), jsonObject);
        }
    }

    /**
     * Delete multiple file objects of the specified bucket , Returns the list of objects with deletion errors , Delete all successfully , Return to empty list  * * @param bucketName  Bucket name  * @param objectNames  Contains multiple... To delete object Iterator object with name  * @return
     */
    public void removeObject(String bucketName, List<String> objectNames) {
        bucketIsExist(bucketName);

        List<DeleteObject> objects = new LinkedList<>();
        for (String objectName : objectNames) {
            objects.add(new DeleteObject(objectName));
        }
        Iterable<Result<DeleteError>> results =
            minioClient.removeObjects(
                RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
        StringBuilder errMsg = new StringBuilder();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bucketName", bucketName);
        jsonObject.addProperty("objectNames", Arrays.toString(objectNames.toArray()));
        try {
            int errSize = Iterables.size(results);
            if (errSize > 0) {
                for (Result<DeleteError> result : results) {
                    DeleteError error = result.get();
                    errMsg.append("Error in deleting object: ").append(error.objectName()).append("; ").append(error.message());
                }

                throw new CustomErrorException(HttpStatus.BAD_REQUEST, errMsg.toString(), jsonObject);
            }
        } catch (ServerException
            | InsufficientDataException
            | ErrorResponseException
            | IOException
            | NoSuchAlgorithmException
            | InvalidKeyException
            | InvalidResponseException
            | XmlParserException
            | InternalException e) {
            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), jsonObject);
        }
    }

    /**
     * Get a file object as a stream （ Breakpoint download ） * * @param bucketName  Bucket name  * @param objectName  The name of the object in the bucket  * @param offset  The position of the start byte  * @param length  Length to read  ( Optional , If there is no value, it means reading to the end of the file ) * @return
     */
    public InputStream getObject(String bucketName, String objectName, long offset, Long length) {

        statObject(bucketName, objectName);
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .offset(offset)
                    .length(length)
                    .build());
        } catch (ServerException
            | InsufficientDataException
            | ErrorResponseException
            | IOException
            | NoSuchAlgorithmException
            | InvalidKeyException
            | InvalidResponseException
            | XmlParserException
            | InternalException e) {
            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }


    /**
     * adopt InputStream Upload object  * * @param bucketName  Bucket name  * @param objectName  The name of the object in the bucket  * @param inputStream  Stream to upload  * @param contentType  Type of file to upload  MimeTypeUtils.IMAGE_JPEG_VALUE * @return
     */
    public boolean putObject(String bucketName, String objectName, InputStream inputStream, String contentType) {
        bucketIsExist(bucketName);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bucketName", bucketName);
        jsonObject.addProperty("objectName", objectName);
        jsonObject.addProperty("contentType", contentType);
        try {
            minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                        inputStream, -1, minioProperties.getFileSize())
                    .contentType(contentType)
                    .build());
            StatObjectResponse statObject = statObject(bucketName, objectName);
            return true;
        } catch (ServerException
            | InsufficientDataException
            | ErrorResponseException
            | IOException
            | NoSuchAlgorithmException
            | InvalidKeyException
            | InvalidResponseException
            | XmlParserException
            | InternalException e) {
            throw new CustomErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), jsonObject);
        }
    }
}

