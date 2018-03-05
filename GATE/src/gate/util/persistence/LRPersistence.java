/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 26/10/2001
 *
 *  $Id: LRPersistence.java,v 1.1 2011/01/13 16:50:52 textmine Exp $
 *
 */
package gate.util.persistence;

import java.io.Serializable;
import java.util.Map;

import gate.DataStore;
import gate.LanguageResource;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;

public class LRPersistence extends ResourcePersistence {

  /**
   * Populates this Persistence with the data that needs to be stored from the
   * original source object.
   */
  public void extractDataFromSource(Object source)throws PersistenceException{
    //check input
    if(! (source instanceof LanguageResource)){
      throw new UnsupportedOperationException(
                getClass().getName() + " can only be used for " +
                LanguageResource.class.getName() +
                " objects!\n" + source.getClass().getName() +
                " is not a " + LanguageResource.class.getName());
    }

    super.extractDataFromSource(source);
    //LR's will have the features saved by their respective persistence
    //mechanism
    features = null;

    LanguageResource lr = (LanguageResource)source;
    if(lr.getDataStore() == null){
      dsData = null;
    }else{
      dsData = PersistenceManager.
               getPersistentRepresentation(lr.getDataStore());
      persistenceID = lr.getLRPersistenceId();
    }
  }

  /**
   * Creates a new object from the data contained. This new object is supposed
   * to be a copy for the original object used as source for data extraction.
   */
  public Object createObject()throws PersistenceException,
                                     ResourceInstantiationException{
    if(dsData == null) return super.createObject();
    else{
      //persistent doc
      initParams = PersistenceManager.getTransientRepresentation(initParams);

      DataStore ds = (DataStore)PersistenceManager.
                     getTransientRepresentation(dsData);
      ((Map)initParams).put(DataStore.DATASTORE_FEATURE_NAME, ds);
      ((Map)initParams).put(DataStore.LR_ID_FEATURE_NAME, persistenceID);
      return super.createObject();
    }
  }

  protected Serializable dsData;
  protected Object persistenceID;
  static final long serialVersionUID = 3541034274225534363L;
}