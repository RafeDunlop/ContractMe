@authoriseContractor
Feature: As Bob, I want to be able to edit my contractor details so that I can keep my skills and contact info up to date.

  Scenario: AC1.2 - Contractor details are on edit profile form if I am a contractor
    When I click the edit profile button
    Then I see field for my skills, phone number, hourly rate, and location

  Scenario: AC1.2 - Contractor details are not on edit profile form if I am a not contractor
    Given I am not a contractor
    When I click the edit profile button
    Then I don't see the fields for my skills, phone number, and hourly rate

  Scenario Outline: AC2 - An error is shown when an invalid phone number is submitted
    Given I am editing the contractor details
    When I enter the phone number <phoneNumber> and submit the form
    Then An error message tells me "Your phone number is invalid"
    Examples:

  Scenario: AC3 - An error is shown when an empty phone number is submitted
    Given I am editing the contractor details
    When I enter the phone number "" and submit the form
    Then An error message tells me "Your phone number is invalid"

  Scenario: AC7 -

