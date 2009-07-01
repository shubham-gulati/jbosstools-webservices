/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tools.ws.creation.core.utils;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.ws.internal.common.J2EEUtils;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.ws.core.JBossWSCorePlugin;
import org.jboss.tools.ws.core.classpath.JBossWSRuntime;
import org.jboss.tools.ws.core.classpath.JBossWSRuntimeManager;
import org.jboss.tools.ws.core.facet.delegate.IJBossWSFacetDataModelProperties;
import org.jboss.tools.ws.core.messages.JBossWSCoreMessages;
import org.jboss.tools.ws.core.utils.StatusUtils;
import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;

public class JBossWSCreationUtils {

	static final String javaKeyWords[] = { "abstract", "assert", "boolean", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"break", "byte", "case", "catch", "char", "class", "const", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"continue", "default", "do", "double", "else", "extends", "false", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"final", "finally", "float", "for", "goto", "if", "implements", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"import", "instanceof", "int", "interface", "long", "native", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"new", "null", "package", "private", "protected", "public", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"return", "short", "static", "strictfp", "super", "switch", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"synchronized", "this", "throw", "throws", "transient", "true", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"try", "void", "volatile", "while" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	public static boolean isJavaKeyword(String keyword) {
		if (hasUpperCase(keyword)) {
			return false;
		}
		return (Arrays.binarySearch(javaKeyWords, keyword, Collator
				.getInstance(Locale.ENGLISH)) >= 0);
	}

	private static boolean hasUpperCase(String nodeName) {
		if (nodeName == null) {
			return false;
		}
		for (int i = 0; i < nodeName.length(); i++) {
			if (Character.isUpperCase(nodeName.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static IPath getWorkspace() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation();
	}

	public static IProject getProjectByName(String project) {
		String projectString = replaceEscapecharactors(project);
		return ResourcesPlugin.getWorkspace().getRoot().getProject(
				getProjectNameFromFramewokNameString(projectString));
	}

	public static IPath getProjectRoot(String project) {
		String projectString = replaceEscapecharactors(project);
		return ResourcesPlugin.getWorkspace().getRoot().getProject(
				getProjectNameFromFramewokNameString(projectString))
				.getLocation();
	}

	public static String pathToWebProjectContainer(String project) {
		IPath projectRoot = getProjectRoot(project);
		IPath currentDynamicWebProjectDir = J2EEUtils
				.getWebContentPath(getProjectByName(project));
		IPath currentDynamicWebProjectDirWithoutProjectRoot = J2EEUtils
				.getWebContentPath(getProjectByName(project))
				.removeFirstSegments(1).makeAbsolute();
		if (projectRoot.toOSString().contains(getWorkspace().toOSString())) {
			return getWorkspace().append(currentDynamicWebProjectDir)
					.toOSString();
		} else {
			return projectRoot.append(
					currentDynamicWebProjectDirWithoutProjectRoot).toOSString();
		}

	}

	public static String pathToWebProjectContainerWEBINF(String project) {
		IPath projectRoot = getProjectRoot(project);
		IPath webContainerWEBINFDir = J2EEUtils
				.getWebInfPath(getProjectByName(project));
		IPath webContainerWEBINFDirWithoutProjectRoot = J2EEUtils
				.getWebInfPath(getProjectByName(project))
				.removeFirstSegments(1).makeAbsolute();
		if (projectRoot.toOSString().contains(getWorkspace().toOSString())) {
			return getWorkspace().append(webContainerWEBINFDir).toOSString();
		} else {
			return projectRoot.append(webContainerWEBINFDirWithoutProjectRoot)
					.toOSString();
		}
	}

	private static String replaceEscapecharactors(String vulnarableString) {
		if (vulnarableString.indexOf("/") != -1) { //$NON-NLS-1$
			vulnarableString = vulnarableString.replace('/', File.separator
					.charAt(0));
		}
		return vulnarableString;
	}

	private static String getProjectNameFromFramewokNameString(
			String frameworkProjectString) {
		if (frameworkProjectString.indexOf(getSplitCharactor()) == -1) {
			return frameworkProjectString;
		} else {
			return frameworkProjectString.split(getSplitCharactors())[1];
		}
	}

	private static String getSplitCharactor() {
		// Windows check (because from inside wtp in return I received a hard
		// coded path)
		if (File.separatorChar == '\\') {
			return "\\"; //$NON-NLS-1$
		} else {
			return File.separator;
		}
	}

	private static String getSplitCharactors() {
		// Windows check (because from inside wtp in return I received a hard
		// coded path)
		if (File.separatorChar == '\\') {
			return "\\" + File.separator; //$NON-NLS-1$
		} else {
			return File.separator;
		}
	}

	public static String classNameFromQualifiedName(String qualifiedCalssName) {
		// This was done due to not splitting with . Strange
		qualifiedCalssName = qualifiedCalssName.replace('.', ':');
		String[] parts = qualifiedCalssName.split(":"); //$NON-NLS-1$
		if (parts.length == 0) {
			return ""; //$NON-NLS-1$
		}
		return parts[parts.length - 1];
	}

	// JDT utils
	/**
	 * get JavaProject object from project name
	 */
	public static IJavaProject getJavaProjectByName(String projectName)
			throws JavaModelException {

		IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace()
				.getRoot());
		model.open(null);

		IJavaProject[] projects = model.getJavaProjects();

		for (IJavaProject proj : projects) {
			if (proj.getProject().getName().equals(projectName)) {
				return proj;
			}
		}

		return null;
	}

	public static ICompilationUnit findUnitByFileName(IJavaElement javaElem,
			String filePath) throws Exception {
		ICompilationUnit unit = null;

		if (!javaElem.getOpenable().isOpen()) {
			javaElem.getOpenable().open(null);
		}

		IJavaElement[] elems = null;

		if (javaElem instanceof IParent) {
			IParent parent = (IParent) javaElem;
			elems = parent.getChildren();
		}

		if (elems == null) {
			return null;
		}

		for (IJavaElement elem : elems) {
			if (elem.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
				IPackageFragmentRoot root = (IPackageFragmentRoot) elem;

				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					unit = findUnitByFileName(elem, filePath);

					if (unit != null) {
						return unit;
					}
				}
			} else if ((elem.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
					|| (elem.getElementType() == IJavaElement.JAVA_PROJECT)) {
				unit = findUnitByFileName(elem, filePath);

				if (unit != null) {
					return unit;
				}
			} else if (elem.getElementType() == IJavaElement.COMPILATION_UNIT) {
				ICompilationUnit compUnit = (ICompilationUnit) elem;

				if (compUnit.getPath().toString().equals(filePath)) {
					compUnit.open(null);

					return compUnit;
				}
			}
		}

		return null;
	}

	/**
	 * get Java compilation unit by file path
	 * 
	 * @param javaFile
	 *            the java sour file to look
	 * @return ICompilationUnit, JDK compilation unit for this java file.
	 */
	public static ICompilationUnit getJavaUnitFromFile(IFile javaFile) {
		try {
			IJavaProject project = getJavaProjectByName(javaFile.getProject()
					.getName());

			if (project == null) {
				return null;
			}

			return findUnitByFileName(project, javaFile.getFullPath()
					.toString());
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean validateJBossWSLocation() {
		String location = JBossWSCorePlugin.getDefault().getPreferenceStore()
				.getString(JBossWSCoreMessages.WS_Location);
		if (location == null || location.equals("")) { //$NON-NLS-1$
			return false;
		}
		return true;
	}

	public static String getJBossWSRuntimeLocation(IProject project)
			throws CoreException {

		String isServerSupplied = project
				.getPersistentProperty(IJBossWSFacetDataModelProperties.PERSISTENCE_PROPERTY_SERVER_SUPPLIED_RUNTIME);
		String jbwsRuntimeName = project
				.getPersistentProperty(IJBossWSFacetDataModelProperties.PERSISTENCE_PROPERTY_QNAME_RUNTIME_NAME);

		if (jbwsRuntimeName != null
				&& !"".equals(jbwsRuntimeName) //$NON-NLS-1$
				&& !IJBossWSFacetDataModelProperties.DEFAULT_VALUE_IS_SERVER_SUPPLIED
						.equals(isServerSupplied)) {
			JBossWSRuntime jbws = JBossWSRuntimeManager.getInstance()
					.findRuntimeByName(jbwsRuntimeName);
			if (jbws != null) {
				return jbws.getHomeDir();
			} else {
				String jbwsHomeDir = project
						.getPersistentProperty(IJBossWSFacetDataModelProperties.PERSISTENCE_PROPERTY_RNTIME_LOCATION);
				if (new File(jbwsHomeDir).exists()) {
					return jbwsHomeDir;
				}
			}
		}
		// if users select server as its jbossws runtime, then get runtime
		// location from project target runtime

		IFacetedProject facetedPrj = ProjectFacetsManager.create(project);
		org.eclipse.wst.common.project.facet.core.runtime.IRuntime prjFacetRuntime = facetedPrj
				.getPrimaryRuntime();

		if (prjFacetRuntime != null) {
			IRuntime serverRuntime = getRuntime(prjFacetRuntime);
			String runtimeTypeName = serverRuntime.getRuntimeType().getName();
			if (runtimeTypeName == null) {
				runtimeTypeName = ""; //$NON-NLS-1$
			}
			if (runtimeTypeName.toUpperCase().indexOf("JBOSS") >= 0) { //$NON-NLS-1$
				String runtimeLocation = serverRuntime.getLocation()
						.toOSString();
				if (runtimeLocation.endsWith("bin")) { //$NON-NLS-1$
					return serverRuntime.getLocation().removeLastSegments(1)
							.toOSString();
				} else {
					return runtimeLocation;
				}
			}
		}

		// if no target runtime has been specified, get runtime location from
		// default jbossws runtime
		if (prjFacetRuntime == null) {
			JBossWSRuntime jbws = JBossWSRuntimeManager.getInstance()
					.getDefaultRuntime();
			if (jbws != null) {
				return jbws.getHomeDir();
			} else {
				throw new CoreException(
						StatusUtils
								.errorStatus(JBossWSCreationCoreMessages.Error_Message_No_Runtime_Specified));
			}

		}

		return ""; //$NON-NLS-1$

	}

	public static IRuntime getRuntime(
			org.eclipse.wst.common.project.facet.core.runtime.IRuntime runtime) {
		if (runtime == null)
			throw new IllegalArgumentException();

		String id = runtime.getProperty("id"); //$NON-NLS-1$
		if (id == null)
			return null;

		org.eclipse.wst.server.core.IRuntime[] runtimes = ServerCore
				.getRuntimes();
		int size = runtimes.length;
		for (int i = 0; i < size; i++) {
			if (id.equals(runtimes[i].getId()))
				return runtimes[i];
		}

		return null;
	}

	public static String getJavaProjectSrcLocation(IProject project) throws JavaModelException {
		IResource[] rs = getJavaSourceRoots(project);
		String src = ""; //$NON-NLS-1$
		if (rs == null || rs.length == 0)
			return src;
		for (int i = 0; i < rs.length; i++) {
			IPath p = rs[i].getLocation();
			if (p != null) {
				src = p.toOSString();
			}
		}
		return src;
	}

	public static IResource[] getJavaSourceRoots(IProject project) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null)
			return null;
		List<IResource> resources = new ArrayList<IResource>();
		IClasspathEntry[] es = javaProject.getResolvedClasspath(true);
		for (int i = 0; i < es.length; i++) {
			if (es[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				IResource findMember = ResourcesPlugin.getWorkspace().getRoot()
						.findMember(es[i].getPath());
				if (findMember != null && findMember.exists()) {
					resources.add(findMember);
				}
			}
		}
		return resources.toArray(new IResource[resources.size()]);
	}

}
