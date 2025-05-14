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




    Scenario Outline: AC11: Given I supply a fully compliant address, when I submit the form,
    then the form is saved with the address I supplied.
      Given I am viewing the enter location details form on the <page_name> page
      When I enter a valid address and submit the location form on the <page_name> page
      Then The form from the <page_name> page is saved and contains the address I supplied
      Examples:
        | page_name               |
        |  "/register"            |




