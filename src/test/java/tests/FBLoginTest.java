package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;
import pages.FBHomepage;
import pages.FBLoginpage;

public class FBLoginTest
{
	public static void main(String[] args) throws Exception
	{
		//Connect to Excel file
		File f=new File("facebooklogintest.xlsx");
		FileInputStream fi=new FileInputStream(f);
		Workbook wb=WorkbookFactory.create(fi);
		Sheet sh=wb.getSheet("Sheet1");
		int nour=sh.getPhysicalNumberOfRows();
		int nouc=sh.getRow(0).getLastCellNum();
		//Give headings to results in excel file
		SimpleDateFormat sf=new SimpleDateFormat("dd-MMM-yyyy-hh-mm-ss");
		Date dt=new Date();
		String cname=sf.format(dt);
		sh.getRow(0).createCell(nouc).setCellValue("Result on "+cname);
		//Data Driven from 2nd row(index=1)
		for(int i=1;i<nour;i++)
		{
			//Read data from excel
			DataFormatter df=new DataFormatter();
			String eadd=df.formatCellValue(sh.getRow(i).getCell(0));
			String eaddc=df.formatCellValue(sh.getRow(i).getCell(1));
			String pass=df.formatCellValue(sh.getRow(i).getCell(2));
			String passc=df.formatCellValue(sh.getRow(i).getCell(3));
			
			//Launch browser
			WebDriverManager.chromedriver().setup();
			RemoteWebDriver driver=new ChromeDriver();
			//Launch site and maximize
			driver.get("https://www.facebook.com");
			driver.manage().window().maximize();
			//Create page classes
			FBLoginpage fblp=new FBLoginpage(driver);
			FBHomepage fbhp=new FBHomepage(driver);
			//Create wait object
			WebDriverWait wait=new WebDriverWait(driver,20);
			wait.until(ExpectedConditions.visibilityOf(fblp.emailaddress));
			fblp.emailAddressFill(eadd);
			wait.until(ExpectedConditions.visibilityOf(fblp.pass));
			fblp.passFill(pass);
			wait.until(ExpectedConditions.elementToBeClickable(fblp.loginbtn));
			fblp.loginbtnClick();
			Thread.sleep(6000);
			//Validations
			try
			{
				if(eadd.length()==0 && passc.equalsIgnoreCase("valid") && fblp.blank_and_nondomain_email_address_err.isDisplayed())
				{
					sh.getRow(i).createCell(nouc).setCellValue("Blank/Nondomain email address test passed");
				}
				else if(eaddc.equalsIgnoreCase("invalid") && passc.equalsIgnoreCase("valid") && fblp.invalid_email_address_err.isDisplayed())
				{
					sh.getRow(i).createCell(nouc).setCellValue("Invalid email address test passed");
				}
				else if(eaddc.equalsIgnoreCase("valid") && passc.equalsIgnoreCase("blank") && fblp.blank_and_invalid_pass.isDisplayed())
				{
					sh.getRow(i).createCell(nouc).setCellValue("Blank/Invalid password test passed");
				}
				else if(eaddc.equalsIgnoreCase("valid") && passc.equalsIgnoreCase("invalid") && fblp.blank_and_invalid_pass.isDisplayed())
				{
					sh.getRow(i).createCell(nouc).setCellValue("Blank/Invalid password test passed");
				}
				else if(eaddc.equalsIgnoreCase("valid") && passc.equalsIgnoreCase("valid") && fbhp.profile_pic.isDisplayed())
				{
					sh.getRow(i).createCell(nouc).setCellValue("Login test passed");
					//Do logout
					fbhp.profilePicClick();
					wait.until(ExpectedConditions.visibilityOf(fbhp.logout));
					fbhp.logoutClick();
					wait.until(ExpectedConditions.visibilityOf(fblp.pass));
				}
				else
				{
					File src=driver.getScreenshotAs(OutputType.FILE);
					String ssname=sf.format(dt)+".png";
					File dest=new File(ssname);
					FileHandler.copy(src,dest);
					sh.getRow(i).createCell(nouc).setCellValue("Login test failed");
				}
			}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage());
			}
			
			//Close site
			driver.close();	
		}
		
		sh.autoSizeColumn(nouc);
		
		//Save data back to excel
		FileOutputStream fo=new FileOutputStream(f);
		wb.write(fo);
		fi.close();
		fo.close();
		wb.close();
	}
}
