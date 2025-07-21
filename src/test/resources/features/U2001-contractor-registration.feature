Feature: U2001 - As Bob, I want to be able to register as a contractor and add my skills.

  Scenario: AC1 - Display Contractor Registration Form
    Given I am on the registration form
    When I select the contractor button
    Then it includes fields where I can add my skills, phone number, and hourly rate

  Scenario Outline: AC2 - Invalid Phone Number
    Given I am on the registration form
    When I enter an invalid phone number <phone_number>
    Then a "phoneNumberError" error message tells me "Your phone number is invalid"
    Examples:
    | phone_number     |
    |"0"      |
    |"1234567899999999999999999999999999999999"        |
    |"a" |


  Scenario: AC3 - No Phone Number
    Given I am on the registration form
    When I don't enter a phone number
    Then a "phoneNumberError" error message tells me "You must enter a phone number"

  Scenario: AC4 - No Location
    Given I am on the registration form
    When I don't enter location details
    Then a "locationError" error message tells me "You must enter a location"

  Scenario: AC9 - All Valid Details
    Given I am on the registration form
    And I enter valid user details "Bob", email address "bob@bob.com", skills "JOINERY", a phone number "02121212121", and a location
    Then the form is saved with the contractor details I supplied

  Scenario: AC10 - Invalid Hourly Rate
    Given I am on the registration form
    When I enter an invalid hourly rate -10.0
    Then a "hourlyRateError" error message tells me "Invalid hourly rate"
