@loginUser
Feature: As Lei, I want to browse for contractors with a particular skill by location on a map so that I can customize which contractors are sent invites to join my team.

  Scenario: AC1 - Prompt appears before browsing for contractors
    Given I own a renovation with an address and no team
    When I create a team for that renovation
    Then I am prompted whether I want to select contractors automatically or choose them myself from a map

  Scenario: AC2 - Option to select contractors manually
    Given I have created a team
    When I select to choose contractors manually
    Then next to each role I see an option to select a contractor for that role on a map