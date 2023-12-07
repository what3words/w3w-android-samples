Feature: Search What3Words
   Scenario Outline: Search Using OCR
      Given The main screen is visible ocr
      When tapped the OCR scan button
      Then accept the permission
      Then suggestions should show <suggestion>
      When tapped suggestion <suggestion>
      And the autosuggest text is <suggestion>

      Examples:
         | suggestion     |
         | home.land.page |

