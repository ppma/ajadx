package jadx.core.xmlgen;

import android.graphics.Bitmap;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import jadx.api.ResourceFile;
import jadx.api.ResourceType;
import jadx.core.codegen.CodeWriter;

import static jadx.core.utils.files.FileUtils.prepareFile;

public class ResourcesSaver implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(ResourcesSaver.class);

	private final ResourceFile resourceFile;
	private File outDir;

	public ResourcesSaver(File outDir, ResourceFile resourceFile) {
		this.resourceFile = resourceFile;
		this.outDir = outDir;
	}

	@Override
	public void run() {
		if (!ResourceType.isSupportedForUnpack(resourceFile.getType())) {
			return;
		}
		ResContainer rc = resourceFile.loadContent();
		if (rc != null) {
			saveResources(rc);
		}
	}

	private void saveResources(ResContainer rc) {
		if (rc == null) {
			return;
		}
		List<ResContainer> subFiles = rc.getSubFiles();
		if (subFiles.isEmpty()) {
			save(rc, outDir);
		} else {
			for (ResContainer subFile : subFiles) {
				saveResources(subFile);
			}
		}
	}
	public boolean write(Bitmap image, String ext, File outFile) throws IOException{

		try {
			FileOutputStream outputStream = new FileOutputStream(outFile);
			if (image.compress(formatExt(ext), 100, outputStream)) {
				outputStream.flush();
				outputStream.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw e;
		}
		return true;
	}

	public Bitmap.CompressFormat formatExt(String ext){
		switch (ext){
			case "jpg": return Bitmap.CompressFormat.JPEG;
			case "png": return Bitmap.CompressFormat.PNG;
			case "gif": return Bitmap.CompressFormat.WEBP;
		}
		return null;
	}

	private void save(ResContainer rc, File outDir) {
		File outFile = new File(outDir, rc.getFileName());
		Bitmap image = rc.getImage();
		if (image != null) {
			String ext = FilenameUtils.getExtension(outFile.getName());
			try {
				outFile = prepareFile(outFile);
				write(image, ext, outFile);
			} catch (IOException e) {
				LOG.error("Failed to save image: {}", rc.getName(), e);
			}
			return;
		}
		CodeWriter cw = rc.getContent();
		if (cw != null) {
			cw.save(outFile);
			return;
		}
		LOG.warn("Resource '{}' not saved, unknown type", rc.getName());
	}


}
