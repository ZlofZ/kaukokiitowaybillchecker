package kaukokiito.waybills.core;

import IO.IOController;
import kaukokiito.waybills.util.DeliveryStatus;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class SeleniumControllerKaukokiito{
	private static WebDriver driver;
	private static WebDriverWait wait;
	private static final String KAUKOKIITO_URL = "https://www.kaukokiito.fi/en/kaukoputki/track-and-trace/";
	private IOController io;
	private static WebElement deliveryStatusElement = null;
	
	private void enterCode(String code){
		WebElement element = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/div/div/div/div[1]/div/div/div[1]/div[1]/div/div/input"));
		element.sendKeys(Keys.CONTROL,"a");
		element.sendKeys(code);
	}
	private void clickSearch(){
		WebElement element = driver.findElement(By.xpath("//*[@id=\"track-and-trace-basic-submit\"]/button"));
		wait.until(ExpectedConditions.elementToBeClickable(element));
		element.click();
	}
	
	private String getDeliveryStatus(){
		String xpath1 = "/html/body/div[2]/div[2]/div/div/div/div/div[5]/div/div[2]/div/button/div[1]/div/div/div";
		String xpath2 = "/html/body/div[2]/div[2]/div/div/div/div/div[5]/div/div[2]/div[1]/div/div[1]/div/div/p/text()";
		By loc = By.xpath(xpath1);
		String ret;
		
		try{
			if(deliveryStatusElement==null){
				wait.until(ExpectedConditions.presenceOfElementLocated(loc));
			}else{
				wait.until(ExpectedConditions.stalenessOf(deliveryStatusElement));
			}
			deliveryStatusElement=driver.findElement(loc);
			ret = deliveryStatusElement.getAttribute("class");
		} catch(NoSuchElementException e){
			ret = "Not found";
		}
		
		return ret;
	}
	
	private DeliveryStatus checkStatus(String status, String code){
		switch(status){
			case "Goods are shipping":
			case "Order received":
			case "delivery-status-description delivery-yellow":
				System.out.printf("%-15s|%15s\n",code,"Not Delivered");
				return DeliveryStatus.NOT_DELIVERED;
			case "Shipment delivered":
			case "delivery-status-description delivery-green":
				System.out.printf("%-15s|%15s\n",code,"Delivered");
				return DeliveryStatus.DELIVERED;
			default:
				System.out.printf("%-15s|%15s\n",code,"Invalid");
				return DeliveryStatus.INVALID;
		}
	}
	
	public DeliveryStatus doCheck(String code){
		if(code.startsWith("_")){
			//System.out.println("["+code+"] starts with underscore");
			code = code.substring(1);
		}
		enterCode(code);
		clickSearch();
		String status = getDeliveryStatus();
		return checkStatus(status, code);
	}
	
	public void stopDriver(){
		driver.quit();
	}
	
	public SeleniumControllerKaukokiito(IOController ioc){
		io = ioc;
		System.setProperty("webdriver.chrome.driver" , "chromedriver.exe");
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--no-sandbox");
		chromeOptions.addArguments("disable-gpu");
		chromeOptions.setHeadless(true);
		driver = new ChromeDriver(chromeOptions);
		//System.setProperty("webdriver.opera.driver" , io.getLaunchPath()+"operadriver.exe");
		//driver = new OperaDriver();
		//System.setProperty("webdriver.gecko.driver" , io.getLaunchPath()+"geckodriver.exe");
		//driver = new FirefoxDriver();
		driver.manage().window().setPosition(new Point(-1920, 0));
		driver.manage().window().maximize();
		wait  = new WebDriverWait(driver , 10);
		driver.navigate().to(KAUKOKIITO_URL);
	}
}