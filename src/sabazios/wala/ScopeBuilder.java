package sabazios.wala;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.util.io.FileProvider;

public class ScopeBuilder {
	public List<String> binaryDependencies;
	public List<String> jarDependencies;
	public List<String> extensionBinaryDependencies;
	public AnalysisCache cache;

	public ScopeBuilder() {
		this.binaryDependencies = new ArrayList<String>();
		this.jarDependencies = new ArrayList<String>();
		this.extensionBinaryDependencies = new ArrayList<String>();
	}

	public AnalysisScope getScope() throws IOException {
		AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
		ClassLoader loader = DataRaceAnalysisTest.class.getClassLoader();
		scope.addToScope(scope.getLoader(AnalysisScope.PRIMORDIAL), 
				new JarFile(getJavaClassesJar()));
	
		for (String directory : binaryDependencies) {
			File sd = FileProvider.getFile(directory, loader);
			assert sd.isDirectory();
			scope.addToScope(scope.getLoader(AnalysisScope.APPLICATION), new BinaryDirectoryTreeModule(sd));
		}
		for (String directory : extensionBinaryDependencies) {
			File sd = FileProvider.getFile(directory, loader);
			assert sd.isDirectory();
			scope.addToScope(scope.getLoader(AnalysisScope.EXTENSION), new BinaryDirectoryTreeModule(sd));
		}
	
		for (String path : jarDependencies) {
			Module M = FileProvider.getJarFileModule(path, loader);
			scope.addToScope(scope.getLoader(AnalysisScope.APPLICATION), M);
		}
	
		return scope;
	}

	public void addBinaryDependency(String path) {
		binaryDependencies.add(path);
	}

	public void addExtensionBinaryDependency(String path) {
		extensionBinaryDependencies.add(path);
	}

	public void addJarDependency(String file) {
		jarDependencies.add(file);
	}

	public String getJavaClassesJar() {
		String javaHome = System.getProperty("java.home");
		String javaClasses;
		if(javaHome.startsWith("/")) {
			javaClasses = javaHome.substring(0, javaHome.length()-4);
			javaClasses += "Classes/classes.jar";
		} else {
			javaClasses = javaHome.replace("\\", "/");
			javaClasses += "/lib/rt.jar";
		}
		return javaClasses;
	}
}