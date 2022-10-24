package SentimentAnalyzer;

/**
 * A Java class that implements a sentiment analyzer, based on WEKA.
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import weka.core.*;
import weka.classifiers.meta.FilteredClassifier;

import java.io.*;

public class SentimentAnalyzer extends AbstractHandler{

	Instances instances;
	FilteredClassifier classifierNaiveBayes;
	FilteredClassifier classifierComplement;
	FilteredClassifier classifierMultinomial;
	FilteredClassifier classifierLibSVM;
	FilteredClassifier classifierJ48;

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) 
			throws IOException, ServletException
	{
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		response.getWriter().println("<h1>Sentiment	Analysis on Turkish	Tweets</h1>");
		response.getWriter().println("<form method=\"post\" action=\"analysis\"><br></br>");
		response.getWriter().println("<input type=\"text\" name=\"text\" value=\"Enter Some Text...\" size=\"140\" maxlength=\"140\" onfocus=\"this.value=''\"><br></br>");
		response.getWriter().println("<input type=\"submit\" value=\"Show Result!\" align=\"center\"></form><br></br>Tweet: ");

		if(baseRequest.toString().contains("analysis")) {

			String tweet = request.getParameter("text");
			response.getWriter().println(tweet + "<br></br>");

			makeInstance(tweet);

			response.getWriter().println("<table style=\"width:20%\">");
			response.getWriter().println("<tr><td><b>Algorithm</b></td><td>"+ "<b>Prediction</b>" +" </td></tr>");
			response.getWriter().println("<tr><td>NaiveBayes</td><td>"+ classify(classifierNaiveBayes) +" </td></tr>");
			response.getWriter().println("<tr><td>Multinomial</td><td>"+ classify(classifierMultinomial) +"  </td></tr>");
			response.getWriter().println("<tr><td>Complement</td><td>"+ classify(classifierComplement) +" </td></tr>");
			response.getWriter().println("<tr><td>LibSVM</td><td>"+ classify(classifierLibSVM) +" </td></tr>");
			response.getWriter().println("<tr><td>J48</td><td>"+ classify(classifierJ48) +" </td></tr>");
			response.getWriter().println("</table>");

		}
	}

	public FilteredClassifier loadModel(String fileName) {

		FilteredClassifier classifier = new FilteredClassifier();

		try {

			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			Object tmp = in.readObject();
			classifier = (FilteredClassifier) tmp;
			in.close();
			System.out.println("Loaded model: " + fileName);
		} 
		catch (Exception e) {
			// Given the cast, a ClassNotFoundException must be caught along with the IOException
			System.out.println("Problem found when reading: " + fileName + e.getMessage());
		}

		return classifier;
	}

	public void makeInstance(String str) {

		String tweet = new String(str);
		tweet = tweet.toLowerCase();
		tweet = tweet.replaceAll("[^a-zöçþðüý ]", " ");
		System.out.println("Tweet is: " + tweet);

		// Create the attributes, class and text
		FastVector fvNominalVal = new FastVector(3);
		fvNominalVal.addElement("Negative");
		fvNominalVal.addElement("Positive");
		Attribute attribute1 = new Attribute("class", fvNominalVal);
		Attribute attribute2 = new Attribute("text",(FastVector) null);
		// Create list of instances with one element
		FastVector fvWekaAttributes = new FastVector(2);
		fvWekaAttributes.addElement(attribute2);
		fvWekaAttributes.addElement(attribute1);
		instances = new Instances("Test relation", fvWekaAttributes, 1);           
		// Set class index
		instances.setClassIndex(1);
		// Create and add the instance
		Instance instance = new Instance(2);
		instance.setValue(attribute2, tweet);
		instances.add(instance);	
	}


	public String classify(FilteredClassifier filteredclassifier) {

		double pred = 0;
		try {

			pred = filteredclassifier.classifyInstance(instances.instance(0));
			return instances.classAttribute().value((int) pred);
		}
		catch (Exception e) {
			System.out.println("Problem found when classifying the text");
		}

		return "Negative";
	}

	public static void main (String[] args) throws Exception {

		SentimentAnalyzer messageclassifier;

		messageclassifier = new SentimentAnalyzer();

		messageclassifier.classifierNaiveBayes = messageclassifier.loadModel("./models/naivebayes.model");
		messageclassifier.classifierMultinomial = messageclassifier.loadModel("./models/multinomial.model");
		messageclassifier.classifierComplement = messageclassifier.loadModel("./models/complement.model");
		messageclassifier.classifierLibSVM = messageclassifier.loadModel("./models/libsvm.model");
		messageclassifier.classifierJ48 = messageclassifier.loadModel("./models/j48.model");

		Server server = new Server(11111);
		server.setHandler((Handler) messageclassifier);

		server.start();	
		server.join();
	}
}	