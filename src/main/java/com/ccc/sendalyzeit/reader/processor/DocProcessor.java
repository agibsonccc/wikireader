package com.ccc.sendalyzeit.reader.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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
		currentFutures = new ArrayList<Future<?>>();
		//runs a futures thread that will run every 10 seconds that will clear out any futures
		//that need removing
		futuresMaintainer = Executors.newScheduledThreadPool(5);
		futuresMaintainer.scheduleAtFixedRate(new LockMaintainer(), 0, 10, TimeUnit.SECONDS);
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
			final Document next = docRetriever.next();
			count++;
			Future<?> future = executor.submit(new Runnable() {

				public void run() {
					if( log.isDebugEnabled() ) 
						log.debug( " Added document " + next.getTitle() );

					processDocument( next );
				}

			});
			//already complete
			if( future != null )
				currentFutures.add( future );
		}
	}
    /**
     * Shutsdown the executors
     */
	public void shutdown() {
		if( docRetriever.hasNext() ) {
			log.warn("Jobs not finished, shutting down ");
			executor.shutdownNow();
			futuresMaintainer.shutdownNow();
		}
		else {
			try {
				if( !executor.awaitTermination(10000, TimeUnit.SECONDS) ) {
					log.warn( "Couldn't shutdown..." );
					executor.shutdownNow();
				}
				if( !futuresMaintainer.awaitTermination(10000, TimeUnit.SECONDS) ) {
					log.warn( "Couldn't shutdown..." );
					futuresMaintainer.shutdownNow();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	public abstract void processDocument(Document document);


	private final class LockMaintainer implements Runnable {

		public void run() {
			while( !currentFutures.isEmpty() || currentFutures.size() < maxConcurrent ) {
				Collection<Future<?>> remove = new ArrayList<Future<?>>();
				for(Future<?> future : currentFutures ) {
					if( future.isDone() ) 
						remove.add( future );

				}
				currentFutures.removeAll( remove );
				synchronized( lock ) {
					lock.notify();
				}

			}
		}
	}
}
