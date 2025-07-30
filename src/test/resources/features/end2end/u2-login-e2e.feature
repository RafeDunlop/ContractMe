Feature: As Sarah, I want to log into the system so that I can have a personalised experience with it and enjoy its features.

  Scenario: AC1
    Given I connect to the system's main URL
    When I see the homepage
    Then It indicates a button labelled "Sign in"

  Scenario: AC3
    Given I am on the login form
    When I click a highlighted link with the text "Not registered? Create an account"
    Then I am taken to the registration page

  Scenario Outline: AC4.1
    Given I am on the login form
    And I enter an email address <email>
    When I click the "Sign in" button
    Then An error message tells me "Email address must be in the form 'jane@doe.nz'."
    Examples:
      | email          |
      | "hello"        |
      | "@gmail.com"   |
      | "!@gmail.com"  |
      | "a.@gmail.com" |
      | "jane@doe"     |
      | "jane@doe.c"   |

  Scenario: AC4.2
    Given I am on the login form
    And I enter an email address ""
    When I click the "Sign in" button
    Then An error message tells me "Email address must be in the form 'jane@doe.nz'."

  Scenario: AC5
    Given I am on the login form
    And I enter an email address that is unknown to the system
    When I click the "Sign in" button
    Then An error message tells me "The email address is unknown, or the password is invalid."

  Scenario: AC7
    Given I am on the login form
    When I click the "Cancel" button
    Then I am taken back to the system’s home page

#  Scenario: AC6
#    Given I am on the login form, and I enter an empty password or the wrong password for the corresponding email address.
#    When I click the “Sign In” button.
#    Then then an error message tells me “The email address is unknown, or the password is invalid”.
#