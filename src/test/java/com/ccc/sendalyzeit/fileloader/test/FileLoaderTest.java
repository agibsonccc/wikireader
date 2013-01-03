package com.ccc.sendalyzeit.fileloader.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.ccc.sendalyzeit.fileloader.FileLoader;

public class FileLoaderTest {
	@Test
	public void loadFileTest() throws IOException {
		FileLoader loader = new FileLoader( new File( "wiki_00.bz2" ) );
		assertEquals(true,!loader.getCompletedDocuments().isEmpty() );
		assertEquals(true,( loader.getPartialText()==null ) );
		assertEquals(6 ,loader.getCompletedDocuments().size() );
	}

 }
