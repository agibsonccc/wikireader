package com.ccc.sendalyzeit.fileloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ccc.sendalyzeit.reader.core.Document;
import com.ccc.sendalyzeit.reader.impl.DocumentImpl;

/**
 * This will parse documents from a single file.
 * @author agibsonccc
 *
 */
public class FileLoader {

	public FileLoader( File f ) throws IOException {
	    this( f , null );
	}
	
	public FileLoader( File f, String partialText ) throws IOException {
		this.read = f;
		this.partialText = partialText;
		completedDocuments = new ArrayList<Document>();
		read();
		
	}
	
	public final static String TITLE = "title=\"";
	public final static String ID = "id=\"";
	
	
	
	private File read;
	
	private Document currentDocument;
	
	private String partialText;
	
	private List<Document> completedDocuments;
	
	private static Logger log = LoggerFactory.getLogger(FileLoader.class);
	
	public String getPartialText() {
		return partialText;
	}

	public void setPartialText(String partialText) {
		this.partialText = partialText;
	}

	public List<Document> getCompletedDocuments() {
		return completedDocuments;
	}

	public void setCompletedDocuments(List<Document> completedDocuments) {
		this.completedDocuments = completedDocuments;
	}

	private void read( Reader inputReader ) throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader( inputReader );
		String line = null;
		currentDocument = new DocumentImpl();
		StringBuffer text = new StringBuffer();
		
		while((line = reader.readLine() ) != null ) {
			if( log.isTraceEnabled() ) 
				log.trace( "Reading line " + line );
			
			//beginning of new document, grab id and title
			if( line.contains("<doc") ) {
				int idIndex = line.indexOf( ID ) + ID.length();
				StringBuffer idBuffer = new StringBuffer();
				for(int i = idIndex; i <  line.length(); i++) {
					if( line.charAt( i ) == '\"' )
						break;
					idBuffer.append( line.charAt( i ) );
					
				}
				//grab id
				Long idAppend = Long.parseLong( idBuffer.toString() );
				currentDocument.setId( idAppend );
				
				//grab title
				int titleIndex = line.indexOf( TITLE ) + TITLE.length();
				StringBuffer titleBuffer = new StringBuffer();
				for(int i = titleIndex; i <  line.length(); i++) {
					if( line.charAt( i ) == '\"' )
						break;
					titleBuffer.append( line.charAt( i ) );
					
				}
				currentDocument.setTitle( titleBuffer.toString() );
			}
			//signals completed document
			else if(line.contains( "</doc>" )) {
				line = line.replace( "</doc>", "" );
				text.append( line );
				currentDocument.setText( text.toString() );
				completedDocuments.add( currentDocument );
				
				//reset
				currentDocument = new DocumentImpl();
				text = new StringBuffer();
			} 
			//continue ref
			else if(line.contains( "<ref>" ) || line.contains( "</ref>" ) )
				continue;
			else 
				text.append(line);
			
		}//end while	
		if( !text.toString().isEmpty() ) {
			partialText = ( partialText == null ) ?  text.toString() : partialText + text.toString();
			if( log.isTraceEnabled() )
				log.trace("Partial text " + partialText);
		}
		reader.close();
	}
	
	public void read() throws IOException {
		//compressed
		if( !read.getAbsolutePath().endsWith( "bz2" ) ) 
			read( new FileReader( read ));
		//assume bz2
		else 
			read( new InputStreamReader( new BZip2CompressorInputStream( new FileInputStream( read ) ) ) );
		
	}
}
