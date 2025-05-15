@authoriseUser
Feature: As Sarah, I want to be able to search for public renovation records by tags so that I can find renovations that are matching my interest.

  Scenario: AC7 Submitting an empty tag search does nothing
    Given the tag search field is empty
    When I make a tag search
    Then I am not redirected

  Scenario Outline: AC8 Multiple tag search returns results ordered by number of matching tags and date
    Given the tags named "<tag_one>" and "<tag_two>" exist
    And a public renovation "<renovation_name_one>" exists with tags:
      | <tag_one> |
      | <tag_two> |
    And a public renovation "<renovation_name_two>" exists with tags:
      | <tag_one> |
    When I search for renovations with tags "<tag_one>" and "<tag_two>"
    Then I should see the following renovations in order:
      | <renovation_name_one> |
      | <renovation_name_two> |

    Examples:
      | renovation_name_one | renovation_name_two | tag_one | tag_two |
      | Renovation A        | Renovation B        | House   | Garden  |

