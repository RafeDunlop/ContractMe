@authoriseUser
Feature: U20 - As Inaya, I want to be able to tag my renovation records with common tags so that my renovation records
  are more easily browsable by others interested in those tags.

  Scenario Outline: AC1.1 - Typing in the tag entry field shows autocomplete suggestions
    Given There is an existing tag named "<existingTag>"
    When I type "<input>" into the tag input field
    Then I should see an autocomplete list containing "<existingTag>"
    Examples:
      | input | existingTag  |
      | ne    | new          |
      | bath  | bathroom     |
      | bui   | build        |

  Scenario: AC1.2 - Typing a tag that doesn't exist will show a message saying that there are no matching tags
    Given There is an existing tag named "tag_exists"
    When I type "tag_doesn't_exist" into the tag input field
    Then I should see an autocomplete list that doesn't contain "tag_doesn't_exist"

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

  Scenario: AC3 - Tags associated with a record are visible on the browse renovations page
    Given I have a renovation record with a tag "tagtag"
    When I go to the browse renovation page
    Then The tag "tagtag" is on the list of tags for the renovation

  Scenario Outline: AC4 - Inputted tags that are valid show up on the view renovations page
    Given I have a renovation record and I am on that page
    When I enter a tag "<input>" into the tag input field
    Then The tag "<input>" is added to the list of tags for the renovation
    Examples:
      | input       |
      | ttaagg      |
      | tag2        |
      | tag3!@#%$^% |

  Scenario: AC5 - Entering a tag to a record with 5 tags returns an error.
    Given I have a renovation record and I am on that page
    And The record has 5 tags
    When I enter a tag "tag" into the tag input field
    Then I am told that I cannot add another tag
    And The tag "tag" is not added

  Scenario Outline: AC7 - Typing in the tag entry field shows autocomplete suggestions that are case insensitive
    Given There is an existing tag named "<existingTag>"
    When I type "<input>" into the tag input field
    Then I should see an autocomplete list containing "<existingTag>"
    Examples:
      | input  | existingTag  |
      | OL     | old          |
      | GrEeN  | green        |
      | bRE    | break        |

  Scenario: AC8 - Pressing the 'X' button next to the tag name will delete the tag
    Given I have a renovation record with a tag "<input>"
    When I go to the view renovation page
    And Press the 'X' button next to the tag "<input>"
    Then The tag "<input>" is removed from the list of tags for the renovation