@loginUser
Feature: As Sarah, I want to edit my user profile so that I can keep my details accurate.

  Scenario: AC1
    Given I am on my profile page
    When I click on the "Edit" button
    Then I see the edit profile form with all my details prepopulated except my password

  Scenario: AC2
    Given I am on the edit profile form
    And I enter valid values for my first name,last name, and email address
    When I click on the "Submit" button
    Then my new details are saved
    And I am taken back to my profile page

  Scenario: AC3.1
    Given I am on the edit profile form
    And I enter an empty first name
    When I click on the "Submit" button
    Then an error message tells me First name cannot be empty
    And no changes are saved

  Scenario: AC3.2
    Given I am on the edit profile form
    And I enter an invalid first name
    When I click on the "Submit" button
    Then an error message tells me First name must only include letters, spaces, hyphens, or apostrophes
    And no changes are saved

  Scenario: AC3.3
    Given I am on the edit profile form
    And I enter an invalid last name
    When I click on the "Submit" button
    Then an error message tells me Last name must only include letters, spaces, hyphens, or apostrophes
    And no changes are saved

  Scenario: AC4.1
    Given I am on the edit profile form
    And I enter an first name that is more than 64 characters
    When I click on the "Submit" button
    Then an error message tells me First name must be 64 characters long or less
    And no changes are saved

  Scenario: AC4.2
    Given I am on the edit profile form
    And I enter an last name that is more than 64 characters
    When I click on the "Submit" button
    Then an error message tells me Last name must be 64 characters long or less
    And no changes are saved

  Scenario Outline: AC5
    Given I am on the edit profile form
    And I enter an invalid email: <email>
    When I click on the "Submit" button
    Then an error message tells me Email address must be in the form ‘jane@doe.nz’
    And no changes are saved

    Examples:
      | ""           |

  Scenario: AC6
    Given I am on the edit profile form
    And I enter an email address associated to an account that already exists
    When I click on the "Submit" button
    Then an error message tells me This email address is already in use
    And no changes are saved


  Scenario: AC7
    Given I am on the edit profile form
    When I click on the "Cancel" button
    Then I am taken back to my profile page
    And no changes are saved





