Feature: Resource Retrieval
  As an API client
  I want to retrieve previously uploaded MP3 resources
  So that I can download the original audio files

  Background:
    Given the system is fully operational
    And the API gateway is accessible at "http://localhost:8084"
    And a resource has been uploaded and processed successfully

  Scenario: Download an existing MP3 resource
    When I send a GET request to "/resources/{resourceId}"
    Then the response status code should be 200
    And the response content type should be "audio/mpeg"
    And the response body should be identical to the originally uploaded MP3 file

  Scenario: Attempt to download a non-existing resource
    When I send a GET request to "/resources/99999"
    Then the response status code should be 404

  Scenario: Retrieve song metadata by ID
    When I send a GET request to "/songs/{songId}"
    Then the response status code should be 200
    And the response should contain valid song metadata fields
