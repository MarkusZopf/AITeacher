package de.markuszopf.aiteacher.model;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.markuszopf.aiteacher.Statics;

public class Questions {

	public static ArrayList<Question> questions = new ArrayList<Question>();

	static {
		String questionTexts;
		try {
			questionTexts = Statics.getTextFileContent("webpage/files/NDMC_small.csv");
			String[] lines = questionTexts.split("\n");

			for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
				String line = lines[lineIndex];
				String[] fields = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

				String questionID = fields[0];
				int answerIndex = getAnswerIndex(fields[3]);
				if (answerIndex == -1) {
					System.out.println("Skipping question '" + line + "'.");
					continue;
				}
				int schoolGrade = Integer.parseInt(fields[7]);
				ArrayList<String> questionSplit = parseQuestion(fields[9].replaceAll("\"", ""));
				if (questionSplit == null) {
					System.out.println("Skipping question '" + line + "'.");
					continue;
				}
				String questionStem = questionSplit.get(0);
				List<String> answers = questionSplit.subList(1, 5);

				Question question = new Question(questionID, answerIndex, schoolGrade, questionStem, answers);
				questions.add(question);
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int getAnswerIndex(String answerKey) {
		if (answerKey.equals("A") || answerKey.equals("1"))
			return 0;
		else if (answerKey.equals("B") || answerKey.equals("2"))
			return 1;
		else if (answerKey.equals("C") || answerKey.equals("3"))
			return 2;
		else if (answerKey.equals("D") || answerKey.equals("4"))
			return 3;
		else
			return -1;
	}

	private static ArrayList<String> parseQuestion(String questionString) {
		try {
			String[] split = null;
			if (questionString.contains("(A)") && questionString.contains("(B)") && questionString.contains("(C)") && questionString.contains("(D)")) {
				split = questionString.split("\\([A-D]\\)");
			}
			else if (questionString.contains("(1)") && questionString.contains("(2)") && questionString.contains("(3)") && questionString.contains("(4)")) {
				split = questionString.split("\\([1-4]\\)");
			}

			ArrayList<String> result = new ArrayList<String>();
			result.add(split[0].trim());
			result.add(split[1].trim());
			result.add(split[2].trim());
			result.add(split[3].trim());
			result.add(split[4].trim());
			return result;
		}

		catch (Exception e) {
			return null;
		}
	}
}
