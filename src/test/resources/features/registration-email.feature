@U6
Feature: As Sarah, I want to confirm my account by email when I register so that may account is more secure.

  @AC1
  Scenario: AC1 - Receive email upon successful registration
    Given I submit a fully valid registration form with first name "Jane", last name "Doe", email "jane@doe.com" and password "Strongpassword123!"
    When I click the register button
    Then A confirmation email is sent to my email address "jane@doe.com"
    And The email contains a message “if you didn’t register, ignore this email, and your account will be deleted in 10 minutes.”
    And I'm presented with a page asking for the signup code.


  @AC2
  Scenario: AC2 - Signup code and account expire after 10mins
    Given A signup code has been created for a new user with first name "Jane", last name "Doe", email "jane@doe.com" and password "Strongpassword123!"
    When 10 minutes have passed after the signup code was sent to the email address "jane@doe.com"
    Then The code and the account are deleted


  @AC3
  Scenario: AC3 - Signup code non functional after 10mins
    Given I received a signup code at the email "jane@doe.com"
    And The code has expired
    When I try to use the signup code
    Then An error message "Signup code invalid” is displayed


  @AC4
  Scenario: AC4 - Login requires signup code if regsitration incomplete
    Given I received a signup code at the email "jane@doe.com"
    And I have not confirmed my regsitration yet
    And My code has not expired
    When I want to log into the system for the first time
    Then I must use the signup code.


  @AC5
  Scenario: AC5 - Validate signup code
    Given I received a signup code at the email "jane@doe.com"
    And I navigate to the signup code page
    When I enter the signup code linked to my account
    Then The system validates the code successfully
    And I am redirected to the login page thast tells me “Your account has been activated, please log in”.

  @AC6
  Scenario: AC6 - Invalid signup code
    Given I am on the signup code page
    When I enter an invalid signup code
    Then An error message “Signup code invalid” is displayed
    And My registration is not confirmed