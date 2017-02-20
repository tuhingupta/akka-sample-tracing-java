package io.tuhin.java.tracing;

import akka.actor.AbstractLoggingActor;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

public class CustomLoggingActor extends AbstractLoggingActor{
	
	@Override
	public void aroundReceive(PartialFunction<Object, BoxedUnit> receive, Object msg) {
		
		try{
		super.aroundReceive(receive, msg);
		
		}catch(Exception ex){
			log().info("Error");
			//context().dispatcher().reportFailure(ex);
		}
	}

	

}
