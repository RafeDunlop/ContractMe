@registerUser
Feature: As Sarah, I want to edit my user profile so that I can keep my details accurate.

  Scenario: AC1
    Given I am on my profile page
    When I click the Edit button
    Then I see the edit profile form with all my details prepopulated except my password

  Scenario: AC2
    Given I am on the edit profile form
    And I enter valid values for my first name,last name, and email address
    When I click the Submit button
    Then my new details are saved
    And I am taken back to my profile page
