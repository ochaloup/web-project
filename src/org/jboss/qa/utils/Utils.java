package org.jboss.qa.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

public class Utils {
	private static final Logger log = Logger.getLogger(Utils.class);
	private static ModelControllerClient client;
	
	private static final String systemPropertyPath = "/system-property=";
	
	public static synchronized ModelControllerClient getClient() {
		if(client == null) {
			client = createClient();
		}
		return client;
	}
	
	public static synchronized void closeClient() {
		if(client!=null) {
			try {
				client.close();
			} catch (IOException ioe) {
				log.error("Not able to close client!");
			}
		}
	}
	
	private static ModelControllerClient createClient() {
		// TODO: not hardcoded address and port :)
		try {
			ModelControllerClient client = ModelControllerClient.Factory.create(
					InetAddress.getByName("127.0.0.1"), 9999);
			return client;
		} catch (UnknownHostException uhe) {
			throw new RuntimeException(uhe);
		}
	}
	
	/**
	 * Address in "standart" format: /subsystem=web/something=something/...
	 * @return  list of PathElements which could be passed to PathAddress class
	 */
	public static List<PathElement> parseAddress(String address) {
		String[] splitAddrParts = address.split("/");
		List<PathElement> elements = new ArrayList<PathElement>();
		
		for (String addrPart: splitAddrParts) {
			addrPart = addrPart.trim();
			if(!addrPart.isEmpty()) {
				String[] nameVal = addrPart.split("=");
				if(nameVal.length == 1 ) {
					elements.add(PathElement.pathElement(addrPart));
				} else if (nameVal.length == 2) {
					elements.add(PathElement.pathElement(nameVal[0], nameVal[1]));
				} else {
					throw new RuntimeException("There is problem with element " + addrPart + " for address: " + address + ". Not able to parse.");
				}
			}
		}
		
		return elements;
	}
	
	public static ModelNode readAttr(String address, String attrName) {
		return readAttr(address, attrName, getClient());
	}
	
	//	op.get("recursive").set(true);
	// op.get("operations").set(true);
	public static ModelNode readAttr(String address, String attrName, ModelControllerClient client) {
		PathAddress pa = PathAddress.pathAddress(parseAddress(address));
		ModelNode mdAddress = pa.toModelNode();
		
		ModelNode operation = new ModelNode(); 
		operation.get(OP).set(READ_ATTRIBUTE_OPERATION);
		operation.get(NAME).set(attrName);
        operation.get(OP_ADDR).set(mdAddress);
        
        return execute(operation, client);

	}
	
	public static ModelNode writeAttr(String address, String attrName, String attrValue) {
		return writeAttr(address, attrName, attrValue, getClient());
	}
	
	public static ModelNode writeAttr(String address, String attrName, String attrValue, ModelControllerClient client) {
		PathAddress pa = PathAddress.pathAddress(parseAddress(address));
		ModelNode mdAddress = pa.toModelNode();
		
		ModelNode operation = new ModelNode(); 
		operation.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
		operation.get(NAME).set(attrName);
		operation.get(VALUE).set(attrValue);
        operation.get(OP_ADDR).set(mdAddress);
        
        return execute(operation, client);
	}
	
	private static ModelNode execute(ModelNode operation, ModelControllerClient client) {
		try {
        	return client.execute(operation);
        } catch(IOException ioe) {
        	throw new RuntimeException(ioe);
        }
	}
	
	public static ModelNode addSystemProperty(String name, String value) {
		return addSystemProperty(name, value, getClient());
	}
	
	public static ModelNode addSystemProperty(String name, String value, ModelControllerClient client) {
		// /system-property=foo:add(value=bar)
		String addressString = systemPropertyPath + name;
		PathAddress pa = PathAddress.pathAddress(parseAddress(addressString));
		ModelNode mdAddress = pa.toModelNode();
		
		ModelNode operation = new ModelNode(); 
		operation.get(OP).set(ADD);
		operation.get(NAME).set("value");
		operation.get(VALUE).set(value);
        operation.get(OP_ADDR).set(mdAddress);
		
		return execute(operation, client);
	}
	
	public static ModelNode readSystemProperty(String name, ModelControllerClient client) {
		String addressString = systemPropertyPath + name;
		PathAddress pa = PathAddress.pathAddress(parseAddress(addressString));
		ModelNode mdAddress = pa.toModelNode();
		
		ModelNode operation = new ModelNode(); 
		operation.get(OP).set(READ_ATTRIBUTE_OPERATION);
		operation.get(NAME).set(VALUE);
        operation.get(OP_ADDR).set(mdAddress);
		
		ModelNode opResult = execute(operation, client);
		assert SUCCESS.equals(opResult.get(OUTCOME).asString());
		return opResult;
	}
	
	public static ModelNode getResult(ModelNode modelNodeResult) {
       if (!SUCCESS.equals(modelNodeResult.get(OUTCOME).asString())) {
           log.error("The outcome of the result is was not succesful: " + modelNodeResult.toString());
       }
       return modelNodeResult.get(RESULT);
	}
	
	public static String readSystemPropertyResolve(String name, ModelControllerClient client) {
		ModelNode sp = readSystemProperty(name, client);
		ModelNode resolved = sp.resolve();
		assert SUCCESS.equals(resolved.get(OUTCOME).asString());
		return getResult(resolved).asString();
	}
}
