package org.jboss.qa;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;



@WebServlet(name="HelloServlet", urlPatterns={"/"})
public class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final Logger log = Logger.getLogger(HelloServlet.class);
    
    // public static final String TESTING_PROPERTY = "testing-jboss.qa-property";
    public static final String TESTING_PROPERTY = "a.b.c";

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException {
    	log.info("We are in the doGet method of " + HelloServlet.class.getSimpleName());
    	
        PrintWriter out = response.getWriter();
        
        String outMsg = System.getProperty(TESTING_PROPERTY);
        out.println("We are getting value " + outMsg + " from property name: " + TESTING_PROPERTY);
        
        /*
        for(String propertyName: System.getProperties().stringPropertyNames()) {
        	System.out.println("Property name: " + propertyName + ", value: " + System.getProperty(propertyName));
        }
        */
        
        System.setProperty(TESTING_PROPERTY, "localhost");
        out.println("HelloServlet");
    }

}
