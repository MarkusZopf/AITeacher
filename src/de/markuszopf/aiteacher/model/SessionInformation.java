package de.markuszopf.aiteacher.model;

import de.markuszopf.aiteacher.AIModule;

public class SessionInformation {
	public String username;
	public int streakLength = 0;
	public int correcAnswers = 0;
	public int wrongAnswers = 0;
	public AIModule aiModule = new AIModule();
	public NextQuestionData nextQuestionData;
}
