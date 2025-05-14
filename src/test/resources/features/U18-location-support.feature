Feature:As Kaia, I want to be able to add location to my profile and
  my renovation records so that I can keep track of where they are.
  Scenario: AC1: Given I register to the system, when I am asked to supply my details, then I can
  optionally supply my location.
    Given I am on the register form
    When I click the location toggle switch
    Then I can see the add location input fields


  Scenario Outline: AC8.1 non valid city is not accepted
    Given I am on the register form
    When I enter an invalid postcode: "<city>"
    Then I am taken back to the register form
    And a message tells me that the city contains invalid characters
    Examples:
      | city              |
      | Christ23church    |
      | Wellington%       |
      | Moscow (Russia)   |
      | Sy^dney            |

  Scenario Outline: AC8.2 valid city is accepted
    Given I am on the register form
    When I enter an valid city: "<city>"
    Then I am taken to the confirm register page
    Examples:
      | city              |
      | Christchurch      |
      | Wellington        |
      | Moscow            |
      | Sydney            |



  Scenario Outline: AC9.1 non valid postcode is not accepted
    Given I am on the register form
    When I enter an invalid postcode: "<postcode>"
    Then I am taken back to the register form
    And a message tells me that Postcode contains invalid characters
    Examples:
      | postcode     |
      | 123-456      |
      | 12@34        |
      | A1B*2C3      |
      | A1B  2C3     |
      | A1B2C3!      |
      | (A1B2C3)     |
      |             |
      |     1234    |
      | 1234     56 |

  Scenario Outline: AC9.2 valid postcode is accepted
    Given I am on the register form
    When I enter an valid postcode: "<postcode>"
    Then I am taken to the confirm register page

    Examples:
      | postcode  |
      | 12345     |
      | A1B 2C3   |
      | AB123CD   |
      | 75008     |
      | 1010      |
      | W1A 1AA   |