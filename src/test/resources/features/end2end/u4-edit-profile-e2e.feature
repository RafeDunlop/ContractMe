@authoriseUser
Feature: As Sarah, I want to edit my user profile so that I can keep my details accurate.

  Scenario: AC1
    Given I am on my profile page
    When I click the Edit button
    Then I see the edit profile form with all my details prepopulated except my password
