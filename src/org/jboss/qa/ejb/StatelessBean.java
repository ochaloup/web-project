package org.jboss.qa.ejb;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.logging.Logger;
import org.jboss.qa.service.ManagementService;
import org.jboss.qa.utils.Utils;

@Stateless
@Remote(IStatelessBean.class)
public class StatelessBean implements IStatelessBean {
	private static final Logger log = Logger.getLogger(StatelessBean.class);

	private static final String TEST_PROP_NAME = "qa.test.property";
	private static final String TEST_EXPRESSION_PROP_NAME = "qa.test.exp";
	private static final String DEFAULT_VALUE = "defaultValue";
	
	@Override
	public String getProperty(String propertyName) {
		ModelControllerClient client = ManagementService.getClient();
		assert(client == null);
		
		String resolvedTestProp = Utils.readSystemPropertyResolve(TEST_PROP_NAME, client);
		log.info("Resolved property " + TEST_PROP_NAME + " value " + resolvedTestProp);
		String resolvedExpressionTestProp = Utils.readSystemPropertyResolve(TEST_EXPRESSION_PROP_NAME, client);
		log.info("Resolved property " + TEST_EXPRESSION_PROP_NAME + " value " + resolvedExpressionTestProp);
		log.info("System property " + TEST_PROP_NAME + " value " + System.getProperty(TEST_PROP_NAME));
		log.info("System property " + TEST_EXPRESSION_PROP_NAME + " value " + System.getProperty(TEST_EXPRESSION_PROP_NAME));
		log.info("System property " + TEST_PROP_NAME + "2 value " + System.getProperty(TEST_PROP_NAME + "2"));
		return "Check the log server log...";
	}

}
