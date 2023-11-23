Feature: Search What3Words

  Scenario: Text Search Flow Using Google Maps
    Given The main screen is visible
    Then I change using MapBox
    When I type "///index.home.raf" into auto suggest text field
    Then Suggestions should show
    When I tape suggestion "index.home.raft"
    Then The auto suggestion text is "///index.home.raft"
    And Map show maker at "51.521251, -0.203586"

