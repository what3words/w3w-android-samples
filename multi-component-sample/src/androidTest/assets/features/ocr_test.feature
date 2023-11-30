Feature: Search What3Words
   Scenario Outline: Search Using Auto Suggestion Text Field and Google Map
      Given The main screen is visible
      When tapped the OCR scan button
      Then accept the permission
      Then suggestions should show <suggestion>
      When tapped suggestion <suggestion>
      Then the autosuggest text is <suggestion>
      And map shows marker at "43.194036, -78.672785"

      Examples:
         | suggestion     |
         | home.land.page |

