package de.markuszopf.aiteacher.model;
import java.util.List;

public class Question {
	public String questionID;
	public int answerIndex;
	public int schoolGrade;
	public String questionStem;
	public List<String> answers;

	public Question(String questionID, int answerIndex, int schoolGrade, String questionStem, List<String> answers) {
		super();
		this.questionID = questionID;
		this.answerIndex = answerIndex;
		this.schoolGrade = schoolGrade;
		this.questionStem = questionStem;
		this.answers = answers;
	}
}
