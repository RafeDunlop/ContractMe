@loginUser
Feature: As Sarah, I want to be able to remove a team from my renovation record so that I do not have teams that I no longer need.

  Scenario: AC1 - Prompt appears before deleting a team
    Given I am viewing the renovation view page for a renovation I own with a team
    When I click the "Delete Team" button
    Then I see the delete team confirmation prompt
    And the prompt text mentions deleting the team
    And the prompt shows "Delete" and "Cancel" actions

  Scenario: AC3 - Cancelling does not delete the team
    Given I am viewing the renovation view page for a renovation I own with a team
    And I click the "Delete Team" button
    When I choose "Cancel" in the prompt
    Then I still see the "Delete Team" button for that renovation
    And I remain on the renovation view page