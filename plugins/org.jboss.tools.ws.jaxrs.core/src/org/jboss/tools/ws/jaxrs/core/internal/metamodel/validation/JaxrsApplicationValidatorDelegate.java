/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.ws.jaxrs.core.internal.metamodel.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.jboss.tools.common.validation.TempMarkerManager;
import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsJavaApplication;
import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsWebxmlApplication;
import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
import org.jboss.tools.ws.jaxrs.core.internal.utils.WtpUtils;
import org.jboss.tools.ws.jaxrs.core.jdt.Annotation;
import org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname;
import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsApplication;
import org.jboss.tools.ws.jaxrs.core.preferences.JaxrsPreferences;

/**
 * Java-based JAX-RS Application validator
 * 
 * @author Xavier Coulon
 * 
 */
public class JaxrsApplicationValidatorDelegate extends AbstractJaxrsElementValidatorDelegate<IJaxrsApplication> {

	public JaxrsApplicationValidatorDelegate(final TempMarkerManager markerManager) {
		super(markerManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.tools.ws.jaxrs.core.internal.metamodel.validation.
	 * AbstractJaxrsElementValidatorDelegate#validate()
	 */
	@Override
	public void validate(final IJaxrsApplication application) throws CoreException {
		if (application.isBinary()) {
			Logger.debug("Skipping validation on binary element {}", application);
			return;
		}
		if (application.isJavaApplication()) {
			validate((JaxrsJavaApplication) application);
		} else {
			validate((JaxrsWebxmlApplication) application);
		}
	}

	public void validate(final JaxrsJavaApplication application) throws CoreException {
		Logger.debug("Validating element {}", application);
		JaxrsMetamodelValidator.deleteJaxrsMarkers(application);
		final Annotation applicationPathAnnotation = application
				.getAnnotation(EnumJaxrsClassname.APPLICATION_PATH.qualifiedName);
		final IType appJavaElement = application.getJavaElement();
		if (!application.isOverriden() && applicationPathAnnotation == null) {
			addProblem(JaxrsValidationMessages.JAVA_APPLICATION_MISSING_APPLICATION_PATH_ANNOTATION,
					JaxrsPreferences.JAVA_APPLICATION_MISSING_APPLICATION_PATH_ANNOTATION, new String[0],
					appJavaElement.getNameRange().getLength(), appJavaElement.getNameRange().getOffset(),
					application.getResource(),
					JaxrsValidationConstants.JAVA_APPLICATION_MISSING_APPLICATION_PATH_ANNOTATION_QUICKFIX_ID);
		}
		if (!application.isJaxrsCoreApplicationSubclass()) {
			addProblem(JaxrsValidationMessages.JAVA_APPLICATION_INVALID_TYPE_HIERARCHY,
					JaxrsPreferences.JAVA_APPLICATION_INVALID_TYPE_HIERARCHY,
					new String[] { appJavaElement.getFullyQualifiedName() }, application.getJavaElement()
							.getSourceRange().getLength(), appJavaElement.getSourceRange().getOffset(),
					application.getResource(),
					JaxrsValidationConstants.JAVA_APPLICATION_INVALID_TYPE_HIERARCHY_QUICKFIX_ID);
		}

		if (application.getMetamodel().hasMultipleApplications()) {
			final IResource javaResource = application.getResource();
			ISourceRange javaNameRange = application.getJavaElement().getNameRange();
			if (javaNameRange == null) {
				Logger.warn("Cannot add a problem marker: unable to locate '"
						+ application.getJavaElement().getElementName() + "' in resource '"
						+ application.getJavaElement().getResource().getFullPath().toString() + "'. ");
			} else {
				addProblem(JaxrsValidationMessages.APPLICATION_TOO_MANY_OCCURRENCES,
						JaxrsPreferences.APPLICATION_TOO_MANY_OCCURRENCES, new String[0], javaNameRange.getLength(),
						javaNameRange.getOffset(), javaResource);
			}
		}

	}

	public void validate(final JaxrsWebxmlApplication webxmlApplication) throws CoreException {
		Logger.debug("Validating element {}", webxmlApplication);
		JaxrsMetamodelValidator.deleteJaxrsMarkers(webxmlApplication);
		if (webxmlApplication.getMetamodel().hasMultipleApplications()) {
			final IResource webxmlResource = webxmlApplication.getResource();
			ISourceRange webxmlNameRange = WtpUtils.getApplicationPathLocation(webxmlApplication.getResource(),
					webxmlApplication.getJavaClassName());
			if (webxmlNameRange == null) {
				Logger.warn("Cannot add a problem marker: unable to locate '" + webxmlApplication.getJavaClassName()
						+ "' in resource '" + webxmlApplication.getResource().getFullPath().toString() + "'. ");
			} else {
				addProblem(JaxrsValidationMessages.APPLICATION_TOO_MANY_OCCURRENCES,
						JaxrsPreferences.APPLICATION_TOO_MANY_OCCURRENCES, new String[0], webxmlNameRange.getLength(),
						webxmlNameRange.getOffset(), webxmlResource);
			}
		}
	}

}