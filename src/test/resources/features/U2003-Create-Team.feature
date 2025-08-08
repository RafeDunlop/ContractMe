@authoriseUser
Feature: U2003 - As Sarah, I want to be able to create a team for my renovation so that I can hire contractors.

  Scenario: AC1 - Go to create team form
    Given I am on the view renovation page for a renovation I own that has a location listed and that doesn't have a team
    When I click the create team button
    Then I can select roles for my renovation

  Scenario: AC4 - Creating roles with one skill
    Given I am on the view renovation page for a renovation I own that has a location listed and that doesn't have a team
    And I click the create team button
    When I add zero roles
    Then I must select at least one skill for the request

  Scenario Outline: AC5 -
    Given I am on the view renovation page for a renovation I own that has a location listed and that doesn't have a team
    When I click the create team button
    Then I can add the skill "<skill>" twice to the same team

    Examples:
      | skill                |
      | Electrical           |
      | Drywall / Plastering |
      | Joinery              |
      | Gas Fitting          |
