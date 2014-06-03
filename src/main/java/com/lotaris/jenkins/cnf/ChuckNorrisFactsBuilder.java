package com.lotaris.jenkins.cnf;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import javax.servlet.ServletException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Build step to retrieve a chuck norris facts and put it as a job variable.
 * The fact is directly logged to the job too.
 * 
 * @author Laurent Prevost <laurent.prevost@lotaris.com>
 */
public class ChuckNorrisFactsBuilder extends Builder {
	/**
	 * The URL to contact
	 */
	private final String factsUrl;
	
	/**
	 * Regex to parse the output
	 */
	private final String regexPattern;
	
	/**
	 * The variable name to set the fact
	 */
	private final String varName;
	
	@DataBoundConstructor
	public ChuckNorrisFactsBuilder(String factsUrl, String regexPattern, String varName) {
		this.factsUrl = factsUrl;
		this.regexPattern = regexPattern;
		this.varName = varName;
	}

	public String getFactsUrl() {
		return factsUrl;
	}

	public String getRegexPattern() {
		return regexPattern;
	}

	public String getVarName() {
		return varName;
	}
	
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		String fact;
		try {
			// Get the build variables
			EnvVars env = build.getEnvironment(listener);
	
			HttpClient client = HttpClients.createSystem();
			HttpGet get = new HttpGet(env.expand(factsUrl));

			HttpResponse response = client.execute(get);
			
			if (response.getStatusLine().getStatusCode() == 200) {
				Pattern p = Pattern.compile(regexPattern);
				Matcher m = p.matcher(EntityUtils.toString(response.getEntity()));
				
				if (m.matches()) {
					fact = m.group(1);
				}
				else {
					throw new Exception("Unable to retrieve the fact from the response: " + EntityUtils.toString(response.getEntity()));
				}
			}
			else {
				throw new Exception("Unable to retrieve the fact");
			}
		}
		catch (Exception e) {
			listener.getLogger().println(e.getMessage());
			fact = "There is no fact today! Chuck Norris is on Holiday!";
		}
		
		if (fact != null && !fact.isEmpty()) {
			Map<String, String> extendedParameters = new HashMap<String, String>();
			extendedParameters.put(varName, fact);
			build.addAction(new ChuckVariableAction(extendedParameters));
		}
		
		listener.getLogger().println("Chuck Norris Daily Fact: " + fact);
		
		return true;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		public DescriptorImpl() {
			load();
		}

		/**
		 * Performs on-the-fly validation of the form field 'urlFacts'.
		 *
		 * @param value This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the browser.
		 * <p>
		 * Note that returning {@link FormValidation#error(String)} does not prevent the form from being saved. It just means that a message will be displayed to
		 * the user.
		 */
		public FormValidation doCheckUrlFacts(@QueryParameter String value)
			throws IOException, ServletException {
			if (value.length() == 0) {
				return FormValidation.error("Please add URL facts.");
			}
			if (value.length() < 4) {
				return FormValidation.warning("Isn't the URL facts too short?");
			}
			return FormValidation.ok();
		}

		/**
		 * Performs on-the-fly validation of the form field 'regexPattern'.
		 *
		 * @param value This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the browser.
		 * <p>
		 * Note that returning {@link FormValidation#error(String)} does not prevent the form from being saved. It just means that a message will be displayed to
		 * the user.
		 */
		public FormValidation doCheckRegexPattern(@QueryParameter String value)
			throws IOException, ServletException {
			if (value.length() == 0) {
				return FormValidation.error("Please add regex pattern.");
			}
			if (value.length() < 4) {
				return FormValidation.warning("Isn't the regex pattern too short?");
			}
			return FormValidation.ok();
		}

		/**
		 * Performs on-the-fly validation of the form field 'varName'.
		 *
		 * @param value This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the browser.
		 * <p>
		 * Note that returning {@link FormValidation#error(String)} does not prevent the form from being saved. It just means that a message will be displayed to
		 * the user.
		 */
		public FormValidation doCheckVarName(@QueryParameter String value)
			throws IOException, ServletException {
			if (value.length() == 0) {
				return FormValidation.error("Please add variable name.");
			}
			if (value.length() < 2) {
				return FormValidation.warning("Isn't the variable name too short?");
			}
			return FormValidation.ok();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		public String getDisplayName() {
			return "Retrieve Chuck Norris Fact.";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			save();
			return super.configure(req, formData);
		}
	}
}
