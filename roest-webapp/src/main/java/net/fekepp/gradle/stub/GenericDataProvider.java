package net.fekepp.gradle.stub;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;

//@ApplicationPath("/")
public class GenericDataProvider extends ResourceConfig {

	public static final File PATH_DATA = new File(GenericDataProvider.class
			.getResource("/data").getPath());

	public GenericDataProvider() {

		// String path =
		// GenericDataProvider.class.getResource("/data").getPath();
		// if (path != null) {
		// PATH_DATA = new File(path);
		// } else {
		// PATH_DATA = new File(
		// "/home/fekepp/local/data/workspaces/eclipse/lunar/local.fekepp.gddp/data");
		// }
		// System.out.println("PAAAAAAAAATHHHHH" + PATH_DATA);

		// Scan packages for servlets
		packages("net.fekepp.gddb.servlets");

		// // List all sub-directories recursively
		// Collection<File> directories = FileUtils.listFilesAndDirs(
		// dataPath, DirectoryFileFilter.INSTANCE,
		// TrueFileFilter.INSTANCE);

		// List all direct sub-directories
		// Collection<File> directories = FileUtils.listFilesAndDirs(dataPath,
		// DirectoryFileFilter.INSTANCE, null);

		// File[] directories = dataPath
		// .listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		//
		// final StringBuffer stringBuffer = new StringBuffer();
		//
		// stringBuffer.append("Listing:<br />");
		//
		// for (File directory : directories) {
		// stringBuffer.append(directory.getName());
		// stringBuffer.append("<br />");
		// stringBuffer.append(directory.getPath());
		// stringBuffer.append("<br />");
		// stringBuffer.append("<br />");
		// }

		// Create a resource builder
		// final Resource.Builder resourceBuilder = Resource.builder();
		// resourceBuilder.path("/");

		// // Create a method builder for GET
		// final ResourceMethod.Builder methodBuilder = resourceBuilder
		// .addMethod("GET");
		// methodBuilder.produces(MediaType.TEXT_HTML).handledBy(
		// new Inflector<ContainerRequestContext, String>() {
		// @Override
		// public String apply(
		// ContainerRequestContext containerRequestContext) {
		//
		// return stringBuffer.toString();
		// }
		// });

		// for (File directory : directories) {
		//
		// directory.getName().startsWith("~");
		//
		// Resource.Builder childResourceBuilder = resourceBuilder
		// .addChildResource(directory.getName());
		//
		// ResourceMethod.Builder childMethodBuilder = childResourceBuilder
		// .addMethod("GET");
		//
		// final String directoryPath = directory.getAbsolutePath();
		//
		// childMethodBuilder.produces(MediaType.TEXT_HTML).handledBy(
		// new Inflector<ContainerRequestContext, String>() {
		// @Override
		// public String apply(
		// ContainerRequestContext containerRequestContext) {
		//
		// return "~"
		// + MediaType.valueOf("text/html").toString()
		// .replace("/", "~") + " > "
		// + MediaType.TEXT_HTML.toString() + " > "
		// + directoryPath;
		// }
		// });
		// }

		// Recursively build the interface for the data directory
		buildRecursiveInterface(PATH_DATA, "");

		// // Build resource
		// final Resource resource = resourceBuilder.build();
		//
		// // Register resource
		// registerResources(resource);

		// final Resource.Builder resourceBuilder =
		// Resource.builder("helloworld");
		//
		// final Resource.Builder childResource =
		// resourceBuilder.addChildResource("subresource");
		// childResource.addMethod("GET").handledBy(new GetInflector());
		//
		// final Resource resource = resourceBuilder.build();

	}

	private void buildRecursiveInterface(File root, final String pathRelative) {

		// Return if file path is not a directory
		if (!root.isDirectory()) {
			return;
		}

		// String name = root.getName();

		final String pathAbsolute = root.getAbsolutePath();

		final File[] directories = root
				.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);

		final File[] files = root.listFiles((FileFilter) FileFileFilter.FILE);

		// Sort directories and files
		Arrays.sort(directories);
		Arrays.sort(files);

		final Resource.Builder resourceBuilder = Resource.builder();
		resourceBuilder.path(pathRelative);

		// directory.getName().startsWith("~");

		// Resource.Builder childResourceBuilder = resourceBuilder
		// .addChildResource(directory.getName());

		ResourceMethod.Builder childMethodBuilder = resourceBuilder
				.addMethod("GET");

		childMethodBuilder.produces(MediaType.TEXT_HTML).handledBy(
				new Inflector<ContainerRequestContext, String>() {

					@Override
					public String apply(
							ContainerRequestContext containerRequestContext) {

						return directoriesAndFilesToString(pathAbsolute,
								pathRelative,
								containerRequestContext.getUriInfo());

						// return "~"
						// + MediaType.valueOf("text/html").toString()
						// .replace("/", "~") + " > "
						// + MediaType.TEXT_HTML.toString() + " > "
						// + directoryPath;
					}

				});

		// Build resource
		final Resource resource = resourceBuilder.build();

		// Register resource
		registerResources(resource);

		// Recursively build the interface for all sub-directories
		for (File subDirectory : directories) {
			buildRecursiveInterface(subDirectory,
					pathRelative + (pathRelative.equals("") ? "" : "/")
							+ subDirectory.getName());
		}

	}

	private String directoriesAndFilesToString(String pathAbsolute,
			String pathRelative, UriInfo uriInfo) {

		File root = new File(pathAbsolute);

		String name = root.getName();

		File[] directories = root
				.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);

		File[] files = root.listFiles((FileFilter) FileFileFilter.FILE);

		Arrays.sort(directories);
		Arrays.sort(files);

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append("URI:<br />");
		stringBuffer.append("uriInfo.getBaseUri() > ").append(
				uriInfo.getBaseUri());
		stringBuffer.append("<br />");
		stringBuffer.append("uriInfo.getRequestUri() > ").append(
				uriInfo.getRequestUri());
		stringBuffer.append("<br />");
		stringBuffer.append("<br />");

		stringBuffer.append("CURRENT:<br />");
		stringBuffer.append(name);
		stringBuffer.append("<br />");
		stringBuffer.append(pathRelative);
		stringBuffer.append("<br />");
		stringBuffer.append(pathAbsolute);

		stringBuffer.append("<br />");
		stringBuffer.append("<br />");
		stringBuffer.append("DIRECTORIES:<br />");

		for (File subDirectory : directories) {
			stringBuffer
					.append("<a href=\"")
					.append(uriInfo.getRequestUri() + "/"
							+ subDirectory.getName()).append("\">")
					.append(subDirectory.getName()).append("</a>");
			stringBuffer.append("<br />");
			stringBuffer.append(subDirectory.getPath());
			stringBuffer.append("<br />");
			stringBuffer.append("<br />");
		}

		if (directories.length == 0) {
			stringBuffer.append("<br />");
		}

		stringBuffer.append("FILES:<br />");

		for (File file : files) {
			stringBuffer.append(file.getName());
			stringBuffer.append("<br />");
			stringBuffer.append(file.getPath());
			stringBuffer.append("<br />");
			stringBuffer.append("<br />");
		}

		return stringBuffer.toString();
	}

}