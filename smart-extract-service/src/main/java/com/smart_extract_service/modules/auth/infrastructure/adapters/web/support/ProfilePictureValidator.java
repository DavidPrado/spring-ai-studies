package com.smart_extract_service.modules.auth.infrastructure.adapters.web.support;

import com.smart_extract_service.core.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class ProfilePictureValidator {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/jpg", "image/png", "image/gif");


    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("upload.file.empty", "Arquivo vazio");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("upload.file.too_large", "Arquivo muito grande. Máximo: 5MB");
        }
        String contentType = file.getContentType();
        if(contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())){
            throw  new BusinessException("upload.file.invalid_type", "Tipo de arquivo não permitido");
        }
    }

}
