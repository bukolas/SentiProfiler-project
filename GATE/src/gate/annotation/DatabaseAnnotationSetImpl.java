/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva 21/10/2001
 *
 *  $Id: DatabaseAnnotationSetImpl.java,v 1.1 2011/01/13 16:52:08 textmine Exp $
 */
package gate.annotation;

import java.util.*;
import junit.framework.Assert;
import gate.*;
import gate.corpora.DatabaseDocumentImpl;
import gate.corpora.DocumentImpl;
import gate.event.*;

// import gate.persist.*;
public class DatabaseAnnotationSetImpl extends AnnotationSetImpl
                                                                implements
                                                                DatastoreListener,
                                                                EventAwareAnnotationSet,
                                                                AnnotationListener {
  /**
   * The listener for the events coming from the document (annotations and
   * annotation sets added or removed).
   */
  // = protected EventsHandler eventHandler;
  protected HashSet<Annotation> addedAnnotations = new HashSet<Annotation>();
  protected HashSet<Annotation> removedAnnotations = new HashSet<Annotation>();
  protected HashSet<Annotation> updatedAnnotations = new HashSet<Annotation>();
  private boolean validating = false;

  public void assertValid() {
    if(validating) return;
    validating = true;
    // avoid recursion
    // doc can't be null
    Assert.assertNotNull(this.doc);
    // doc.assertValid();
    validating = false;
  }

  /** Construction from Document. */
  public DatabaseAnnotationSetImpl(Document doc) {
    super(doc);
    // preconditions
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    // = eventHandler = new EventsHandler();
    // = this.addAnnotationSetListener(eventHandler);
    // add self as listener for sync events from the document's datastore
    // 00 doc.getDataStore().removeDatastoreListener(this);
    doc.getDataStore().addDatastoreListener(this);
    // ((VerboseHashMap)annotsById).setOwner(this);
  } // construction from document

  /** Construction from Document and name. */
  public DatabaseAnnotationSetImpl(Document doc, String name) {
    super(doc, name);
    // preconditions
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    // = eventHandler = new EventsHandler();
    // = this.addAnnotationSetListener(eventHandler);
    // add self as listener for sync events from the document's datastore
    // 00 doc.getDataStore().removeDatastoreListener(this);
    doc.getDataStore().addDatastoreListener(this);
    // ((VerboseHashMap)annotsById).setOwner(this);
  } // construction from document and name

  /** Construction from Document and name. */
  public DatabaseAnnotationSetImpl(Document doc, AnnotationSet c) {
    this(c);
    this.doc = (DocumentImpl)doc;
    // add self as listener for sync events from the document's datastore
    // 00 doc.getDataStore().removeDatastoreListener(this);
    // 00 doc.getDataStore().addDatastoreListener(this);
  } // construction from document and name

  /** Construction from Document and name. */
  public DatabaseAnnotationSetImpl(Document doc, String name, AnnotationSet c) {
    this(doc, c);
    this.name = name;
    // add self as listener for sync events from the document's datastore
    // 00 doc.getDataStore().removeDatastoreListener(this);
    doc.getDataStore().addDatastoreListener(this);
  } // construction from document and name

  /** Construction from Collection (which must be an AnnotationSet) */
  public DatabaseAnnotationSetImpl(AnnotationSet c) throws ClassCastException {
    super(c);
    // also copy the name, because that super one doesn't
    AnnotationSet as = (AnnotationSet)c;
    this.name = as.getName();
    // = eventHandler = new EventsHandler();
    // = this.addAnnotationSetListener(eventHandler);
    Iterator<Annotation> iter = this.iterator();
    while(iter.hasNext())
      iter.next().addAnnotationListener(this);
    Document doc = as.getDocument();
    // add self as listener for sync events from the document's datastore
    // 00 doc.getDataStore().removeDatastoreListener(this);
    doc.getDataStore().addDatastoreListener(this);
    // ((VerboseHashMap)annotsById).setOwner(this);
  } // construction from collection

  public String toString() {
    return super.toString() + "added annots: " + addedAnnotations
            + "removed annots: " + removedAnnotations + "updated annots: "
            + updatedAnnotations;
  }

  // /** Two AnnotationSet are equal if their name, the documents of which
  // belong
  // * to the AnnotationSets and annotations from the sets are the same
  // */
  // public boolean equals(Object other) {
  //
  // if (false == other instanceof DatabaseAnnotationSetImpl) {
  // return super.equals(other);
  // }
  //
  // boolean result = true;
  //
  // if (!super.equals((AnnotationSet)other)) {
  // return false;
  // }
  //
  // DatabaseAnnotationSetImpl target = (DatabaseAnnotationSetImpl)other;
  //
  // result = result && this.addedAnnotations.equals(target.addedAnnotations)
  // && this.removedAnnotations.equals(target.removedAnnotations)
  // && this.updatedAnnotations.equals(target.updatedAnnotations);
  //
  // //FINALLY - CHECK THAT THE SET IS FROM THE SAME DOCUMENT *INSTANCE*
  // //DO *NOT* USE EQUALS()
  // result = result && ( this.getDocument() == target.getDocument());
  //
  // return result;
  // } // equals
  /**
   * All the events from the document or its annotation sets are handled by this
   * inner class.
   */
  /*
   * class EventsHandler implements AnnotationListener AnnotationSetListener{
   * 
   * 
   * public void annotationAdded(gate.event.AnnotationSetEvent e) {
   * AnnotationSet set = (AnnotationSet)e.getSource(); String setName =
   * set.getName(); if (setName != DatabaseAnnotationSetImpl.this.name && !
   * setName.equals(DatabaseAnnotationSetImpl.this.name)) return; Annotation ann =
   * e.getAnnotation(); ann.addAnnotationListener(this);
   * DatabaseAnnotationSetImpl.this.addedAnnotations.add(ann); }
   * 
   * public void annotationRemoved(AnnotationSetEvent e){ AnnotationSet set =
   * (AnnotationSet)e.getSource(); String setName = set.getName(); if (setName !=
   * DatabaseAnnotationSetImpl.this.name && !
   * setName.equals(DatabaseAnnotationSetImpl.this.name)) return; Annotation ann =
   * e.getAnnotation(); ann.removeAnnotationListener(this);
   * 
   * //1. check if this annot is in the newly created annotations set if
   * (addedAnnotations.contains(ann)) { //a new annotatyion that was deleted
   * afterwards, remove it from all sets
   * DatabaseAnnotationSetImpl.this.addedAnnotations.remove(ann); return; } //2.
   * check if the annotation was updated, if so, remove it from the //update
   * list if (updatedAnnotations.contains(ann)) {
   * DatabaseAnnotationSetImpl.this.updatedAnnotations.remove(ann); }
   * 
   * DatabaseAnnotationSetImpl.this.removedAnnotations.add(ann); }
   * 
   * 
   * public void annotationUpdated(AnnotationEvent e){ Annotation ann =
   * (Annotation) e.getSource();
   * 
   * //check if the annotation is newly created //if so, do not add it to the
   * update list, since it was not stored in the //database yet, so the most
   * recent value will be inserted into the DB upon //DataStore::sync() if
   * (addedAnnotations.contains(ann)) { return; }
   * 
   * DatabaseAnnotationSetImpl.this.updatedAnnotations.add(ann); }
   * 
   * }//inner class EventsHandler
   * 
   */
  /**
   * Called by a datastore when a new resource has been adopted
   */
  public void resourceAdopted(DatastoreEvent evt) {
    Assert.assertNotNull(evt);
    Assert.assertNotNull(evt.getResourceID());
    // check if this is our resource
    // rememeber - a data store handles many resources
    if(evt.getResourceID().equals(this.doc.getLRPersistenceId())) {
      // System.out.println("ASNAME=["+this.getName()+"], resourceAdopted()
      // called");
      // we're synced wtith the DB now
      clearChangeLists();
    }
  }

  /**
   * Called by a datastore when a resource has been deleted
   */
  public void resourceDeleted(DatastoreEvent evt) {
    Assert.assertNotNull(evt);
    Assert.assertNotNull(evt.getResourceID());
    // check if this is our resource
    // rememeber - a data store handles many resources
    if(evt.getResourceID().equals(this.doc.getLRPersistenceId())) {
      // System.out.println("ASNAME=["+this.getName()+"],resourceDeleted()
      // called");
      // unregister self
      // this is not the correct way, since the resource is null in this case
      // DataStore ds = (DataStore)evt.getResource();
      DataStore ds = this.doc.getDataStore();
      if(ds != null) ds.removeDatastoreListener(this);
    }
  }// resourceDeleted

  /**
   * Called by a datastore when a resource has been wrote into the datastore
   */
  public void resourceWritten(DatastoreEvent evt) {
    Assert.assertNotNull(evt);
    Assert.assertNotNull(evt.getResourceID());
    // check if this is our resource
    // rememeber - a data store handles many resources
    if(evt.getResourceID().equals(this.doc.getLRPersistenceId())) {
      // System.out.println("ASNAME=["+this.getName()+"],resourceWritten()
      // called");
      // clear lists with updates - we're synced with the DB
      clearChangeLists();
    }
  }

  private void clearChangeLists() {
    // ok, we're synced now, clear all lists with changed IDs
    synchronized(this) {
      // System.out.println("clearing lists...");
      this.addedAnnotations.clear();
      this.updatedAnnotations.clear();
      this.removedAnnotations.clear();
    }
  }

  public Collection<Annotation> getAddedAnnotations() {
    // System.out.println("getAddedIDs() called");
    HashSet<Annotation> result = new HashSet<Annotation>();
    result.addAll(this.addedAnnotations);
    return result;
  }

  public Collection<Annotation> getChangedAnnotations() {
    // System.out.println("getChangedIDs() called");
    HashSet<Annotation> result = new HashSet<Annotation>();
    result.addAll(this.updatedAnnotations);
    return result;
  }

  public Collection<Annotation> getRemovedAnnotations() {
    // System.out.println("getremovedIDs() called...");
    HashSet<Annotation> result = new HashSet<Annotation>();
    result.addAll(this.removedAnnotations);
    return result;
  }

  public void annotationUpdated(AnnotationEvent e) {
    Annotation ann = (Annotation)e.getSource();
    // check if the annotation is newly created
    // if so, do not add it to the update list, since it was not stored in the
    // database yet, so the most recent value will be inserted into the DB upon
    // DataStore::sync()
    if(false == this.addedAnnotations.contains(ann)) {
      this.updatedAnnotations.add(ann);
    }
    // sanity check
    Assert.assertTrue(false == this.removedAnnotations.contains(ann));
  }

  /** Add an existing annotation. Returns true when the set is modified. */
  public boolean add(Annotation o) throws ClassCastException {
    // check if this annotation was removed beforehand
    // if so then just delete it from the list of annotations waiting for
    // persistent removal
    if(this.removedAnnotations.contains(o)) {
      this.removedAnnotations.remove(o);
    }
    boolean result = super.add(o);
    if(true == result) {
      // register as listener for update events from this annotation
      o.addAnnotationListener(this);
      // add to the newly created annotations set
      this.addedAnnotations.add(o);
    }
    return result;
  }

  /**
   * 
   * @param e
   */
  protected void fireAnnotationRemoved(AnnotationSetEvent e) {
    if(annotationSetListeners != null) {
      Vector listeners = annotationSetListeners;
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        ((AnnotationSetListener)listeners.elementAt(i)).annotationRemoved(e);
      }
    }
  }

  /** Remove an element from this set. */
  public boolean remove(Object o) throws ClassCastException {
    boolean result = super.remove(o);
    if(true == result) {
      // UNregister as listener for update events from this annotation
      Annotation ann = (Annotation)o;
      ann.removeAnnotationListener(this);
      // 1. check if this annot is in the newly created annotations set
      if(this.addedAnnotations.contains(ann)) {
        // a new annotation that was deleted afterwards, remove it from all sets
        this.addedAnnotations.remove(ann);
      } else {
        // 2. check if the annotation was updated, if so, remove it from the
        // update list
        if(this.updatedAnnotations.contains(ann)) {
          this.updatedAnnotations.remove(ann);
        }
        // 3. add to the list with deleted anns
        this.removedAnnotations.add(ann);
      }
    }
    return result;
  }

  public Iterator<Annotation> iterator() {
    return new DatabaseAnnotationSetIterator();
  }

  class DatabaseAnnotationSetIterator
                                     extends
                                       AnnotationSetImpl.AnnotationSetIterator {
    public void remove() {
      super.remove();
      Annotation annRemoved = lastNext;
      // UNregister as listener for update events from this annotation
      annRemoved.removeAnnotationListener(DatabaseAnnotationSetImpl.this);
      // 1. check if this annot is in the newly created annotations set
      if(DatabaseAnnotationSetImpl.this.addedAnnotations.contains(annRemoved)) {
        // a new annotation that was deleted afterwards, remove it from all sets
        DatabaseAnnotationSetImpl.this.addedAnnotations.remove(annRemoved);
      } else {
        // 2. check if the annotation was updated, if so, remove it from the
        // update list
        if(DatabaseAnnotationSetImpl.this.updatedAnnotations
                .contains(annRemoved)) {
          DatabaseAnnotationSetImpl.this.updatedAnnotations.remove(annRemoved);
        }
        // 3. add to the list with deleted anns
        DatabaseAnnotationSetImpl.this.removedAnnotations.add(annRemoved);
      }
    }
  }
}