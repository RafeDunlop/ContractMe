@authoriseUser
Feature: U2001 - As Sarah, I want to be able to create a team for my renovation so that I can hire contractors.

  Scenario: AC1 - Go to create team form
    Given I am on the view renovation page for a renovation I own that has a location listed and that doesn't have a team
    When I click the create team button
    Then I can select roles for my renovation

  Scenario: AC4 - Creating roles with one skill
    Given I am on the create team form
    When I add a role
    Then I must select exactly one skill for that role

  Scenario Outline: AC5 -
    Given I am on the create team form
    When I have added the role with the skill <skill>
    Then I can add the skill <skill> again

    Examples:
      | skill              |
      | Electrical         |
      | Drywall Plastering |
      | Joinery            |
      | Gas Fitting        |
