package org.jboss.qa.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.Services;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * The idea and the most part of the code taken from: 
 * http://management-platform.blogspot.cz/2012/07/co-located-management-client-for.html
 * The service loading problem seems to still exist: https://issues.jboss.org/browse/AS7-5172
 */
public class ManagementService implements ServiceActivator {
	   private static final Logger log = Logger.getLogger(ManagementService.class);
	   private static volatile ModelController controller;
	   private static volatile ExecutorService executor;


	   public static ModelControllerClient getClient() {
	      ModelControllerClient client = controller.createClient(executor);
		  log.info("Returning controller: " + controller + " and client " + client);
	      return client;
	   }


	   @Override
	   public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
		  log.info("Activating service " + ManagementService.class.getName());
	      final GetModelControllerService service = new GetModelControllerService();
	      context
	          .getServiceTarget()
	          .addService(ServiceName.of("management", "client", "getter"), service)
	          .addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class, service.modelControllerValue)
	          .install();
	   }


	   private class GetModelControllerService implements Service<Void> {
		  private final Logger log = Logger.getLogger(GetModelControllerService.class);
	      private InjectedValue<ModelController> modelControllerValue = new InjectedValue<ModelController>();


	      @Override
	      public Void getValue() throws IllegalStateException, IllegalArgumentException {
	         return null;
	      }


	      @Override
	      public void start(StartContext context) throws StartException {
	         ManagementService.executor = Executors.newFixedThreadPool(5, new ThreadFactory() {
	             @Override
	             public Thread newThread(Runnable r) {
	                 Thread t = new Thread(r);
	                 t.setDaemon(true);
	                 t.setName("ManagementServiceModelControllerClientThread");
	                 return t;
	             }
	         });
	         ManagementService.controller = modelControllerValue.getValue();
	         log.info(GetModelControllerService.class.getSimpleName() + " service started");
	      }


	      @Override
	      public void stop(StopContext context) {
	         try {
	            ManagementService.executor.shutdownNow();
	         } finally {
	            ManagementService.executor = null;
	            ManagementService.controller = null;
	         }
	         log.info(GetModelControllerService.class.getSimpleName() + " service stopped");
	      }
	   }
	}
