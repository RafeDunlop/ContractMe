Feature: As Bob, I want to be able to edit my contractor details so that I can keep my skills and contact info up to date.

  @authoriseContractor
  Scenario: AC1.1 - Contractor details are on edit profile form if I am a contractor
    When I click the edit profile button
    Then I see fields for my skills, phone number, hourly rate, and location

  @authoriseUser
  Scenario: AC1.2 - Contractor details are not on edit profile form if I am a not contractor
    When I click the edit profile button
    Then I don't see the fields for my skills, phone number, and hourly rate

  @authoriseContractor
  Scenario Outline: AC2 - An error is shown when an invalid phone number is submitted
    Given I am editing the contractor details
    And I enter the phone number "<phoneNumber>"
    When I submit the edit profile form
    Then An error message tells me "Your phone number is invalid"
    Examples:
      | phoneNumber        |
      | 0                  |
      | 1234567            |
      | 1234567890123456   |
      | 1234567a           |
      | 1234567.           |
      | 1234 567           |

  @authoriseContractor
  Scenario: AC3 - An error is shown when an empty phone number is submitted
    Given I am editing the contractor details
    And I enter the phone number ""
    When I submit the edit profile form
    Then An error message tells me "You must enter a phone number"

  @authoriseContractor
  Scenario Outline: AC7 - Contractor details must contain one or more skills
    Given I am editing the contractor details
    And I add "<skills>" to the skills input
    When I submit the edit profile form
    Then My skills are updated to the new values
    Examples:
      | skills                   |
      | CARPENTRY                |
      | HVAC,WELDING             |
      | SURVEYING,JOINERY,TILING |

  @authoriseContractor
  Scenario: AC8 - An error is shown when no skills are submitted
    Given I am editing the contractor details
    And I don't have any skills in the input field
    When I submit the edit profile form
    Then An error message tells me "You must select one or more skills"

  @authoriseContractor
  Scenario Outline: AC11 - An error is shown when an invalid hourly rate is submitted
    Given I am editing the contractor details
    And I enter an hourly rate <hourlyRate>
    When I submit the edit profile form
    Then An error message tells me "Invalid hourly rate"
    Examples:
      | hourlyRate |
      | -999999999 |
      | -0.0000001 |