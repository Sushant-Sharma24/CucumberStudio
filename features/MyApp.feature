Feature: Test Facebook Snoke Scenario


  Scenario: Test Login With Valid Credentials
   Given User Open Facebook
   When  User Enter Valid User Name and Valid Password
   Then User Should Be Able To Login Successfully
  