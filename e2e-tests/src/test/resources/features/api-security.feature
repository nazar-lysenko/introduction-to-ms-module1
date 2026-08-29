Feature: API Security and Role-Based Access Control
  As the owner of the platform
  I want every API call to carry a valid JWT and be checked against the caller's role
  So that only administrators can modify data while regular users can only read it

  Background:
    Given the system is fully operational
    And the API gateway is accessible at "http://localhost:8084"

  Scenario: Reading storages without a token is rejected
    Given I am not authenticated
    When I send a "GET" request to "/storages"
    Then the response status code should be 401

  Scenario: Creating a storage without a token is rejected
    Given I am not authenticated
    When I send a "POST" request to "/storages"
    Then the response status code should be 401

  Scenario: Deleting a storage without a token is rejected
    Given I am not authenticated
    When I send a "DELETE" request to "/storages?id=99999"
    Then the response status code should be 401

  Scenario: A forged JWT is rejected
    Given I present a forged JWT token
    When I send a "GET" request to "/storages"
    Then the response status code should be 401

  Scenario: A regular user can read storages
    Given I am authenticated as a regular user
    When I send a "GET" request to "/storages"
    Then the response status code should be 200

  Scenario: A regular user cannot create storages
    Given I am authenticated as a regular user
    When I send a "POST" request to "/storages"
    Then the response status code should be 403

  Scenario: A regular user cannot delete storages
    Given I am authenticated as a regular user
    When I send a "DELETE" request to "/storages?id=99999"
    Then the response status code should be 403

  Scenario: A regular user cannot upload resources
    Given I am authenticated as a regular user
    When I attempt to upload an MP3 file to "/resources"
    Then the response status code should be 403

  Scenario: A regular user cannot delete resources
    Given I am authenticated as a regular user
    When I send a "DELETE" request to "/resources?id=99999"
    Then the response status code should be 403

  Scenario: A regular user can read the song metadata of an existing resource
    Given a resource has been uploaded and its metadata has been processed
    And I am authenticated as a regular user
    When I send a "GET" request to "/songs/{songId}"
    Then the response status code should be 200

  Scenario: An admin can create and delete storages
    Given I am authenticated as an admin
    When I attempt to create a storage entry
    Then the response status code should be 200
    And I delete the storage entry created in this scenario
    Then the response status code should be 200
    And the seeded storage entries should still be present

  Scenario: The storage service itself rejects unauthenticated calls
    Given I am not authenticated
    When I send a "GET" request directly to the storage service at "/storages"
    Then the response status code should be 401

  Scenario: The storage service itself rejects writes from a regular user
    Given I am authenticated as a regular user
    When I send a "DELETE" request directly to the storage service at "/storages?id=99999"
    Then the response status code should be 403
