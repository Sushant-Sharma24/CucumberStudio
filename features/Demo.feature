Feature: Demo


  Scenario: Verify Facebook Login
    Given User Has Accessed Facebook Page
    And User Is Able To View User Name Text Field
    When User Enter Valid User Name
    And User Enter Valid Password
    Then User Is Navigated To Facebook Logged In
