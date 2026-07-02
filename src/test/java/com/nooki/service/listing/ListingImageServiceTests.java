package com.nooki.service.listing;

import com.nooki.dto.ListingImage.ListingImageResponse;
import com.nooki.dto.exception.customException.ImageException.ImageStorageException;
import com.nooki.dto.exception.customException.forbiden.ForbiddenUserException;
import com.nooki.dto.listing.ListingResponse;
import com.nooki.entity.Listing;
import com.nooki.entity.ListingImage;
import com.nooki.entity.User;
import com.nooki.mapper.ListingMapper;
import com.nooki.repository.ListingImageRepository;
import com.nooki.repository.ListingRepository;
import com.nooki.service.ListingServices.ListingDomainService;
import com.nooki.service.ListingServices.ListingImageService;
import com.nooki.service.MinioService;
import com.nooki.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingImageServiceTests {

    @InjectMocks
    private ListingImageService listingImageService;

    @Mock
    private MinioService minioService;

    @Mock
    private ListingDomainService domainService;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingMapper listingMapper;

    @Mock
    private ListingImageRepository listingImageRepository;

    @Mock
    private UserService userService;

    @Nested
    @DisplayName("Add image to listing")
    class AddImageToListing {
        private final UUID listingId = UUID.randomUUID();

        @Test
        @DisplayName("valid request should add image to listing")
        void validRequest() {
            UUID hostId = UUID.randomUUID();

            MockMultipartFile file1 = new MockMultipartFile(
                    "file",
                    "testImage1.png",
                    "image/png",
                    "test image".getBytes()
            );

            MockMultipartFile file2 = new MockMultipartFile(
                    "file",
                    "testImage2.png",
                    "image/png",
                    "test image".getBytes()
            );

            List<MultipartFile> files = List.of(file1, file2);

            User host = new User();
            host.setUserId(hostId);

            Listing listing = new Listing();
            listing.setUser(host);

            ListingImageResponse image1 = new ListingImageResponse(); image1.setFileName("/pathToFile1");
            ListingImageResponse image2 = new ListingImageResponse(); image2.setFileName("/pathToFile2");
            List<ListingImageResponse> expectedImage = List.of(image1, image2);

            ListingResponse expectedResponse = new ListingResponse();
            expectedResponse.setListingId(listingId);
            expectedResponse.setListingImages(expectedImage);

            when(domainService.getListingDetailedOrThrow(listingId)).thenReturn(listing);
            when(userService.getCurrentUserId()).thenReturn(hostId);

            when(minioService.uploadFile(file1)).thenReturn("/pathToFile1");
            when(minioService.uploadFile(file2)).thenReturn("/pathToFile2");
            when(listingMapper.toListingResponse(listing)).thenReturn(expectedResponse);

            ListingResponse result = listingImageService.addImageToListing(listingId, files);

            assertThat(expectedResponse).isEqualTo(result);
            assertThat(listing.getListingImages().size()).isEqualTo(2);

            assertThat(listing.getListingImages().get(0).getFileName()).isEqualTo(image1.getFileName());
            assertThat(listing.getListingImages().get(1).getFileName()).isEqualTo(image2.getFileName());
            verify(listingRepository, times(1)).save(any(Listing.class));
        }

        @DisplayName("not owner trying to add image to listing should throw ForbiddenUserException")
        @Test
        void notOwnerTryingToAddImageToListing_shouldThrowForbiddenUserException() {
            MockMultipartFile file1 = new MockMultipartFile(
                    "file",
                    "testImage1.png",
                    "image/png",
                    "test image".getBytes()
            );

            MockMultipartFile file2 = new MockMultipartFile(
                    "file",
                    "testImage2.png",
                    "image/png",
                    "test image".getBytes()
            );

            List<MultipartFile> files = List.of(file1, file2);

            User host = new User();
            host.setUserId(UUID.randomUUID());

            Listing listing = new Listing();
            listing.setUser(host);

            when(domainService.getListingDetailedOrThrow(listingId)).thenReturn(listing);
            when(userService.getCurrentUserId()).thenReturn(UUID.randomUUID());

            assertThatThrownBy(() -> listingImageService.addImageToListing(listingId, files))
                    .isInstanceOf(ForbiddenUserException.class)
                    .hasMessage("You are not owner of the listing");

            verify(listingRepository, never()).save(any(Listing.class));
        }

        @Test
        @DisplayName("Minio throw exception")
        void minioThrowsException_shouldThrowImageStorageException() {
            MockMultipartFile file1 = new MockMultipartFile(
                    "file",
                    "testImage1.png",
                    "image/png",
                    "test image".getBytes()
            );

            MockMultipartFile file2 = new MockMultipartFile(
                    "file",
                    "testImage2.png",
                    "image/png",
                    "test image".getBytes()
            );

            List<MultipartFile> files = List.of(file1, file2);

            UUID hostId = UUID.randomUUID();
            User host = new User();
            host.setUserId(hostId);

            Listing listing = new Listing();
            listing.setUser(host);

            when(domainService.getListingDetailedOrThrow(listingId)).thenReturn(listing);
            when(userService.getCurrentUserId()).thenReturn(hostId);
            when(minioService.uploadFile(any(MultipartFile.class))).thenThrow(IllegalArgumentException.class);

            assertThatThrownBy(() -> listingImageService.addImageToListing(listingId, files))
                    .isInstanceOf(ImageStorageException.class)
                    .hasMessage("Failed to save listing, cleaning up files");
            verify(listingRepository, never()).save(any(Listing.class));
        }
    }

    @Nested
    @DisplayName("Remove image to listing")
    class RemoveImageToListing {
        private final UUID listingId = UUID.randomUUID();
        private final Long imageId = 1L;

        @Test
        @DisplayName("valid request should delete listingImage")
        void validRequest() {
            UUID hostId = UUID.randomUUID();
            User host = new User();
            host.setUserId(hostId);

            ListingImage listingImage = new ListingImage();
            listingImage.setFileName("path1");

            Listing listing = new Listing();
            listing.setUser(host);
            listing.setListingImages(new ArrayList<>(List.of(listingImage)));

            when(userService.getCurrentUserId()).thenReturn(hostId);
            when(domainService.getListingDetailedOrThrow(listingId)).thenReturn(listing);
            when(listingImageRepository.findByListingImageIdAndListing_ListingId(imageId, listingId )).thenReturn(Optional.of(listingImage));

            listingImageService.removeImage(listingId, imageId);

            verify(minioService, times(1)).deleteFile("path1");
        }

        @Test
        @DisplayName("user not owner of the listing should throw forbidden User Exception")
        void userNotOwner_shouldThrowForbiddenUserException() {
            UUID hostId = UUID.randomUUID();
            User host = new User();
            host.setUserId(hostId);

            Listing listing = new Listing();
            listing.setUser(host);

            when(userService.getCurrentUserId()).thenReturn(UUID.randomUUID());
            when(domainService.getListingDetailedOrThrow(listingId)).thenReturn(listing);

            assertThatThrownBy(() -> listingImageService.removeImage(listingId, imageId))
                    .isInstanceOf(ForbiddenUserException.class)
                    .hasMessage("You are not owner of the listing");
        }

        @Test
        @DisplayName("image not found should throw image storage exception")
        void listingImageNotFound_shouldThrowImageStorageException() {
            UUID hostId = UUID.randomUUID();
            User host = new User();
            host.setUserId(hostId);

            Listing listing = new Listing();
            listing.setUser(host);

            when(userService.getCurrentUserId()).thenReturn(hostId);
            when(domainService.getListingDetailedOrThrow(listingId)).thenReturn(listing);

            when(listingImageRepository.findByListingImageIdAndListing_ListingId(imageId, listingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingImageService.removeImage(listingId, imageId))
                    .isInstanceOf(ImageStorageException.class)
                    .hasMessage("Not exists image of listing");
        }
    }
}
