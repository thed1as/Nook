package com.library.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import com.library.minio.MinioProperties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MinioServiceTests {

    @InjectMocks
    private MinioService minioService;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @Test
    void uploadFile_validFile_ReturnsUrl() {
        byte[] content = "Hello World".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "tests.jpg",
                "text/plain",
                content
        );

        when(minioProperties.getUrl()).thenReturn("http://localhost:9000");
        when(minioProperties.getBucket()).thenReturn("images");

        String result = minioService.uploadFile(file);

        assertNotNull(result);

        assertTrue(result.startsWith("http://localhost:9000/images/"));
        assertTrue(result.endsWith("_tests.jpg"));

        try{
            ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
            verify(minioClient).putObject(captor.capture());

            PutObjectArgs args = captor.getValue();
            assertEquals("images", args.bucket());
            assertEquals("text/plain", args.contentType());
            assertTrue(args.object().endsWith("_tests.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void uploadFile_EmptyFile_ThrowsIllegalArgumentException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "text/plain",
                new byte[0]
        );

        assertThatThrownBy(() -> minioService.uploadFile(emptyFile)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File is empty");
    }

    @Test
    void uploadFile_minioThrows_exceptionWrapped() throws Exception {
        byte[] content = "Hello World".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "tests.jpg",
                "text/plain",
                content
        );

        when(minioProperties.getBucket()).thenReturn("images");

        doThrow(new RuntimeException("Minio error"))
                .when(minioClient).putObject(any());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> minioService.uploadFile(file));

        assertTrue(exception.getMessage().contains("Error"));
    }

    @Test
    void deleteFile_ValidUrl() throws Exception {
        String url = "http://localhost:9000/images/test-file.jpg";

        when(minioProperties.getUrl()).thenReturn("http://localhost:9000");
        when(minioProperties.getBucket()).thenReturn("images");

        minioService.deleteFile(url);
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(captor.capture());

        RemoveObjectArgs args = captor.getValue();
        assertEquals("images", args.bucket());
        assertEquals("test-file.jpg", args.object());
    }

    @Test
    void deleteFile_InvalidUrl_ThrowsRuntimeException() throws Exception {
        String url = "http://invalidUrl.com/file.jpg";

        when(minioProperties.getUrl()).thenReturn("http://localhost:9000");
        when(minioProperties.getBucket()).thenReturn("images");

        assertThatThrownBy(() -> minioService.deleteFile(url)).isInstanceOf(RuntimeException.class);

        verify(minioClient, never()).removeObject(any());
    }

    @Test
    void deleteFile_minioThrows_exceptionWrapped() throws Exception {
        String url = "http://localhost:9000/images/test.jpg";

        when(minioProperties.getUrl()).thenReturn("http://localhost:9000");
        when(minioProperties.getBucket()).thenReturn("images");

        doThrow(new RuntimeException("Minio error"))
                .when(minioClient).removeObject(any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> minioService.deleteFile(url));

        assertTrue(exception.getMessage().contains("Error"));
    }
}
