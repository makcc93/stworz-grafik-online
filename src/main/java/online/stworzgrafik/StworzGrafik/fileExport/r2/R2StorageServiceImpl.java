package online.stworzgrafik.StworzGrafik.fileExport.r2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class R2StorageServiceImpl implements R2StorageService{
    private final S3Client r2Client;
    private final S3Presigner r2Presigner;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.presigned-url-expiry-minutes:15}")
    private int expiryMinutes;

    @Override
    public String uploadAndPresign(byte[] data, String key, String contentType) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength((long) data.length)
                .build();

        r2Client.putObject(putRequest, RequestBody.fromBytes(data));
        log.info("[R2 storage] Wgrano plik: bucket={}, key={}, size={}", bucket,key,data.length);

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .getObjectRequest(r -> r.bucket(bucket).key(key))
                .build();

        String url = r2Presigner.presignGetObject(presignRequest).url().toString();
        log.info("[R2 storage] Presigned URL wygasa za {} min: {}", expiryMinutes,key);

        return url;
    }

    @Override
    public String getPresignedUrl(String key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .getObjectRequest(r -> r.bucket(bucket).key(key))
                .build();

        return r2Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void deleteFolderPrefix(String prefix) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listResponse = r2Client.listObjectsV2(listRequest);

            if (listResponse.hasContents()) {
                List<ObjectIdentifier> objectsToDelete = listResponse.contents().stream()
                        .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                        .collect(Collectors.toList());

                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder().objects(objectsToDelete).build())
                        .build();

                r2Client.deleteObjects(deleteRequest);
                log.info("[R2 storage] Usunięto wszystkie pliki z prefiksu: {}", prefix);
            }
        } catch (Exception e) {
            log.error("[R2 storage] Błąd podczas usuwania plików dla prefiksu {}: {}", prefix, e.getMessage());
        }
    }
}
