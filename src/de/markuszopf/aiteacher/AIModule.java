package de.markuszopf.aiteacher;

import java.util.ArrayList;
import java.util.Random;

import de.markuszopf.aiteacher.model.KeyValuePair;
import de.markuszopf.aiteacher.model.NextQuestionData;
import de.markuszopf.aiteacher.resources.Database;

public class AIModule {

	private Random random = new Random(0);

	public NextQuestionData getNextQuestion(String username, double sessionCAQRate) {
		double optimalCAQRate = Database.getOptimalCAQRate(username);

		ArrayList<KeyValuePair<Integer>> questionProbabilities = Database.getQuestionProbabilities(username);

		NextQuestionData nextQuestionData = new NextQuestionData();
		if (!Double.isNaN(sessionCAQRate)) {
			int[] questionIndexes;
			if (sessionCAQRate >= 1 - (1 - optimalCAQRate) / 2.0) { // the learner is far too good, we want to ask a very difficult question
				questionIndexes = questionProbabilities.stream().filter(q -> q.value < 0.25).mapToInt(q -> q.key).toArray();
			}

			else if (sessionCAQRate > optimalCAQRate) { // the learner is too good, we want to ask a difficult question
				questionIndexes = questionProbabilities.stream().filter(q -> q.value >= 0.25 && q.value < 0.50).mapToInt(q -> q.key).toArray();
			}

			else if (sessionCAQRate > optimalCAQRate / 2.0) { // the learner is too bad, we want to ask a easy question
				questionIndexes = questionProbabilities.stream().filter(q -> q.value >= 0.50 && q.value < 0.75).mapToInt(q -> q.key).toArray();
			}

			else { // the learner is way too bad, we want to ask a very easy question
				questionIndexes = questionProbabilities.stream().filter(q -> q.value >= 0.75).mapToInt(q -> q.key).toArray();
			}

			if (questionIndexes.length > 0) {
				nextQuestionData.questionIndex = questionIndexes[random.nextInt(questionIndexes.length)];
			}

			else {
				nextQuestionData.questionIndex = random.nextInt(questionProbabilities.size()); // if no suitable question was found, return a random question
			}
		}

		else {
			nextQuestionData.questionIndex = random.nextInt(questionProbabilities.size()); // if session streak is NaN, return a random question
		}

		nextQuestionData.optimalCAQRate = optimalCAQRate;
		nextQuestionData.correctAnswerProbability = questionProbabilities.get(nextQuestionData.questionIndex).value;
		nextQuestionData.questionProbabilities = questionProbabilities;

		nextQuestionData.correctlyAnsweredLastTime = Database.questionAnsweredCorrectlyLastTime(username, nextQuestionData.questionIndex);

		return nextQuestionData;
	}
}
