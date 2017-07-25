package com.ngray.option.ig.refdata;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OptionReferenceDataWriter {
	
	public static void writeToFile(String filename, List<OptionReferenceData> refDataList) throws IOException {
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			writer.write("OptionEpic,UnderlyingEpic,Strike,Expiry,CallOrPut,DividendYield,RiskFreeRate\n");
			for (OptionReferenceData refData : refDataList) {
				String thisEntry = refData.getOptionName() + ",";
				thisEntry += refData.getUnderlyingEpic() + ",";
				thisEntry += refData.getStrike() + ",";
				thisEntry += refData.getExpiryDate() + ",";
				thisEntry += refData.getCallOrPut().toString() + ",";
				thisEntry += refData.getDividendYield() + ",";
				thisEntry += refData.getRiskFreeRate() + "\n";
				writer.write(thisEntry);
			}
		}
	}
	
	public static void writeToResource(String resourceName, List<OptionReferenceData> refDataList) throws IOException {
		String filename = "./src/main/resources/" + resourceName;
		writeToFile(filename, refDataList);	
	}

}
