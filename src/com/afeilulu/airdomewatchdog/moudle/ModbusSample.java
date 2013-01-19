package com.afeilulu.airdomewatchdog.moudle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;

import com.afeilulu.airdomewatchdog.io.Analog;
import com.afeilulu.airdomewatchdog.io.Data;
import com.afeilulu.airdomewatchdog.io.Digital;

/**
 * read modbus reference,and generate xml file
 * @author chen
 *
 */
public class ModbusSample {
		
	public static Data Read(String id, String password){
		// this a is fake data
		// Realistic data will be read from a modbus client
		
		Data data = new Data();
		data.setId(id);
		data.setPassword(password);
		
		List<Digital> digitals = new ArrayList<Digital>();
		for (int i = 0; i < 3; i++) {
			Digital digital = new Digital();
			digital.setId(String.valueOf(i));
			digital.setValue(String.valueOf(i + 1000));
			digitals.add(digital);
		}
		data.setDigitals(digitals);
		
		List<Analog> analogs = new ArrayList<Analog>();
		for (int i = 0; i < 3; i++) {
			Analog analog = new Analog();
			analog.setId(String.valueOf(i));
			analog.setValue(String.valueOf(i+2000));
		}
		data.setAnalogs(analogs);
		
		return data;
	}
	
	public static void GenerateXml(String id, String password, FileOutputStream fOut){
		Data data = Read(id,password);
		
		final String xmlString = writeXml(data);
        
        // ##### Write a file to the disk #####
        /* We have to use the openFileOutput()-method
         * the ActivityContext provides, to
         * protect your file from others and
         * This is done for security-reasons.
         * We chose MODE_WORLD_READABLE, because
         *  we have nothing to hide in our file */            
//        FileOutputStream fOut = openFileOutput("upload.xml", MODE_WORLD_READABLE);
        OutputStreamWriter osw = new OutputStreamWriter(fOut); 

        try {
			osw.write(xmlString);
			osw.flush();
			osw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}

	private static String writeXml(Data data){
	    XmlSerializer serializer = Xml.newSerializer();
	    StringWriter writer = new StringWriter();
	    try {
	        serializer.setOutput(writer);
	        serializer.startDocument("UTF-8", true);
	        serializer.startTag("", "data");
	        serializer.attribute("", "ID", data.getId());
	        serializer.attribute("", "Password", data.getPassword());
	        for (Digital digital:data.getDigitals()){
	        	serializer.startTag("", "Digital");
	            serializer.attribute("", "ID", digital.getId());
	            serializer.text(digital.getValue());
	            serializer.endTag("", "Digital");
	        }
	        for (Analog analog:data.getAnalogs()){
	        	serializer.startTag("", "Analog");
	            serializer.attribute("", "ID", analog.getId());
	            serializer.text(analog.getValue());
	            serializer.endTag("", "Analog");
	        }
	        serializer.endTag("", "data");
	        serializer.endDocument();
	        return writer.toString();
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    } 
	}
}
