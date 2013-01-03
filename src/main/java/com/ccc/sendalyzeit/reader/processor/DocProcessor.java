package com.ccc.sendalyzeit.reader.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ccc.sendalyzeit.reader.core.DocRetriever;
import com.ccc.sendalyzeit.reader.core.Document;
import com.ccc.sendalyzeit.reader.impl.DocumentRetriverImpl;
/**
 * Implements a basic parallel executor for
 * processing documents
 * @author agibsonccc
 *
 */
public abstract class DocProcessor {
	/**
	 * Runs the doc processor with a max concurrent of
	 * 5 threads
	 * @param rootDir
	 */
	public DocProcessor( File rootDir ) {
		this( rootDir,5 );
	}
	public DocProcessor( File rootDir,int maxConcurrent ) {
		docRetriever = new DocumentRetriverImpl( rootDir );
		executor = Executors.newCachedThreadPool();
		this.maxConcurrent = maxConcurrent;
		currentFutures = new HashSet<Future<?>>();
		//runs a futures thread that will run every 10 seconds that will clear out any futures
		//that need removing
		futuresMaintainer = Executors.newScheduledThreadPool(5);
		futuresMaintainer.scheduleAtFixedRate(new LockMaintainer(), 0, 10, TimeUnit.SECONDS);
		processed = new HashSet<String>();
	}
	/**
	 * Handles iterating of documents
	 */
	protected DocRetriever docRetriever;
	/**
	 * Handles processing tasks
	 */
	protected ExecutorService executor;
	private static Logger log = LoggerFactory.getLogger( DocProcessor.class );
	/* Collection of futures */
	private Collection<Future<?>> currentFutures;
	/* Max concurrent document processing threads */
	protected int maxConcurrent;
	/* semaphore */
	protected final Object lock = new Object();
	/* thread pool for handling clearing of futures */
	protected final ScheduledExecutorService futuresMaintainer;
	/* a set of titles to ensure no duplicate documents are processed by parallel threads: worse case scenario is around 4MM strings in memory */
	private Set<String> processed;
	/**
	 * Enqueue a set number of documents for processing.
	 * Note that this method will block if called excessively.
	 * When the current futures running is > the number of 
	 * max allowed current threads, it will wait.
	 * Pass a number < 0 to iterate over everything
	 * @param numDocs the number of docs to enqueue
	 */
	public void addDocs( int numDocs ) {
		//wait till some futures are cleared out
		if( currentFutures.size() >= maxConcurrent ) {
			synchronized( lock ) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

		}
		int count = 0;
		while( docRetriever.hasNext() || count < numDocs ) {
			//wait till some futures are cleared out
			if( currentFutures.size() >= maxConcurrent ) {
				synchronized( lock ) {
					try {
						lock.wait();

					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}

			}
			count++;
			Future<?> future = executor.submit(new Runnable() {

				public void run() {
					Document next = docRetriever.next();

					if( log.isDebugEnabled() ) 
						log.debug( " Added document " + next.getTitle() );
					//ensure only one thread at a time
					synchronized( processed ) {
						if(! processed.contains(next.getTitle() ))
							processDocument( next );
					}

					//ensure no duplicates processed
					processed.add( next.getTitle() );

					synchronized( lock ) {
						lock.notify();
					}
				}

			});
			//already complete
			if( future != null )
				currentFutures.add( future );
		}
		shutdown();
	}
	/**
	 * Shutsdown the executors
	 */
	public void shutdown() {
		log.info( "Shutting down..." );
		executor.shutdownNow();
		futuresMaintainer.shutdownNow();

	}
	public abstract void processDocument(Document document);


	private final class LockMaintainer implements Runnable {

		public void run() {
			Collection<Future<?>> remove = new ArrayList<Future<?>>();
			for(Future<?> future : currentFutures ) {
				if( future.isDone() ) 
					remove.add( future );

			}
			currentFutures.removeAll( remove );
			synchronized( lock ) {
				lock.notifyAll();
			}


		}
	}
}
