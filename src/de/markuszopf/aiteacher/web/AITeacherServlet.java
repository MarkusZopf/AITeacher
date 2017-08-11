package de.markuszopf.aiteacher.web;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.markuszopf.aiteacher.Statics;
import de.markuszopf.aiteacher.model.KeyValuePair;
import de.markuszopf.aiteacher.model.NextQuestionData;
import de.markuszopf.aiteacher.model.Question;
import de.markuszopf.aiteacher.model.Questions;
import de.markuszopf.aiteacher.model.SessionInformation;
import de.markuszopf.aiteacher.resources.Database;
import de.markuszopf.aiteacher.resources.ImageSearch;

public class AITeacherServlet extends HttpServlet {
	private static final long serialVersionUID = -5982178805895235990L;

	private HashMap<String, SessionInformation> sessions = new HashMap<String, SessionInformation>();

	private static String loginPageTemplate;
	private static String pageTemplate;
	private static String multipleChoiceTemplate;
	private static String resultTemplate;

	static {
		try {
			loginPageTemplate = Statics.getTextFileContent("webpage/login_page.html");
			pageTemplate = Statics.getTextFileContent("webpage/page.html");
			multipleChoiceTemplate = Statics.getTextFileContent("webpage/multiple_choice.html");
			resultTemplate = Statics.getTextFileContent("webpage/result.html");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(loginPageTemplate);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		sessions.putIfAbsent(request.getSession().getId(), new SessionInformation());
		SessionInformation sessionInformation = sessions.get(request.getSession().getId());

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		if (request.getParameter("login") != null) {
			if (request.getParameter("username").length() == 0 || request.getParameter("password").length() == 0) { // invalid input
				response.getWriter().println(loginPageTemplate);
				return;
			}

			else {
				// login:
				boolean loginResult = Database.login(request.getParameter("username"), request.getParameter("password"));

				if (!loginResult) { // wrong password
					response.getWriter().println(loginPageTemplate);
					return;
				}

				else { // successfully logged in
					sessionInformation.username = request.getParameter("username");
					double optimalCAQRate = Database.getOptimalCAQRate(sessionInformation.username);
					int lastQuestionAnsweredCorrectly = Database.getLastQuestionAnsweredCorrectly(sessionInformation.username);

					if (lastQuestionAnsweredCorrectly == 0) {
						Database.updateOptimalCAQRate(sessionInformation.username, optimalCAQRate + (1 - optimalCAQRate) * 0.1);
					}

					else if (lastQuestionAnsweredCorrectly == 1) {
						Database.updateOptimalCAQRate(sessionInformation.username, optimalCAQRate - (optimalCAQRate * 0.1));
					}

					sessionInformation.nextQuestionData = sessionInformation.aiModule.getNextQuestion(sessionInformation.username, sessionInformation.correcAnswers / (double) (sessionInformation.correcAnswers + sessionInformation.wrongAnswers));
					response.getWriter().println(getQuestionPage(sessionInformation.nextQuestionData, multipleChoiceTemplate, sessionInformation));
				}
			}
		}

		else {
			if (request.getParameter("next_question") != null) { // show next question
				sessionInformation.nextQuestionData = sessionInformation.aiModule.getNextQuestion(sessionInformation.username, sessionInformation.correcAnswers / (double) (sessionInformation.correcAnswers + sessionInformation.wrongAnswers));
				response.getWriter().println(getQuestionPage(sessionInformation.nextQuestionData, multipleChoiceTemplate, sessionInformation));
			}

			else { // show result
				int givenAnswerIndex = Integer.parseInt(request.getParameter("answer"));
				boolean result = Questions.questions.get(sessionInformation.nextQuestionData.questionIndex).answerIndex == givenAnswerIndex;
				Database.updateLastQuestionAnsweredCorrectly(sessionInformation.username, result ? 1 : 0);
				Database.insertObservation(sessionInformation.username, sessionInformation.nextQuestionData.questionIndex, result);

				String page = getQuestionPage(sessionInformation.nextQuestionData, resultTemplate, sessionInformation);
				if (result) {
					sessionInformation.streakLength++;
					sessionInformation.correcAnswers++;
					int highscore = Database.getHighscore(sessionInformation.username);
					if (sessionInformation.streakLength > highscore) {
						Database.updateHighscore(sessionInformation.username, sessionInformation.streakLength);
					}

					page = page.replace("${result}", "<span class=\"heading1\">Correct!</span>");
				}
				else {
					sessionInformation.streakLength = 0;
					sessionInformation.wrongAnswers++;

					page = page.replace("${result}", "<span class=\"heading1\">Wrong :(</span>");
				}

				response.getWriter().println(page);
			}
		}
	}

	public static final DecimalFormat decimalFormatShort = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));

	private String getQuestionPage(NextQuestionData nextQuestionData, String template, SessionInformation sessionInformation) {
		Question question = Questions.questions.get(nextQuestionData.questionIndex);
		String page = pageTemplate.replace("${question}", question.questionStem);

		String multipleChoice = template.replace("${answerA}", question.answers.get(0));
		multipleChoice = multipleChoice.replace("${answerB}", question.answers.get(1));
		multipleChoice = multipleChoice.replace("${answerC}", question.answers.get(2));
		multipleChoice = multipleChoice.replace("${answerD}", question.answers.get(3));
		page = page.replace("${answers}", multipleChoice);

		if (nextQuestionData.correctlyAnsweredLastTime == null || sessionInformation.nextQuestionData.correctlyAnsweredLastTime) {
			page = page.replace("${hint}", "");
		}

		else { // make question easier by giving an image as a hint
			String imageURL = ImageSearch.getImageURL(question.answers.get(question.answerIndex));
			page = page.replace("${hint}", "<div><span class=\"heading1\">Hint:</span> </br></br> <img style=\"box-shadow: 0 0 10px #FFFFFF; -webkit-box-shadow: 0 0 10px #FFFFFF\" src=\"" + imageURL + "\" height=\"200\" width=\"200\"></div>");
		}

		// print current streak / highscore:
		int highscore = Database.getHighscore(sessionInformation.username);
		page = page.replace("${highscore}", Integer.toString(highscore));
		page = page.replace("${currentStreak}", Integer.toString(sessionInformation.streakLength));

		// print achievements:
		if (highscore > 0) {
			page = page.replace("${achievement1}", "<img src=\"files/trophy.png\" height=\"50\" width=\"50\" title=\"Get the party started: 1 correctly answered question\">");
		}

		else {
			page = page.replace("${achievement1}", "<img style=\"filter: gray; -webkit-filter: grayscale(1); filter: grayscale(1);\" src=\"files/trophy.png\" height=\"50\" width=\"50\" title=\"???\">");
		}
		page = page.replace("${achievement2}", "<img style=\"filter: gray; -webkit-filter: grayscale(1); filter: grayscale(1);\" src=\"files/trophy.png\" height=\"50\" width=\"50\" title=\"???\">");
		page = page.replace("${achievement3}", "<img style=\"filter: gray; -webkit-filter: grayscale(1); filter: grayscale(1);\" src=\"files/trophy.png\" height=\"50\" width=\"50\" title=\"???\">");

		// print internals:
		page = page.replace("${questionIndex}", Integer.toString(nextQuestionData.questionIndex));
		if (Double.isNaN(sessionInformation.correcAnswers / (double) (sessionInformation.correcAnswers + sessionInformation.wrongAnswers))) {
			page = page.replace("${sessionCAQrate}", "?.??");
		}
		else {
			page = page.replace("${sessionCAQrate}", decimalFormatShort.format(sessionInformation.correcAnswers / (double) (sessionInformation.correcAnswers + sessionInformation.wrongAnswers)));
		}
		page = page.replace("${optimalCAQrate}", decimalFormatShort.format(nextQuestionData.optimalCAQRate));

		if (Double.isNaN(nextQuestionData.correctAnswerProbability)) {
			page = page.replace("${correctAnswerProbability}", "?.??");
		}
		else {
			page = page.replace("${correctAnswerProbability}", decimalFormatShort.format(nextQuestionData.correctAnswerProbability));
		}

		StringBuilder questionProbabilities = new StringBuilder();
		for (KeyValuePair<Integer> q : nextQuestionData.questionProbabilities) {
			if (Double.isNaN(q.value)) {

				questionProbabilities.append("<tr><td>" + q.key + ": </td><td>?.??</td></tr>");
			}
			else {
				questionProbabilities.append("<tr><td>" + q.key + ": </td><td>" + decimalFormatShort.format(q.value) + "</td></tr>");
			}
		}

		page = page.replace("${questionProbabilities}", questionProbabilities);

		return page;
	}
}
