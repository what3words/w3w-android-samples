Feature: Voice Search What3Words

  @CustomComposable
  Scenario: Search using voice auto suggestion and google map
     Given The main screen is visible voice
     When tapped the voice button
     Then accept the voice permission
     Then suggestions by voice should show
     When I tape suggestion "filled.count.soap"
     And The auto suggestion text is "///filled.count.soap"



