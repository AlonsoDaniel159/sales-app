package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.exception.CloudinaryException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cloudinary Service Tests")
class CloudinaryServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile multipartFile;

    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() {
        cloudinaryService = new CloudinaryServiceImpl("cloudinary://key:secret@cloud");
        // Inyectamos el mock de Cloudinary usando reflexión
        ReflectionTestUtils.setField(cloudinaryService, "cloudinary", cloudinary);
    }

    @Nested
    @DisplayName("Subir Imagen")
    class UploadTests {

        @Test
        @DisplayName("Debería subir imagen exitosamente")
        void shouldUploadImage_Successfully() throws IOException {
            Map<String, Object> expectedResult = Map.of(
                    "secure_url", "https://cloudinary.com/image.jpg",
                    "public_id", "img_123"
            );

            when(cloudinary.uploader()).thenReturn(uploader);
            when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
            when(multipartFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
            when(uploader.upload(any(), anyMap())).thenReturn(expectedResult);

            Map result = cloudinaryService.upload(multipartFile);

            assertThat(result).isNotNull();
            assertThat(result.get("secure_url")).isEqualTo("https://cloudinary.com/image.jpg");
            assertThat(result.get("public_id")).isEqualTo("img_123");
            verify(uploader).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Debería lanzar CloudinaryException cuando falla la subida")
        void shouldThrowCloudinaryException_WhenUploadFails() throws IOException {
            when(cloudinary.uploader()).thenReturn(uploader);
            when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
            when(multipartFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
            when(uploader.upload(any(), anyMap())).thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> cloudinaryService.upload(multipartFile))
                    .isInstanceOf(CloudinaryException.class)
                    .hasMessageContaining("Error al subir imagen a Cloudinary")
                    .hasCauseInstanceOf(IOException.class);

            verify(uploader).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Debería manejar archivo sin nombre")
        void shouldHandleFile_WithoutName() throws IOException {
            Map<String, Object> expectedResult = Map.of("secure_url", "url", "public_id", "id");

            when(cloudinary.uploader()).thenReturn(uploader);
            when(multipartFile.getOriginalFilename()).thenReturn(null);
            when(multipartFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
            when(uploader.upload(any(), anyMap())).thenReturn(expectedResult);

            Map result = cloudinaryService.upload(multipartFile);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Debería lanzar exception cuando falla la conversión del archivo")
        void shouldThrowException_WhenFileConversionFails() throws IOException {
            when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
            when(multipartFile.getBytes()).thenThrow(new IOException("Cannot read file"));

            assertThatThrownBy(() -> cloudinaryService.upload(multipartFile))
                    .isInstanceOf(CloudinaryException.class)
                    .hasCauseInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("Eliminar Imagen")
    class DeleteTests {

        @Test
        @DisplayName("Debería eliminar imagen exitosamente")
        void shouldDeleteImage_Successfully() throws IOException {
            Map<String, Object> expectedResult = Map.of("result", "ok");

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(eq("img_123"), anyMap())).thenReturn(expectedResult);

            Map result = cloudinaryService.delete("img_123");

            assertThat(result).isNotNull();
            assertThat(result.get("result")).isEqualTo("ok");
            verify(uploader).destroy("img_123", Map.of());
        }

        @Test
        @DisplayName("Debería lanzar CloudinaryException cuando falla la eliminación")
        void shouldThrowCloudinaryException_WhenDeleteFails() throws IOException {
            when(uploader.destroy(eq("img_123"), anyMap())).thenThrow(new IOException("Delete failed"));
            when(cloudinary.uploader()).thenReturn(uploader);

            assertThatThrownBy(() -> cloudinaryService.delete("img_123"))
                    .isInstanceOf(CloudinaryException.class)
                    .hasMessageContaining("Error al eliminar imagen de Cloudinary")
                    .hasCauseInstanceOf(IOException.class);

            verify(uploader).destroy("img_123", Map.of());
        }

        @Test
        @DisplayName("Debería procesar public_id vacío")
        void shouldProcessEmpty_PublicId() throws IOException {
            Map<String, Object> expectedResult = Map.of("result", "ok");

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(eq(""), anyMap())).thenReturn(expectedResult);

            Map result = cloudinaryService.delete("");

            assertThat(result).isNotNull();
            verify(uploader).destroy("", Map.of());
        }

        @Test
        @DisplayName("Debería procesar public_id null")
        void shouldProcessNull_PublicId() throws IOException {
            Map<String, Object> expectedResult = Map.of("result", "ok");

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(isNull(), anyMap())).thenReturn(expectedResult);

            Map result = cloudinaryService.delete(null);

            assertThat(result).isNotNull();
            verify(uploader).destroy(null, Map.of());
        }
    }

    @Nested
    @DisplayName("Validaciones de Configuración")
    class ConfigurationTests {

        @Test
        @DisplayName("Debería crear servicio con URL válida")
        void shouldCreateService_WithValidUrl() {
            CloudinaryServiceImpl service = new CloudinaryServiceImpl("cloudinary://key:secret@cloud");

            assertThat(service).isNotNull();
        }

        @Test
        @DisplayName("Debería manejar URL de Cloudinary correctamente")
        void shouldHandleCloudinaryUrl_Correctly() {
            String cloudinaryUrl = "cloudinary://123456789:abcdefg@mycloud";

            CloudinaryServiceImpl service = new CloudinaryServiceImpl(cloudinaryUrl);

            assertThat(service).isNotNull();
        }
    }
}
