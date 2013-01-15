package com.ccc.sendalyzeit.reader.processor.test;

import java.io.File;

import org.junit.Test;

import com.ccc.sendalyzeit.reader.core.Document;
import com.ccc.sendalyzeit.reader.processor.DocProcessor;

public class DocProcessorTest {

	@Test
	public void testDocProcessor() {
		TestDocProcessor test = new TestDocProcessor( new File( "testdir" ) );
		test.addDocs(-1);
		test.shutdown();
	}
	
	private class TestDocProcessor extends DocProcessor {

		public TestDocProcessor(File rootDir) {
			super(rootDir);
		}

		@Override
		public void processDocument(Document document) {
			System.out.println(document.getTitle());
			System.out.println(document.getText());
		}
		
	}
}
