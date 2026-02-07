package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.exception.CloudinaryException;
import com.alonso.salesapp.service.ICloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements ICloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(@Value("${cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    @Override
    public Map upload(MultipartFile multipartFile) {
        File file = null;
        try {
            file = convert(multipartFile);
            return cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new CloudinaryException("Error al subir imagen a Cloudinary", e);
        } finally {
            if (file != null) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException ignored) {}
            }
        }
    }

    @Override
    public Map delete(String publicId) {
        try {
            return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new CloudinaryException("Error al eliminar imagen de Cloudinary", e);
        }
    }

    private File convert(MultipartFile multipartFile) throws IOException {
        File file = Files.createTempFile("upload_", "_" + multipartFile.getOriginalFilename()).toFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }
}
