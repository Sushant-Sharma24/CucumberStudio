package stepDefinition;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import cucumber.api.java.en.Given;

public class smokeTest {

	WebDriver driver;
	@Given("^User Open Facebook$")
	public void OpenBroAndApp()
	{
		String driverPath = "/C:/Users/sushant.sharma/workspace/Tech_direct/Browser/";
		System.setProperty("webdriver.chrome.driver", driverPath + "chromedriver.exe");
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.get("https://clientauthtest.lifecare.com/index.html");
	}
	
	@Given("^User Enter Valid User Name and Valid Password$")
	public void EnterLoginDetails()
	{
		driver.findElement(By.id("ScreenNamebox")).clear();
		driver.findElement(By.id("ScreenNamebox")).sendKeys("lifedemo");
		driver.findElement(By.id("Passwordbox")).clear();
		driver.findElement(By.id("Passwordbox")).sendKeys("login");
		driver.findElement(By.xpath("//input[@title='Log In']")).click();
	}
	@Given("^User Should Be Able To Login Successfully$")
	public void CheckLoginSuccess() throws InterruptedException
	{
		driver.findElement(By.id("navigationBrowse")).click();
		Thread.sleep(3000);
		driver.findElement(By.xpath("(//a[text()=' Senior Care Management'])[1]")).click();
	}
}
