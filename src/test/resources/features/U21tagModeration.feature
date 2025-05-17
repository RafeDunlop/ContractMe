@authoriseUser
Feature: U21 - As Inaya, I want to be able to know when I input an inappropriate tag so that the system remains free
  from inappropriate language.

  Scenario Outline: AC1 - Typing an inappropriate tag name will show a warning that the tag is not allowed
    Given I have a renovation record and I am on that page
    When I enter a tag "<input>" into the tag input field
    Then I am told that the tag name is inappropriate
    And The tag "<input>" is not added
    Examples:
      | input   |
      | ass     |
      | frigged |
      | turd    |