package com.example;

import cucumber.api.DataTable;
import cucumber.api.java.en.*;

public class StepDefinitions {
    public Actionwords actionwords = new Actionwords();

    @Given("^User Has Accessed Facebook Page$")
    public void userHasAccessedFacebookPage() {
        actionwords.userHasAccessedFacebookPage();
    }

    @Given("^User Is Able To View User Name Text Field$")
    public void userIsAbleToViewUserNameTextField() {
        actionwords.userIsAbleToViewUserNameTextField();
    }

    @When("^User Enter Valid User Name$")
    public void userEnterValidUserName() {
        actionwords.userEnterValidUserName();
    }

    @When("^User Enter Valid Password$")
    public void userEnterValidPassword() {
        actionwords.userEnterValidPassword();
    }

    @Then("^User Is Navigated To Facebook Logged In$")
    public void userIsNavigatedToFacebookLoggedIn() {
        actionwords.userIsNavigatedToFacebookLoggedIn();
    }
}