package io.tuhin.java.tracing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.github.levkhomich.akka.tracing.TracingExtension;
import com.github.levkhomich.akka.tracing.TracingExtensionImpl;
import com.github.levkhomich.akka.tracing.japi.TracingSupport;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

/**
 * <h1>ActorA</h1> This is an Akka actor that would create 50 requests in a loop
 * and pass them to ActoB for processing. Once the request processing is done,
 * its <code>receive</code> method will receive the response back.
 * <code>TracingExtensionImpl</code> is used to create the instance object
 * <code>trace</code>
 * 
 * <code>trace</code> is used to start the tracing and sample the requests.
 * Zipkin's core data structures that provide tracing are mentioned here
 * <link>http://zipkin.io/pages/instrumenting.html</link>
 * <code>TracingExtensionImpl</code> provides implementations for these data
 * structures.
 * 
 * @author Tuhin Gupta
 * @since 2017-02-19
 * 
 */
public class ActorA extends AbstractLoggingActor {

	final static String CLASSNAME = ActorA.class.getSimpleName();

	TracingExtensionImpl trace = (TracingExtensionImpl) TracingExtension.apply(context().system());
	private ActorRef actorB = createActorB();
	Map<String, Integer> headers = new HashMap<String, Integer>();

	/**
	* 
	*/
	public ActorA() {

		log().info(CLASSNAME + " initiates messages...");

		// actor A generates 50 messages
		int i = 0;
		StringBuffer sb = new StringBuffer().append(CLASSNAME);
		headers.put("id", new Integer(i));

		while (++i < 50) {

			sb.append(" message: " + i);
			RequestA reqA = new RequestA(headers, sb.toString());

			trace.sample(reqA, CLASSNAME, true);
			trace.start(reqA, CLASSNAME);

			actorB.tell(reqA, self());

			trace.finish(reqA);

		} // while

		// akka behavior implementation
		receive(ReceiveBuilder.
				match(ExternalCallActor.APIResponse.class, resp -> processResponse(resp))
				.build());

	}

	/**
	 * @param e
	 */
	private void handleExp(Exception e) {
		e.printStackTrace();
	}

	/**
	 * @param response
	 */
	public void processResponse(ExternalCallActor.APIResponse response) {

		trace.sample(response, CLASSNAME, true);

		log().info(CLASSNAME + " => " + response.getPayload().toString() + " Path " + self().path());
	}

	private ActorRef createActorB() {

		return getContext().actorOf(Props.create(ActorB.class), "actorB");
	}

	// akka state - storage
	/**
	 * @author Tuhin Gupta
	 *
	 */
	public static final class RequestA extends TracingSupport implements Serializable {

		private final Map<String, Integer> headers;
		private final String payload;

		public Map<String, Integer> getHeaders() {
			return headers;
		}

		public String getPayload() {
			return payload;
		}

		public RequestA(Map<String, Integer> headers, String payload) {
			this.headers = headers;
			this.payload = payload;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((headers == null) ? 0 : headers.hashCode());
			result = prime * result + ((payload == null) ? 0 : payload.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RequestA other = (RequestA) obj;
			if (headers == null) {
				if (other.headers != null)
					return false;
			} else if (!headers.equals(other.headers))
				return false;
			if (payload == null) {
				if (other.payload != null)
					return false;
			} else if (!payload.equals(other.payload))
				return false;
			return true;
		}

	}
}
