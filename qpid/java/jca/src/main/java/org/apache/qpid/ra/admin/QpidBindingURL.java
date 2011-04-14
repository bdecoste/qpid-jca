package org.apache.qpid.ra.admin;

import java.net.URISyntaxException;

import org.apache.qpid.url.AMQBindingURL;
import org.apache.qpid.url.BindingURL;

public class QpidBindingURL  extends AMQBindingURL {
	
	private String url;

	public QpidBindingURL(String url) throws URISyntaxException {
		super(url);

		if (!url.contains(BindingURL.OPTION_ROUTING_KEY) || getRoutingKey() == null) {
			setOption(BindingURL.OPTION_ROUTING_KEY, null);
		}

		this.url = url;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public String toString() {
		return url;
	}

}
