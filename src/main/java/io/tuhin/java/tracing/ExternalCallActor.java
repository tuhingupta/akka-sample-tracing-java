package io.tuhin.java.tracing;

import java.io.Serializable;

import com.github.levkhomich.akka.tracing.TracingExtension;
import com.github.levkhomich.akka.tracing.TracingExtensionImpl;
import com.github.levkhomich.akka.tracing.japi.TracingSupport;

import akka.japi.pf.ReceiveBuilder;

/**
 * <h1>ExternalCallActor</h1> This is an Akka actor that would receive messages
 * from ActorB and work on them. Then it will <code>tell</code> the response
 * back to <code>ActorA</code>. <code>TracingExtensionImpl</code> is used to
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
public class ExternalCallActor extends CustomLoggingActor {

	final static String CLASSNAME = ExternalCallActor.class.getSimpleName();

	TracingExtensionImpl trace = (TracingExtensionImpl) TracingExtension.apply(context().system());

	public ExternalCallActor() {

		receive(ReceiveBuilder.match(ActorB.RequestB.class, reqB -> processRequestB(reqB))
				.match(ActorA.RequestA.class, reqA -> processRequestA(reqA)).build());

	}

	/**
	 * @param reqA
	 * @throws Exception
	 */
	private void processRequestA(ActorA.RequestA reqA) throws Exception {

		log().info(CLASSNAME + " => " + reqA.getPayload() + " Path: " + self().path());

		APIResponse resp = new APIResponse(CLASSNAME);

		trace.recordKeyValue(resp, "response", resp.toString());

		trace.sample(resp, CLASSNAME, true);

		// if you enable exception then CustomLoggingActor will be used
		// throw new Exception();

		sender().tell(resp, self());
	}

	/**
	 * @param reqB
	 * @throws Exception
	 */
	private void processRequestB(ActorB.RequestB reqB) throws Exception {

		log().info(CLASSNAME + " => " + reqB.getPayload() + " Path: " + self().path());

		APIResponse resp = new APIResponse(CLASSNAME);

		trace.sample(resp, CLASSNAME, true);
		trace.recordKeyValue(resp, "response", resp.toString());

		trace.createChild(resp, reqB);
		trace.start(resp, CLASSNAME);

		// throw new Exception();

		try {
			Thread.sleep(100);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		sender().tell(resp, self());

		trace.finish(resp);
	}

	/**
	 * @param e
	 */
	private void handleExp(Exception e) {
		log().info("Error--->>");
		e.printStackTrace();
	}

	/**
	 * @author Tuhin Gupta
	 *
	 */
	public static final class APIResponse extends TracingSupport implements Serializable {

		private final String payload;

		public String getPayload() {
			return payload;
		}

		public APIResponse(String payload) {

			this.payload = payload;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			APIResponse other = (APIResponse) obj;
			if (payload == null) {
				if (other.payload != null)
					return false;
			} else if (!payload.equals(other.payload))
				return false;
			return true;
		}

	}

}
