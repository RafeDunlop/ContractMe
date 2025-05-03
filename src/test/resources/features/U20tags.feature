Feature: U20 - As Inaya, I want to be able to tag my renovation records with common tags so that my renovation records
  are more easily browsable by others interested in those tags.

  Scenario Outline: AC1.1 - Typing in the tag entry field shows autocomplete suggestions
    Given there is an existing tag named "<existingTag>"
    When I type "<input>" into the tag input field
    Then I should see an autocomplete list containing "<existingTag>"

    Examples:
      | input | existingTag  |
      | ne    | new          |
      | Bath  | bathroom     |
      | bui   | Build        |



