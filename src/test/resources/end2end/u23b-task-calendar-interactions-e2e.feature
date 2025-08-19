@loginUser
Feature: As Kaia, I want to be able to create and edit tasks directly from the calendar view.

  Scenario: AC1: Open create task form
    Given I navigate to the renovation
    When I double click an empty space on a day
    Then I see the add task form
    And the due date is set to the date I clicked

  Scenario: AC2
    Given I have a renovation with a task due tomorrow
    And I navigate to the renovation
    When I double click on the task in the calendar
    Then I see the edit task page

  Scenario Outline: AC3.1
    Given I am viewing the calendar for this month with a task due on day <day of month>
    And I double click on day <day of month> to go to the <task form> form
    And I enter valid details to the <task form> form
    When I click the form button labelled <button>
    Then I am returned to the calendar view with the <day of month> of the edited task highlighted yellow if it is not the current day

    Examples:
      | day of month  | task form          | button       |
      | 1             | "Edit Task"        | "Cancel"     |
      | 10            | "Edit Task"        | "Submit"     |
      | 27            | "Create Task"      | "Cancel"     |
      | 16            | "Create Task"      | "Submit"     |


  Scenario: AC3.2
    Given I am viewing the calendar for this month with a task due on day 1
    When I double click on day 1 to go to the "Create Task" form
    Then I can see the "dueDate" field prefilled with day 1
