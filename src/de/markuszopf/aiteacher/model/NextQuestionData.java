package de.markuszopf.aiteacher.model;
import java.util.ArrayList;

public class NextQuestionData {
	public int questionIndex;
	public Boolean correctlyAnsweredLastTime;
	public double correctAnswerProbability;
	public double optimalCAQRate;
	public ArrayList<KeyValuePair<Integer>> questionProbabilities;
}
