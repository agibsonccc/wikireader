package com.ccc.sendalyzeit.reader.test;

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.*;

import com.ccc.sendalyzeit.reader.core.DocRetriever;
import com.ccc.sendalyzeit.reader.core.Document;
import com.ccc.sendalyzeit.reader.impl.DocumentRetriverImpl;

public class DocumentRetrieverTest {

	@Test
	public void testDocumentRetriever() {
		DocRetriever retriever = new DocumentRetriverImpl( new File( "testdir" ) );
		assertEquals(true,retriever.next() != null );
	}
	@Test
	public void testDocumentRetrieverContents() {
		DocRetriever retriever = new DocumentRetriverImpl( new File( "testdir" ) );
		Document next = retriever.next();
		
		assertEquals( true, !next.getText().isEmpty() );
		
	}
}
