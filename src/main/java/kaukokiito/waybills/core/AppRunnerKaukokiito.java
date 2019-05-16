package kaukokiito.waybills.core;

import IO.IOController;
import IO.helper.ConsoleInput;
import IO.helper.FileManager;
import controller.PdfController;
import kaukokiito.waybills.model.Waybill;
import kaukokiito.waybills.util.DeliveryStatus;
import org.apache.pdfbox.pdmodel.PDDocument;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AppRunnerKaukokiito{
	private static IOController io;
	private static ConsoleInput ci;
	private static SeleniumControllerKaukokiito sCon;
	private String lastHandled;
	private ArrayList<String> secret;
	
	private ArrayList<Waybill> waybills;
	
	private void removeDuplicates(){
		System.out.println("Removing duplicates.");
		for(int i = waybills.size()-1; i >= 0 ; i--){
			Waybill current = waybills.get(i);
			for(int j = i-1; j >= 0; j--){
				if(current.getBarcode().equalsIgnoreCase(waybills.get(j).getBarcode())){
					System.out.println("Removing: "+ current);
					try{
						current.getPDF().close();
					} catch(IOException e){
						System.out.println("Couldn't close PDF:\n"+e.getMessage());
					}
					waybills.remove(current);
					j=-1;
				}
			}
		}
	}
	
	private void mergeCropAndSaveInvalid(){
		PDDocument invalidBills = new PDDocument();
		for(Waybill b: waybills){
			int x = b.getFileName().compareTo(lastHandled);
			//System.out.println(b.getFileName() + " vs " + io.getLastHandled() + " = " + x);
			if(b.getStatus() == DeliveryStatus.INVALID && x>0){
				System.out.println("invalid!! \n"+b);
				invalidBills.addPage(b.getPDF().getPage(0));
			}
		}
		PdfController.savePDF(invalidBills, "invalid");
	}
	
	private void enterInvalidCodes(){
		System.out.println("Enter barcodes for invalid Waybills");
		for(Waybill w: waybills){
			if(w.getStatus()== DeliveryStatus.INVALID){
				w.setBarcode(ci.readString(w.getFileName()+" >"));
			}
		}
	}
	
	private void saveNotDeliveredPDF(){
		PDDocument notDelivered = new PDDocument();
		ArrayList<String> ndcodes = new ArrayList<>();
		for(Waybill w: waybills){
			if(w.getStatus()!= DeliveryStatus.DELIVERED){
				ndcodes.add(w.getBarcode());
				notDelivered.addPage(w.getPDF().getPage(0));
			}
		}
		FileManager.saveFile(ndcodes.toArray(new String[ndcodes.size()]), "notDelivered.txt", "txt");
		PdfController.uncropPDF(notDelivered);
		PdfController.savePDF(notDelivered, "Not_Delivered");
	}
	
	private void getCSVData(){
		ArrayList<String> csvRows = io.loadTxt(new File("csv/barcodes.CSV"));
		for(String row: csvRows){
			String[] splitRow = row.split(",");
			String barcode = splitRow[16].substring(1,splitRow[16].length()-1);
			String name = splitRow[9].substring(1,splitRow[9].length()-1);
			if(lastHandled.compareTo(name)<0 && lastHandled.length() <= name.length()){
				waybills.add(new Waybill(name, barcode));
			}
		}
	}
	
	private void loadPDFs(){
		ArrayList<String> waybillNames = new ArrayList<>();
		File[] bills = FileManager.listFiles("pdf", "waybills", false);
		for(Waybill b : waybills){
			//b.setPDF(pConK.loadWaybill(b.getFileName());
			for(File f: bills){
				if(f.getName().equalsIgnoreCase(b.getFileName())){
					try{
						b.loadPDF(f);
					}catch(IOException e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void load(){
		getCSVData();
		for(Waybill b : waybills){
			if(b.getBarcode().isEmpty())
				b.setDeliveryStatus(DeliveryStatus.INVALID);
		}
		loadPDFs();
		mergeCropAndSaveInvalid();
		enterInvalidCodes();
		removeDuplicates();
		
		sCon = new SeleniumControllerKaukokiito(io);
		System.out.printf("%-15s|%15s\n","Waybill Number","  Delivered Status");
		System.out.println("----------------------------------");
		for(Waybill b: waybills){
			b.setDeliveryStatus(sCon.doCheck(b.getBarcode()));
			//System.out.println(b);
			int x = b.getFileName().compareTo(lastHandled);
			//System.out.println(b+"\n"+b.getFileName()+" vs "+lastBillHandled+" = "+ x);
			if(x > 0 && b.getFileName().length()<=lastHandled.length()){
				lastHandled=b.getFileName();
			}
		}
		saveNotDeliveredPDF();
		FileManager.saveFile(lastHandled,lastHandled);
	}
	
	public AppRunnerKaukokiito(){
		io = new IOController();
		ci = new ConsoleInput();
		lastHandled = io.loadTxt(new File("lastHandled.txt")).get(0);
		File[] dirs = {new File("csv") ,
				new File("txt"),
				new File("waybills"),
				new File("pdfOut")};
		io.createRequiredDirectories(dirs);
		//secret = io.loadTxt(io.loadResource());
		waybills = new ArrayList<>();
		load();
		sCon.stopDriver();
	}
}
