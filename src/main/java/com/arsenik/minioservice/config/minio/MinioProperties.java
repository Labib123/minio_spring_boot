package com.arsenik.minioservice.config.minio;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /** *  It's a URL, domain name ,IPv4 perhaps IPv6 Address ") */
    private String endpoint;

    /** * //"TCP/IP Port number " */
    private Integer port;

    /** * //"accessKey Similar to user ID, Used to uniquely identify your account " */
    private String accessKey;

    /** * //"secretKey It's the password for your account " */
    private String secretKey;

    /** * //" If it is true, It uses https instead of http, The default value is true" */
    private boolean secure;

    /** * //" Default bucket " */

    private String bucketName;

    /** *  The maximum size of the picture  */
    private long imageSize;

    /** *  Maximum size of other files  */
    private long fileSize;

    private String downloadUrl;

    /** *  From the official website   Construction method , I just climbed the official website  （ The dog's head lives ） *  This is   The class that the client operates on  */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .credentials(accessKey, secretKey)
            .endpoint(endpoint,port,secure)
            .build();
    }
}
