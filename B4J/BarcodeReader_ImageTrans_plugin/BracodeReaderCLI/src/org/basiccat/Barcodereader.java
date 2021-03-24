package org.basiccat;

import java.io.FileWriter;
import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.TextResult;

public class Barcodereader {

	public static void main(String[] args) throws BarcodeReaderException, IOException {
		// TODO Auto-generated method stub
		System.out.println(args.length);
        String filePath = args[0];
        System.out.println(filePath);
        String license = args[1];
        BarcodeReader br = new BarcodeReader(license);
        TextResult[] results = br.decodeFile(filePath, null);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(results);
        FileWriter writer=new FileWriter(filePath+"-out.json");
        writer.write(json);
        writer.close();
	}
}
