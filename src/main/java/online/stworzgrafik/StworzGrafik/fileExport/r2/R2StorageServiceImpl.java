package online.stworzgrafik.StworzGrafik.fileExport.r2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class R2StorageServiceImpl implements R2StorageService{
    private final S3Client r2Client;
    private final S3Presigner r2Presigner;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("{cloudflare.r2.presigned-url-expiry-minutes:15}")
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
}
