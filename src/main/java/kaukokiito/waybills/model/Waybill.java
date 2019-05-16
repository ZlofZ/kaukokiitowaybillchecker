package kaukokiito.waybills.model;


import kaukokiito.waybills.util.DeliveryStatus;
import model.PDF;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

public class Waybill extends PDF{
	private String barcode = null;
	private DeliveryStatus status = DeliveryStatus.UNSET;
	
	public  Waybill(String fileName, String barcode){
		super(fileName);
		this.barcode = barcode;
	}
	
	public Waybill(File file, String fileName) throws IOException{
		super(file, fileName);
	}
	public Waybill(File file, String barcode, String fileName) throws IOException{
		super(file, fileName);
		this.barcode = barcode;

	}
	public Waybill(PDDocument file, String barcode, String fileName) throws IOException{
		super(file, fileName);
		this.barcode = barcode;
	}
	
	public void setPDF(PDDocument pdf){
		super.setPDF(pdf);
	}
	public void setBarcode(String barcode){
		this.barcode = barcode;
	}
	public void setDeliveryStatus(DeliveryStatus status){
		this.status = status;
	}
	
	public String getBarcode(){
		return barcode;
	}
	public String getFileName(){
		return super.getName();
	}
	public DeliveryStatus getStatus(){
		return status;
	}
	
	@Override
	public String toString(){
		String name = super.getName();
		String code = barcode;
		if(name.isEmpty()) name = "No Name";
		if(barcode.isEmpty()) code = "No Code";
		return "[" + code + ", " +  name + ", "+ status +", PDF present:" + (super.getPDF()!=null) + "]";
	}
}
