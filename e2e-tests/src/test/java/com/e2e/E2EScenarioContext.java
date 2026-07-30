package com.e2e;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
public class E2EScenarioContext {

    private Long resourceId;
    private Long songId;
    private Response lastResponse;
    private byte[] uploadedFileBytes;
    private final Map<String, Long> namedResourceIds = new HashMap<>();

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }

    public Response getLastResponse() { return lastResponse; }
    public void setLastResponse(Response lastResponse) { this.lastResponse = lastResponse; }

    public byte[] getUploadedFileBytes() { return uploadedFileBytes; }
    public void setUploadedFileBytes(byte[] uploadedFileBytes) { this.uploadedFileBytes = uploadedFileBytes; }

    public void putNamedResourceId(String name, Long id) { namedResourceIds.put(name, id); }
    public Long getNamedResourceId(String name) { return namedResourceIds.get(name); }
    public Map<String, Long> getNamedResourceIds() { return namedResourceIds; }
}
