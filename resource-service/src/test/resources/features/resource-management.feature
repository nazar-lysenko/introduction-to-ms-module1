Feature: Resource Management
  As the resource-service
  I need to manage MP3 file storage and lifecycle
  So that resources are persisted and downstream services are notified

  Background:
    Given the resource-service is running
    And the S3 bucket "resources-bucket" exists
    And the song-service is available

  Scenario: Successfully upload an MP3 file
    Given I have a valid MP3 file "song.mp3"
    When I upload the MP3 file to the resource-service
    Then the response status code should be 200
    And the response should contain the resource ID
    And the MP3 file should be stored in S3
    And a resource record should be saved in the database
    And a resource event should be published to Kafka with the resource ID

  Scenario: Reject upload of non-MP3 file
    Given I have a file "document.pdf" that is not an MP3
    When I upload the file to the resource-service
    Then the response status code should be 400
    And no file should be stored in S3
    And no resource record should be saved in the database
    And no Kafka event should be published

  Scenario: Download an existing resource
    Given a resource exists in the database with its MP3 file in S3
    When I request the stored resource by ID
    Then the response status code should be 200
    And the response content type should be "audio/mpeg"
    And the response body should contain the original MP3 binary

  Scenario: Download a non-existing resource
    When I request the resource with ID 999999
    Then the response status code should be 404

  Scenario: Delete an existing resource
    Given a resource exists in the database with its MP3 file in S3
    And the song-service will successfully delete songs for the resource
    When I delete the stored resource by ID
    Then the response status code should be 200
    And the resource record should be removed from the database
    And the MP3 file should be removed from S3
    And the song-service should have received a delete request

  Scenario: Delete resource when song-service is temporarily unavailable
    Given a resource exists in the database with its MP3 file in S3
    And the song-service is temporarily unavailable but recovers after 2 attempts
    When I delete the stored resource by ID
    Then the response status code should be 200
    And the song-service should have received 3 requests due to retries
