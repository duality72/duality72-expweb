/*
 * The MIT License
 *
 * Copyright (c) 2011, Centrum Systems Pty Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package au.com.centrumsystems.hudson.plugin.util;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Cause.UpstreamCause;
import hudson.model.CauseAction;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Provides helper methods for #hudson.model.AbstractBuild
 * 
 * @author Centrum Systems
 * 
 */
public final class BuildUtil {

    /**
     * Gets the next downstream build based on the upstream build and downstream project.
     * 
     * @param downstreamProject
     *            - The downstream project
     * @param upstreamBuild
     *            - The upstream build
     * @return - The next downstream build based on the upstream build and downstream project, or null if there is no downstream project.
     */
    public static AbstractBuild<?, ?> getDownstreamBuild(final AbstractProject<?, ?> downstreamProject,
            final AbstractBuild<?, ?> upstreamBuild) {
        if ((downstreamProject != null) && (upstreamBuild != null)) {
            for (final AbstractBuild<?, ?> innerBuild : (List<AbstractBuild<?, ?>>) downstreamProject.getBuilds()) {
                for (final CauseAction action : innerBuild.getActions(CauseAction.class)) {
                    for (final Cause cause : action.getCauses()) {
                        if (cause instanceof UpstreamCause) {
                            final UpstreamCause upstreamCause = (UpstreamCause) cause;
                            if (upstreamCause.getUpstreamProject().equals(upstreamBuild.getProject().getName())
                                    && (upstreamCause.getUpstreamBuild() == upstreamBuild.getNumber())) {
                                return innerBuild;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Given an Upstream AbstractBuild and a Downstream AbstractProject will retrieve the associated ParametersAction.
     * This will result in parameters from the upstream build not overriding parameters on the downstream project. 
     * @param downstreamBuild - The AbstractBuild
     * @param downstreamProject - The AbstractProject
     * @return - AbstractBuild's ParametersAction
     */
    public static Action getAllBuildParametersAction(AbstractBuild<?, ?> downstreamBuild, AbstractProject<?, ?> downstreamProject) {
        // Retrieve the List of Actions from the downstream project
        final ParametersAction dsProjectParametersAction = ProjectUtil.getProjectParametersAction(downstreamProject);
        
        // Retrieve the List of Actions from the upstream build
        final ParametersAction usBuildParametersAction = BuildUtil.getBuildParametersAction(downstreamBuild);
        
        return mergeParameters(usBuildParametersAction, dsProjectParametersAction);
    }

    /**
     * Gets the ParametersAction of an AbstractBuild
     * @param build - AbstractBuild
     * @return - ParametersAction of AbstractBuild
     */
    public static ParametersAction getBuildParametersAction(AbstractBuild<?, ?> build) {
        ParametersAction buildParametersAction = null;
        if (build != null) {
            // If a ParametersAction is found
            for (Action nextAction : build.getActions()) {
                if (nextAction instanceof ParametersAction) {
                    buildParametersAction = (ParametersAction) nextAction;
                }
            }
        }
        
        return buildParametersAction;
    }

    /**
     * Merges two sets of ParametersAction
     * @param base ParametersAction set 1
     * @param overlay ParametersAction set 2
     * @return - Single set of ParametersAction
     */
    public static ParametersAction mergeParameters(ParametersAction base, ParametersAction overlay) {
        final LinkedHashMap<String, ParameterValue> params = new LinkedHashMap<String, ParameterValue>();
        if (base != null) {
            for (ParameterValue param : base.getParameters()) {
                params.put(param.getName(), param);
            }
        }
        
        if (overlay != null) {
            for (ParameterValue param : overlay.getParameters()) {
                params.put(param.getName(), param);
            }
        }
        
        return new ParametersAction(params.values().toArray(new ParameterValue[params.size()]));
    }
    
}
