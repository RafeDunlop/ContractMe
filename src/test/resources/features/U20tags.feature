@authoriseUser
Feature: U20 - As Inaya, I want to be able to tag my renovation records with common tags so that my renovation records
  are more easily browsable by others interested in those tags.

  Scenario Outline: AC1 - Typing in the tag entry field shows autocomplete suggestions
    Given There is an existing tag named "<existingTag>"
    When I type "<input>" into the tag input field
    Then I should see an autocomplete list containing "<existingTag>"
    Examples:
      | input | existingTag  |
      | ne    | new          |
      | bath  | bathroom     |
      | bui   | build        |

  Scenario Outline: AC2 - Entering a tag with only special characters and numbers returns an error.
    Given I have a renovation record and I am on that page
    When I enter a tag "<input>" into the tag input field
    Then I am told that a tag must contain one or more letters
    And The tag "<input>" is not added
    Examples:
      | input      |
      | 1234567890 |
      | !@#$%^&*() |
      |            |

  Scenario: AC5 - Entering a tag to a record with 5 tags returns an error.
    Given I have a renovation record and I am on that page
    And The record has 5 tags
    When I enter a tag "tag" into the tag input field
    Then I am told that I cannot add another tag
    And The tag "tag" is not added

  Scenario: AC6.1 - Typing a tag that doesn't exist will show a message saying that there are no matching tags
    Given I have a renovation record and I am on that page
    When I type "tag_doesn't_exist" into the tag input field
    Then I should see an autocomplete list that doesn't contain "tag_doesn't_exist"
    And I am told that there are no matching tags

  Scenario Outline: AC7 - Typing in the tag entry field shows autocomplete suggestions that are case insensitive
    Given There is an existing tag named "<existingTag>"
    When I type "<input>" into the tag input field
    Then I should see an autocomplete list containing "<existingTag>"
    Examples:
      | input  | existingTag  |
      | OL     | old          |
      | GrEeN  | green        |
      | bRE    | break        |
