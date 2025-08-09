@authoriseUser
Feature: U2003 - As Sarah, I want to be able to create a team for my renovation so that I can hire contractors.

  Scenario: AC1 - Go to create team form
    Given I am on the view renovation page for a renovation I own that has a location listed and that doesn't have a team
    When I click the create team button
    Then I can select roles for my renovation

  Scenario: AC2.1 - I can submit with a valid number of roles which is between 1 and 5
    Given I am on the view renovation page for a renovation I own that has a location listed and that doesn't have a team
    And I click the create team button
    When I add a valid amount of skills
    Then My team request is successfully created

  Scenario: AC2.2 - Submission fails when I submit with more then 5 roles and it shows an error message
    Given I am on the view renovation page for a renovation I own that has a location listed and that doesn't have a team
    And I click the create team button
    When I submit with more then five skills
    Then An error message displays, "Your team request cannot have more than 5 roles."

  Scenario: AC4 - I can't create a team request with no skills added
    Given I am on the view renovation page for a renovation I own that has a location listed and that doesn't have a team
    And I click the create team button
    When I add zero skills
    Then An error message displays, "Your team request must have at least one role."

  Scenario Outline: AC5 - When I have added a certain skill to the form, I can add a duplicate of that same skill
    Given I am on the view renovation page for a renovation I own that has a location listed and that doesn't have a team
    When I click the create team button
    Then I can add the skill "<skill>" twice to the same team

    Examples:
      | skill                   |
      | ELECTRICAL              |
      | DRYWALL_PLASTERING      |
      | SPLASHBACK_INSTALLATION |
      | TILING                  |
