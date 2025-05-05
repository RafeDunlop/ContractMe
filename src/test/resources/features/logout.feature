Feature: Logout functionality
  Scenario: User logs out successfully
    Given I am logged in as a user
    When I logout
    Then I should be redirected to the login page
    And I should not be able to access protected pages