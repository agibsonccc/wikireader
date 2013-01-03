package com.ccc.sendalyzeit.reader.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.ccc.sendalyzeit.fileloader.FileLoader;
import com.ccc.sendalyzeit.reader.core.DocRetriever;
import com.ccc.sendalyzeit.reader.core.Document;
/**
 * Document retriever: will iterate over directories
 * and automatically iterate to next files as well as directories
 * relative to the root directory 
 * @author agibsonccc
 *
 */
public class DocumentRetriverImpl implements DocRetriever {


	public DocumentRetriverImpl(File rootDir) {
		if( rootDir == null || !rootDir.exists() || !rootDir.isDirectory() )
			throw new IllegalArgumentException( "Root file is doesn't exist, isn't a directory, or is null" );
		//load only directories
		this.subDirs = rootDir.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}

		});
		docs = new ArrayList<Document>();

	}

	//all subdirectories of root folder
	private File[] subDirs;
	//current folder
	private int currDirIndex;
	//current file
	private int currFileIndex;
	//files in current directory
	private File[] currFiles;
	//document buffer
	private List<Document> docs;

	public boolean hasNext() {
		//current file                             current directory                 docs in buffer
		return currFileIndex < currFiles.length || currDirIndex < subDirs.length || !docs.isEmpty();
	}

	public Document next() {
		Document ret = null;
		//remove doc from buffer
		if(!docs.isEmpty()) {
			ret = docs.remove( 0 );
		}
		//load next docs
		else if( currFiles == null || currFileIndex < currFiles.length ) {
			try {
				//load initial files
				if( currFiles == null ) 
					currFiles = subDirs[ currDirIndex ].listFiles();
				
				
				//read next documents in to buffer
				FileLoader loader = new FileLoader( currFiles[ currFileIndex ] );
				docs.addAll( loader.getCompletedDocuments() );
				currFileIndex++;
			} catch (IOException e) {
				throw new RuntimeException( e );
			}
			ret = docs.remove( 0 );

		}
		//load next files
		else if( currDirIndex < subDirs.length ) {
			currFiles = subDirs[ currDirIndex ].listFiles();
			currDirIndex++;
			currFileIndex = 0;

			try {
				FileLoader loader = new FileLoader( currFiles[ currFileIndex ] );
				docs.addAll( loader.getCompletedDocuments() );
				currFileIndex++;
			} catch ( IOException e ) {
				throw new RuntimeException( e );
			}
			ret = docs.remove( 0 );
		}
		return ret;
	}

	public void remove() {
		throw new RuntimeException( " Not implemented " );
	}

	public Document getNextDocument() {
		return next();
	}

}
