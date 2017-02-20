package io.tuhin.java.tracing;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * <h1>Main</h1> This is the main class that runs the project and passes control
 * to ActorA.
 * 
 * @author Tuhin Gupta
 * @since 2017-02-19
 * 
 */
public class Main {

	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("akka-tracing");
		ActorRef actorA = system.actorOf(Props.create(ActorA.class), "ActorA");

	}

}
