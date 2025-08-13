Feature: As Bob, I want to be able to edit my contractor details so that I can keep my skills and contact info up to date.

  @authoriseContractor
  Scenario: AC1.1 - Contractor details are on edit profile form if I am a contractor
    When I click the edit profile button
    Then I see fields for my skills, phone number, hourly rate, and location

  @authoriseUser
  Scenario: AC1.2 - Contractor details are not on edit profile form if I am a not contractor
    When I click the edit profile button
    Then I don't see the fields for my skills, phone number, and hourly rate

#  @authoriseContractor
#  Scenario Outline: AC2 - An error is shown when an invalid phone number is submitted
#    Given I am editing the contractor details
#    When I enter the phone number <phoneNumber> and submit the form
#    Then An error message tells me "Your phone number is invalid"
#    Examples:
#
#  @authoriseContractor
#  Scenario: AC3 - An error is shown when an empty phone number is submitted
#    Given I am editing the contractor details
#    When I enter the phone number "" and submit the form
#    Then An error message tells me "Your phone number is invalid"
#
#  @authoriseContractor
#  Scenario : AC7 -
#    Given I am editing the contractor details
#    And I make changes to the skills in the input field, and I have at least one skill in the input field
#    When I submit the form
#    Then My skills are updated to the new values
#
#  @authoriseContractor
#  Scenario: AC8 -
#    Given I am editing the contractor details
#    And I don't have any skills in the input field
#    When I submit the form
#    Then An error message tells me "You must select one or more skills"
#
#  @authoriseContractor
#  Scenario: AC11 -
#    Given I am editing the contractor details
#    When I enter an invalid hourly rate <hourlyRate>
#    Then An error message tells me "Invalid hourly rate"