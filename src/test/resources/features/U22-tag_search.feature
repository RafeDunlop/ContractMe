@authoriseUser
Feature: As Sarah, I want to be able to search for public renovation records by tags so that I can find renovations that are matching my interest.

  Scenario Outline: AC1 Tag autocomplete shows partially matched tags.
    Given I enter a search <searchQuery> in the search renovation bar
    When The search partially matches a tag known by the system <tagName>
    Then I can see a list of matching tags <matchingTag>

    Examples:
      | searchQuery | tagName    | matchingTag |
      | "te"        | "test"     | "test"      |
      | "bath"      | "bathroom" | "bathroom"  |
      | "bed"       | "bedroom"  | "bedroom"   |

  Scenario Outline: AC4 Tag search returns only public records in results.
    Given the tags named "<tag_one>" and "<tag_two>" exist
    And a public renovation "<renovation_name_one>" exists with tags:
      | <tag_one> |
      | <tag_two> |
    And a private renovation "<private_renovation_name>" exists with tags:
      | <tag_one> |
      | <tag_two> |
    When I search for renovations with tags "<tag_one>" and "<tag_two>"
    Then I should see the following renovations in order:
      | <renovation_name_one> |

    Examples:
      | renovation_name_one     | private_renovation_name | tag_one | tag_two |
      | PublicRenovation        | PrivateRenovation       | House   | Garden  |
      | Public                  | Private                 | spare   | new     |

  Scenario: AC5 Tag search returns no results and shows error message
    Given the tags named "NewBuild" and "Apartment" exist
    And a public renovation "HouseRenovation" exists with tags:
      | House |
    When I search for renovations with tags "NewBuild" and "Apartment"
    Then I should see no records

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
      | renovation_name_one | renovation_name_two | tag_one   | tag_two |
      | Renovation A        | Renovation B        | House     | Garden  |
      | Renovation1         | Renovation2         | bedroom   | kitchen |

