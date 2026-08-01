package com.resourceservice.storage;

import com.resourceservice.config.S3Properties;
import com.resourceservice.resource.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Retryable(
            retryFor = SdkException.class,
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${retry.initial-delay:1000}",
                    multiplierExpression = "${retry.multiplier:2}"
            )
    )
    public String upload(byte[] data, String bucket, String path) {
        String key = buildKey(path, UUID.randomUUID() + ".mp3");
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(Constants.RESOURCE_SUPPORTED_MEDIA_TYPE)
                        .build(),
                RequestBody.fromBytes(data)
        );

        return key;
    }

    @Retryable(
            retryFor = SdkException.class,
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${retry.initial-delay:1000}",
                    multiplierExpression = "${retry.multiplier:2}"
            )
    )
    public byte[] download(String bucket, String key) {
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );

        return response.asByteArray();
    }

    @Retryable(
            retryFor = SdkException.class,
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${retry.initial-delay:1000}",
                    multiplierExpression = "${retry.multiplier:2}"
            )
    )
    public void delete(String bucket, String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }

    @Retryable(
            retryFor = SdkException.class,
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${retry.initial-delay:1000}",
                    multiplierExpression = "${retry.multiplier:2}"
            )
    )
    public String move(String sourceBucket, String sourceKey, String destBucket, String destPath) {
        String filename = sourceKey.substring(sourceKey.lastIndexOf('/') + 1);
        String destKey = buildKey(destPath, filename);

        s3Client.copyObject(
                CopyObjectRequest.builder()
                        .sourceBucket(sourceBucket)
                        .sourceKey(sourceKey)
                        .destinationBucket(destBucket)
                        .destinationKey(destKey)
                        .build()
        );

        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(sourceBucket)
                            .key(sourceKey)
                            .build()
            );
        } catch (NoSuchKeyException ignored) {
        }

        return destKey;
    }

    private String buildKey(String path, String filename) {
        String prefix = path.replaceAll("^/+", "");
        return prefix.isEmpty() ? filename : prefix + "/" + filename;
    }
}
