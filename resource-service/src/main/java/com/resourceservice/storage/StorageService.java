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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {
    private static final String KEY_TEMPLATE = "resources/%s.mp3";

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
    public String upload(byte[] data) {
        String key = KEY_TEMPLATE.formatted(UUID.randomUUID());
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(s3Properties.getBucket())
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
    public byte[] download(String key) {
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(s3Properties.getBucket())
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
    public void delete(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(s3Properties.getBucket())
                        .key(key)
                        .build()
        );
    }

}
