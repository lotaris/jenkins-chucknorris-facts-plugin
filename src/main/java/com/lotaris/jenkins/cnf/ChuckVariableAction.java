package com.lotaris.jenkins.cnf;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;
import java.util.Map;

/**
 * Action to add a variable to the build with the Chuck Norris Fact
 * 
 * @author Laurent Prevost <laurent.prevost@lotaris.com>
 */
public class ChuckVariableAction implements EnvironmentContributingAction {
	/**
	 * Extended parameters
	 */
	private Map<String, String> extendedParameters;
	
	/**
	 * Empty constructor
	 */
	public ChuckVariableAction() {}

	/**
	 * Constructor
	 * 
	 * @param extendedParameters Parameters for extending
	 */
	public ChuckVariableAction(Map<String, String> extendedParameters) {
		this.extendedParameters = extendedParameters;
	}

	public String getDisplayName() {
		return "ChuckVariableAction";
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return null;
	}

	public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
		// Nothing to do if no extended parameters
		if (env == null || extendedParameters == null) {
			return;
		} 
		
		// Add each param if value is not null and key is not null
		for (Map.Entry<String, String> e : extendedParameters.entrySet()) {
			if (e.getKey() != null && e.getValue() != null) {
				env.put(e.getKey(), e.getValue());
			}
		}
	}
}
