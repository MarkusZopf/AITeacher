package de.markuszopf.aiteacher.resources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

import de.markuszopf.aiteacher.model.KeyValuePair;
import de.markuszopf.aiteacher.model.Questions;

public class Database {

	private static String SERVER_ADDRESS = "ADD YOUR SERVER ADDRESS HERE";
	private static String DATABASE_NAME = "ADD YOUR DATABASE NAME HERE";
	private static String USERNAME = "ADD YOUR USERNAME HERE";
	private static String PASSWORD = "ADD YOUR PASSWORD HERE";

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://" + SERVER_ADDRESS + "/" + DATABASE_NAME + "?" + "user=" + USERNAME + "&password=" + PASSWORD);
	}

	public static void insertUser(String username, String password, double sessionCAQRate, double optimalCAQRate) {
		try {
			Connection connection = getConnection();
			PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO users (username, password, questionHistory, optimalCAQRate) VALUES (?, ?, ?, ?)");

			insertStatement.setString(1, username);
			insertStatement.setString(2, password);
			insertStatement.setString(3, new String(new char[Questions.questions.size()]).replace('\0', '?'));
			insertStatement.setDouble(4, optimalCAQRate);

			insertStatement.execute();
			insertStatement.close();
			connection.close();
		}

		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertObservation(String username, int questionAsked, boolean result) {
		try {
			String questionHistory = getCurrentQuestionHistory(username);

			Connection connection = getConnection();

			// insert the new observation:
			PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO observations (questionHistory, questionAsked, result) VALUES (?, ?, ?);");
			insertStatement.setString(1, questionHistory);
			insertStatement.setInt(2, questionAsked);
			insertStatement.setBoolean(3, result);
			insertStatement.execute();
			insertStatement.close();

			// update the question history of the user:
			PreparedStatement updateStatement = connection.prepareStatement("UPDATE users SET questionHistory = ? WHERE username = ?;");
			questionHistory = questionHistory.substring(0, questionAsked) + (result ? "+" : "-") + questionHistory.substring(questionAsked + 1);
			updateStatement.setString(1, questionHistory);
			updateStatement.setString(2, username);
			updateStatement.execute();
			updateStatement.close();

			connection.close();
		}

		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Boolean questionAnsweredCorrectlyLastTime(String username, int questionIndex) {
		try {
			Connection connection = getConnection();
			PreparedStatement selectStatement = connection.prepareStatement("SELECT questionHistory FROM users WHERE username = '" + username + "';");
			ResultSet resultSet = selectStatement.executeQuery();
			resultSet.next();
			String questionHistory = resultSet.getString("questionHistory");

			if (questionHistory.charAt(questionIndex) == '+') {
				return true;
			}

			else if (questionHistory.charAt(questionIndex) == '-') {
				return false;
			}

			else {
				return null;
			}
		}

		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void insertFakeObservation(String questionHistory, int questionAsked, boolean result) {
		try {
			Connection connection = getConnection();
			PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO observations (questionHistory, questionAsked, result) VALUES (?, ?, ?);");
			insertStatement.setString(1, questionHistory);
			insertStatement.setInt(2, questionAsked);
			insertStatement.setBoolean(3, result);
			insertStatement.execute();
			insertStatement.close();
			connection.close();
		}

		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static String getCurrentQuestionHistory(String username) {
		try {
			Connection connection = getConnection();
			PreparedStatement selectStatement = connection.prepareStatement("SELECT questionHistory FROM users WHERE username = '" + username + "';");
			ResultSet resultSet = selectStatement.executeQuery();
			resultSet.next();
			String questionHistory = resultSet.getString("questionHistory");
			selectStatement.close();
			connection.close();
			return questionHistory;
		}

		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getHighscore(String username) {
		try {
			Connection connection = getConnection();
			PreparedStatement selectStatement = connection.prepareStatement("SELECT highscore FROM users WHERE username = '" + username + "';");
			ResultSet resultSet = selectStatement.executeQuery();
			resultSet.next();
			int highscore = resultSet.getInt("highscore");
			selectStatement.close();
			connection.close();
			return highscore;
		}

		catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static void updateHighscore(String username, int highscore) {
		try {
			Connection connection = getConnection();
			PreparedStatement updateStatement = connection.prepareStatement("UPDATE users SET highscore = ? WHERE username = ?;");
			updateStatement.setInt(1, highscore);
			updateStatement.setString(2, username);
			updateStatement.execute();
			updateStatement.close();

			connection.close();
		}

		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updateOptimalCAQRate(String username, double newOptimalCAQRate) {
		try {
			Connection connection = getConnection();
			PreparedStatement updateStatement = connection.prepareStatement("UPDATE users SET optimalCAQRate = ? WHERE username = ?;");
			updateStatement.setDouble(1, newOptimalCAQRate);
			updateStatement.setString(2, username);
			updateStatement.execute();
			updateStatement.close();

			connection.close();
		}

		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Double getOptimalCAQRate(String username) {
		try {
			Connection connection = getConnection();
			PreparedStatement selectStatement = connection.prepareStatement("SELECT optimalCAQRate FROM users WHERE username = '" + username + "';");
			ResultSet resultSet = selectStatement.executeQuery();
			resultSet.next();
			Double optimalCAQRate = resultSet.getDouble("optimalCAQRate");
			selectStatement.close();
			connection.close();
			return optimalCAQRate;
		}

		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ArrayList<KeyValuePair<Integer>> getQuestionProbabilities(String username) {
		try {
			String questionHistory = getCurrentQuestionHistory(username);

			Connection connection = getConnection();
			PreparedStatement selectStatement = connection.prepareStatement("SELECT *, levenshtein(?, questionHistory) AS distance FROM observations");
			selectStatement.setString(1, questionHistory);
			ResultSet resultSet = selectStatement.executeQuery();

			ArrayList<Integer> correctAnswers = new ArrayList<Integer>(Collections.nCopies(Questions.questions.size(), 0));
			ArrayList<Integer> questionAsked = new ArrayList<Integer>(Collections.nCopies(Questions.questions.size(), 0));
			while (resultSet.next()) {
				if (resultSet.getInt("distance") == 0) {
					int questionIndex = resultSet.getInt("questionAsked");
					questionAsked.set(questionIndex, questionAsked.get(questionIndex) + 1);
					if (resultSet.getBoolean("result")) {
						correctAnswers.set(questionIndex, correctAnswers.get(questionIndex) + 1);
					}
				}
			}

			ArrayList<KeyValuePair<Integer>> questionProbabilities = new ArrayList<KeyValuePair<Integer>>();
			IntStream.range(0, correctAnswers.size()).forEach(i -> questionProbabilities.add(new KeyValuePair<Integer>(i, correctAnswers.get(i) / (double) questionAsked.get(i))));

			connection.close();
			return questionProbabilities;
		}

		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean login(String username, String password) {
		try {
			Connection connection = getConnection();
			PreparedStatement selectStatement = connection.prepareStatement("SELECT password FROM users WHERE username = '" + username + "';");
			ResultSet resultSet = selectStatement.executeQuery();

			if (resultSet.next()) {
				return resultSet.getString("password").equals(password);
			}

			else { // add new user to database
				insertUser(username, password, 0.5, 0.5);
				return true;
			}
		}

		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void clearDatabase() {
		try {
			Connection connection = getConnection();
			connection.prepareStatement("TRUNCATE users;").execute();
			connection.prepareStatement("TRUNCATE observations;").execute();
			connection.close();
		}

		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updateLastQuestionAnsweredCorrectly(String username, Integer lastQuestionAnsweredCorrectly) {
		try {
			Connection connection = getConnection();
			PreparedStatement updateStatement = connection.prepareStatement("UPDATE users SET lastQuestionAnsweredCorrectly = ? WHERE username = ?;");
			updateStatement.setInt(1, lastQuestionAnsweredCorrectly);
			updateStatement.setString(2, username);
			updateStatement.execute();
			updateStatement.close();

			connection.close();
		}

		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Integer getLastQuestionAnsweredCorrectly(String username) {
		try {
			Connection connection = getConnection();
			PreparedStatement selectStatement = connection.prepareStatement("SELECT lastQuestionAnsweredCorrectly FROM users WHERE username = '" + username + "';");
			ResultSet resultSet = selectStatement.executeQuery();
			resultSet.next();
			Integer lastQuestionAnsweredCorrectly = resultSet.getInt("lastQuestionAnsweredCorrectly");
			selectStatement.close();
			connection.close();
			return lastQuestionAnsweredCorrectly;
		}

		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
