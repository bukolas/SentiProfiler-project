/*
 *  SearchableDataStore.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: SearchableDataStore.java,v 1.1 2011/01/13 16:52:15 textmine Exp $
 */
package gate.creole.annic;

import gate.DataStore;

/**
 * Datastores want to become indexable and searchable should implement this interface.
 * @author niraj
 */
public interface SearchableDataStore extends Searchable, DataStore {
  // it doesn't specify its own methods, may be later when recognized
}
