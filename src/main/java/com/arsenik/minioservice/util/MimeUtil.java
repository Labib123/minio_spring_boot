package com.arsenik.minioservice.util;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public final class MimeUtil {

    private MimeUtil(){}

    public static MediaType getMimeType(InputStream inputStream, String filenameWithExtension) throws IOException {
        TikaConfig config = TikaConfig.getDefaultConfig();
        Detector detector = config.getDetector();
        TikaInputStream stream = TikaInputStream.get(inputStream);

        Metadata metadata = new Metadata();
        metadata.add(Metadata.RESOURCE_NAME_KEY, filenameWithExtension);
        return detector.detect(stream, metadata);
    }



    public static String getFileType(MultipartFile multipartFile) {
        InputStream inputStream = null;
        String type = null;
        try {
            inputStream = multipartFile.getInputStream();

            Tika tika = new Tika();

            type = tika.getDetector().detect(
                TikaInputStream.get(inputStream), new Metadata()).getType();
//            type =  tika.detect(inputStream);
       //     System.out.println(type);
            return type;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
