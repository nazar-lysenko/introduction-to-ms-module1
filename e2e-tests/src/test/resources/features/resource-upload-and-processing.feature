Feature: Resource Upload and Processing Pipeline
  As an API client
  I want to upload MP3 files and have their metadata automatically extracted
  So that I can later query song information separately from the binary

  Background:
    Given the system is fully operational
    And the API gateway is accessible at "http://localhost:8084"

  Scenario: Upload MP3 and verify metadata is automatically extracted
    Given I have an MP3 file "classpath:files/test-song.mp3" with embedded metadata:
      | artist      | album      | name        | year | duration |
      | Test Artist | Test Album | Test Title  | 2025 | 00:07    |
    When I upload the MP3 file via POST to "/resources"
    Then the response status code should be 200
    And the response body should contain the created resource ID
    And within 30 seconds the song metadata should be available via GET "/songs/{id}"
    And the song metadata should match:
      | artist      | album      | name        | year | duration |
      | Test Artist | Test Album | Test Title  | 2025 | 00:07    |

  Scenario: Upload MP3 with partial metadata is stored as a resource
    Given I have an MP3 file "classpath:files/minimal-tags.mp3" with only artist and title
    When I upload the MP3 file via POST to "/resources"
    Then the response status code should be 200
    And the response body should contain the created resource ID

  Scenario: Reject non-MP3 file upload
    Given I have a text file "not-an-mp3.txt"
    When I attempt to upload the file via POST to "/resources"
    Then the response status code should be 400
    And no song metadata should be created
