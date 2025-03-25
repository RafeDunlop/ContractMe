@U9
Feature: U9 - As Kaia, I want to create a record of my renovation so that I can manage all the tasks that must be
  completed

  @AC2
  Scenario Outline: AC2 - A user creates a renovation with valid values for name, description and optionally a list of rooms.
    Given The user is logged in
    When I create a renovation with renovation name <renovation_name>, description <description> and a list of rooms <list_of_rooms>
    Then The renovation is created with renovation name <renovation_name>, description <description> and a list of rooms <list_of_rooms>
    And I am taken to the renovation record details page
    Examples:
    | renovation_name      | description  | list_of_rooms           |
    | "myReno"             | "cool"       |            []           |
    | "Jake's renovation"  | "extra cool" | ["bedroom", "kitchen"]  |


  @AC3
  Scenario Outline: AC3 - A renovation record cannot be empty, or include on-alphanumeric characters other than spaces, dots, commas, dots,
  hyphens, or apostrophes.
    Given The user is logged in
    When I create an invalid renovation with renovation name <renovation_name>
    Then I cannot create a renovation record
    Examples:
      | renovation_name  |
      | ""               |
      | "$#@"            |

  @AC4
  Scenario Outline: AC4 - A renovation record name must be unique across all the users renovation records.
    Given The user is logged in
    And There exists a renovation record with renovation name <renovation_name> created by the user
    When I create a renovation record with renovation name <renovation_name>
    Then I recieve name is not unique error
    And An additional renovation record is not created
    Examples:
      | renovation_name      |
      | "Greg"               |
      | "Steve's Renovation" |

