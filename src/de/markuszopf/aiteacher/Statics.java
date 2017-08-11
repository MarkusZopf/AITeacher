package de.markuszopf.aiteacher;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Statics {
	public static String getTextFileContent(String filename) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), "UTF-8"));
		String line;
		boolean firstLine = true;
		while ((line = bufferedReader.readLine()) != null) {
			if (!firstLine) {
				stringBuilder.append("\n");
			}
			else {
				firstLine = false;
			}

			stringBuilder.append(line);
		}

		bufferedReader.close();

		return stringBuilder.toString();
	}
}
