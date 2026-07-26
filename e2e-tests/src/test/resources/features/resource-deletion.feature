Feature: Resource Deletion with Cascade
  As an API client
  I want to delete resources and have associated metadata cleaned up
  So that the system remains consistent with no orphaned data

  Background:
    Given the system is fully operational
    And the API gateway is accessible at "http://localhost:8084"

  Scenario: Delete resource cascades to song metadata
    Given a resource has been uploaded and its metadata has been processed
    And I can confirm the song metadata exists via GET "/songs/{songId}"
    When I send a DELETE request to "/resources?id={resourceId}"
    Then the response status code should be 200
    And the resource should no longer be retrievable via GET "/resources/{resourceId}"
    And the associated song metadata should no longer exist via GET "/songs/{songId}"

  Scenario: Delete multiple resources
    Given resources have been uploaded with IDs stored as "resource1" and "resource2"
    When I send a DELETE request to "/resources?id={resource1},{resource2}"
    Then the response status code should be 200
    And neither resource should be retrievable
    And neither song metadata record should exist

  Scenario: Delete non-existing resource
    When I send a DELETE request to "/resources?id=99999"
    Then the response status code should be 200
