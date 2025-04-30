Feature: Logout functionality
  Scenario: User logs out successfully
    Given I am logged in as a user
    When I click the logout button
    Then I should be redirected to the login page
    And I should not be able to access protected pages