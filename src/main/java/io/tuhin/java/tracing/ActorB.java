package io.tuhin.java.tracing;

import java.io.Serializable;
import java.util.Map;

import com.github.levkhomich.akka.tracing.TracingExtension;
import com.github.levkhomich.akka.tracing.TracingExtensionImpl;
import com.github.levkhomich.akka.tracing.japi.TracingSupport;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.tuhin.java.tracing.ActorA.RequestA;

/**
 * <h1>ActorB</h1> This is an Akka actor that would receive messages from ActorA
 * and work on them. Then it will <code>forward</code> the request to
 * <code>ExternalCallActor</code>. <code>TracingExtensionImpl</code> is used to
 * create the instance object <code>trace</code>
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
public class ActorB extends AbstractLoggingActor {

	final static String CLASSNAME = ActorB.class.getSimpleName();

	TracingExtensionImpl trace = (TracingExtensionImpl) TracingExtension.apply(context().system());

	ActorRef externalCallActor = createExternalCallActor();

	public ActorB() {

		// akka behavior implementation
		receive(ReceiveBuilder.
				match(ActorA.RequestA.class, reqA -> sendB(reqA)).
				build());
	}

	/**
	 * @param reqA
	 */
	private void sendB(RequestA reqA) {

		log().info(CLASSNAME + " => " + reqA.getPayload() + " Path: " + self().path());

		// update the payload of RequestA
		StringBuffer sb = new StringBuffer().append(CLASSNAME + " " + reqA.getPayload());
		RequestB reqB = new RequestB(reqA.getHeaders(), sb.toString());

		trace.sample(reqB, CLASSNAME, true);
		trace.record(reqB, CLASSNAME);
		trace.createChild(reqB, reqA);
		trace.start(reqB, CLASSNAME);

		// simple try-catch to demonstrate heavy processing done by this actor.
		try {
			Thread.sleep(100);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		externalCallActor.forward(reqB, getContext());

		trace.finish(reqB);

	}

	/**
	 * @return
	 */
	private ActorRef createExternalCallActor() {

		return context().actorOf(Props.create(ExternalCallActor.class), "ExternalCallActor");
	}

	// akka state
	/**
	 * @author Tuhin Gupta
	 *
	 */
	public static final class RequestB extends TracingSupport implements Serializable {

		private final Map<String, Integer> headers;
		private final String payload;

		public Map<String, Integer> getHeaders() {
			return headers;
		}

		public String getPayload() {
			return payload;
		}

		public RequestB(Map<String, Integer> headers, String payload) {
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
			RequestB other = (RequestB) obj;
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
