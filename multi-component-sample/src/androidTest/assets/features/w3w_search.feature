Feature: Search What3Words

  Scenario: Search Using Auto Suggestion Text Field and Google Map
    Given The main screen is visible
    When I type "///index.home.raf" into auto suggest text field
    Then Suggestions should show
    When I tape suggestion "index.home.raft"
    Then The auto suggestion text is "///index.home.raft"
    And GoogleMap show maker at "51.521251, -0.203586"

  Scenario: Search Using Auto Suggestion Text Field and MapBox
    Given The main screen is visible
    Then I change using MapBox
    When I type "///index.home.raf" into auto suggest text field
    Then Suggestions should show
    When I tape suggestion "index.home.raft"
    Then The auto suggestion text is "///index.home.raft"
    And MapBox show maker at "51.521251, -0.203586"

