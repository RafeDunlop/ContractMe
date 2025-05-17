Feature:As Kaia, I want to be able to add location to my profile and
  my renovation records so that I can keep track of where they are.
  Scenario: AC1: Given I register to the system, when I am asked to supply my details, then I can
  optionally supply my location.
    Given I am on the register form
    When I click the location toggle switch
    Then I can see the add location input fields


  Scenario Outline: AC5:  Given I am facing a form that asks for my location, when I want to give my location, then I must
  supply a street address with the street number, optionally a suburb, a city, a postcode, and a country.
    Given I am viewing the enter location details form on the <page_name> page
    When I leave the address field blank but fill any other field on the location form on the <page_name> page
    Then I am told that I must supply an address field
    Examples:
      | page_name               |
      |  "/register"            |


    Scenario Outline: AC7:  Given I supply a suburb, when the suburb contains non valid characters (i.e. characters others than
    letters, hyphen, apostrophe, number, space), then a message tells me that “Suburb contains invalid characters.”
    and the form is not saved.
      Given I am viewing the enter location details form on the <page_name> page
      When I enter a valid address but an invalid suburb and submit the form on the <page_name> page
      Then I am taken back to the <page_name> page
      And I am told that I have entered an invalid suburb
      Examples:
        | page_name               |
        |  "/register"            |



  Scenario Outline: AC8: Given I supply a city, when the city contains non valid characters (i.e. characters
  other than letters, hyphen, apostrophe, space), then a message tells me that “City contains invalid characters.”
  and the form is not saved.
    Given I am viewing the enter location details form on the <page_name> page
    When I enter a valid address but an invalid city and submit the form on the <page_name> page
    Then I am taken back to the <page_name> page
    And I am told that I have entered an invalid city
    Examples:
      | page_name               |
      | "/register"             |



  Scenario Outline: AC9.1 non valid postcode is not accepted
    Given I am on the register form
    When I enter an invalid postcode: "<postcode>"
    Then I am taken back to the register form
    And a message tells me that Postcode contains invalid characters
    Examples:
      | postcode     |
      | 123-456      |  # Hyphen is invalid
      | 12@34        |  # Symbol (@)
      | A1B*2C3      |  # Asterisk is invalid
      | A1B  2C3     |  # Multiple spaces
      | A1B2C3!      |  # Ends with symbol
      | (A1B2C3)     |  # Parentheses
      | 1234     56  |  # Multiple internal spaces

  Scenario Outline: AC9.2 valid postcode is accepted
    Given I am on the register form
    When I enter a valid postcode: "<postcode>"
    Then I am taken to the confirm register page

    Examples:
      | postcode  |
      | 12345     |
      | A1B 2C3   |
      | AB123CD   |
      | 75008     |
      | 1010      |
      | W1A 1AA   |


    Scenario Outline: AC10: Given I supply a country, when the country contains non valid characters (i.e. characters
    other than letters, hyphen, apostrophe, single space), then a message tells me that “Country contains invalid
    characters.” and the form is not saved
      Given I am viewing the enter location details form on the <page_name> page
      When I enter a valid address but an invalid country and submit the form on the <page_name> page
      Then I am taken back to the <page_name> page
      And I am told that I have entered an invalid country
      Examples:
        | page_name               |
        |  "/register"            |


  Scenario Outline: AC11: Given I supply a fully compliant address, when I submit the form,
  then the form is saved with the address I supplied.
    Given I am viewing the enter location details form on the <page_name> page
    When I enter a valid address and submit the location form on the <page_name> page
    Then The form from the <page_name> page is saved and contains the address I supplied
    Examples:
      | page_name               |
      |  "/register"            |


