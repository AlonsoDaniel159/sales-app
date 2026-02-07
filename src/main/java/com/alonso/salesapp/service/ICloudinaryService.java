package com.alonso.salesapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ICloudinaryService {
    Map upload(MultipartFile multipartFile);
    Map delete(String id);
}
