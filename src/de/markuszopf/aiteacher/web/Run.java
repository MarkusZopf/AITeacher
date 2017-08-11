package de.markuszopf.aiteacher.web;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import de.markuszopf.aiteacher.resources.Database;

public class Run {

	public static void main(String[] args) throws Exception {
		fillDatabase();

		Server server = new Server(8080);

		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setResourceBase("./webpage");

		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContextHandler.addServlet(new ServletHolder(new AITeacherServlet()), "/AITeacher");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceHandler, servletContextHandler });

		server.setHandler(handlers);
		server.start();
		server.join();
	}

	private static void fillDatabase() {
		Database.clearDatabase();
		Database.insertUser("Alice", "alice", 0.5, 0.5);
		Database.insertUser("Bob", "bob", 0.5, 0.5);
		Database.insertUser("Charlie", "charlie", 0.5, 0.5);
		Database.insertUser("Denise", "denise", 0.5, 0.5);

		Database.insertObservation("Alice", 0, true);
		Database.insertObservation("Bob", 2, false);
		Database.insertObservation("Charlie", 1, false);
		Database.insertObservation("Denise", 2, true);

		Database.insertFakeObservation("??-?", 1, true);
		Database.insertFakeObservation("??-?", 1, true);
		Database.insertFakeObservation("??-?", 1, true);
		Database.insertFakeObservation("??-?", 1, false);

		Database.insertFakeObservation("??-?", 0, true);
		Database.insertFakeObservation("??-?", 0, false);

		Database.insertFakeObservation("??-?", 3, true);
		Database.insertFakeObservation("??-?", 3, false);
		Database.insertFakeObservation("??-?", 3, false);

		Database.insertFakeObservation("??-?", 2, true);
		Database.insertFakeObservation("??-?", 2, false);
		Database.insertFakeObservation("??-?", 2, false);
		Database.insertFakeObservation("??-?", 2, false);

		Database.insertUser("markus", "123", 0.5, 0.5);

		System.out.println("Database filled!");
	}
}
