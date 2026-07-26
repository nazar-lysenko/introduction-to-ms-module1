Feature: System Resilience
  As a system operator
  I want the system to handle transient failures gracefully
  So that data is eventually consistent even when services restart

  Background:
    Given the system is fully operational
    And the API gateway is accessible at "http://localhost:8084"

  Scenario: Processing recovers after resource-processor restart
    Given I upload an MP3 file via POST to "/resources"
    And the resource is stored successfully
    When the resource-processor service is restarted
    Then within 60 seconds the song metadata should be available
    And the metadata should match the MP3 file content

  Scenario: Upload succeeds when song-service is temporarily slow
    Given the song-service replicas are under heavy load
    When I upload an MP3 file via POST to "/resources"
    Then the upload response status code should be 200
    And within 45 seconds the song metadata should eventually be created
