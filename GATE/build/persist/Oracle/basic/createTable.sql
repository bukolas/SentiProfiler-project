/*
 *  DDL script for Oracle 8.x and Oracle 9.x
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 19/Sep/2001
 *
 *  auto generated: Thu Jan 31 14:10:12 2002
 *
 *  $Id: createTable.sql 12006 2009-12-01 17:24:28Z thomas_heitz $
 *
 */



DROP TABLE T_FEATURE_KEY CASCADE CONSTRAINTS;

CREATE TABLE T_FEATURE_KEY (
       FK_ID		    NUMBER NOT NULL,
       FK_STRING	    VARCHAR2(128) NOT NULL,
       CONSTRAINT XPKT_FEATURE_KEY
	      PRIMARY KEY (FK_ID)
)
	 PCTFREE 0
;


DROP TABLE T_USER CASCADE CONSTRAINTS;

CREATE TABLE T_USER (
       USR_ID		    NUMBER NOT NULL,
       USR_LOGIN	    VARCHAR2(16) NOT NULL,
       USR_PASS 	    VARCHAR2(16) NOT NULL,
       CONSTRAINT XPKT_USER
	      PRIMARY KEY (USR_ID)
)
;


DROP TABLE T_GROUP CASCADE CONSTRAINTS;

CREATE TABLE T_GROUP (
       GRP_ID		    NUMBER NOT NULL,
       GRP_NAME 	    VARCHAR2(128) NULL,
       CONSTRAINT XPKT_GROUP
	      PRIMARY KEY (GRP_ID)
)
;


DROP TABLE T_USER_GROUP CASCADE CONSTRAINTS;

CREATE TABLE T_USER_GROUP (
       UGRP_ID		    NUMBER NOT NULL,
       UGRP_USER_ID	    NUMBER NULL,
       UGRP_GROUP_ID	    NUMBER NULL,
       CONSTRAINT XPKT_USER_GROUP
	      PRIMARY KEY (UGRP_ID),
       CONSTRAINT member_of
	      FOREIGN KEY (UGRP_USER_ID)
			     REFERENCES T_USER,
       CONSTRAINT group_of
	      FOREIGN KEY (UGRP_GROUP_ID)
			     REFERENCES T_GROUP
)
;


DROP TABLE T_DOC_ENCODING CASCADE CONSTRAINTS;

CREATE TABLE T_DOC_ENCODING (
       ENC_ID		    NUMBER NOT NULL,
       ENC_NAME 	    VARCHAR2(16) NOT NULL,
       CONSTRAINT XPKT_DOC_ENCODING
	      PRIMARY KEY (ENC_ID)
)
  PCTFREE 0
;


DROP TABLE T_DOC_CONTENT CASCADE CONSTRAINTS;

CREATE TABLE T_DOC_CONTENT (
       DC_ID		    NUMBER NOT NULL,
       DC_ENCODING_ID	    NUMBER NULL,
       DC_CHARACTER_CONTENT CLOB NULL,
       DC_BINARY_CONTENT    BLOB NULL,
       DC_CONTENT_TYPE	    NUMBER NOT NULL,
       CONSTRAINT XPKT_DOC_CONTENT
	      PRIMARY KEY (DC_ID),
       FOREIGN KEY (DC_ENCODING_ID)
			     REFERENCES T_DOC_ENCODING
)
   PCTFREE 0
;


DROP TABLE T_FEATURE CASCADE CONSTRAINTS;

CREATE TABLE T_FEATURE (
       FT_ID		    NUMBER NOT NULL,
       FT_ENTITY_ID	    NUMBER NOT NULL,
       FT_ENTITY_TYPE	    NUMBER NOT NULL,
       FT_KEY_ID	    NUMBER NOT NULL,
       FT_NUMBER_VALUE	    NUMBER NULL,
       FT_BINARY_VALUE	    BLOB NULL,
       FT_CHARACTER_VALUE   VARCHAR2(4000) NULL,
       FT_LONG_CHARACTER_VALUE CLOB NULL,
       FT_VALUE_TYPE	    NUMBER NOT NULL,
       CONSTRAINT XPKT_FEATURE
	      PRIMARY KEY (FT_ID),
       FOREIGN KEY (FT_KEY_ID)
			     REFERENCES T_FEATURE_KEY
)
	 PCTFREE 0
;


DROP TABLE T_LR_TYPE CASCADE CONSTRAINTS;

CREATE TABLE T_LR_TYPE (
       LRTP_ID		    NUMBER NOT NULL,
       LRTP_TYPE	    VARCHAR2(128) NOT NULL,
       CONSTRAINT XPKT_LR_TYPE
	      PRIMARY KEY (LRTP_ID)
)
;


DROP TABLE T_LANG_RESOURCE CASCADE CONSTRAINTS;

CREATE TABLE T_LANG_RESOURCE (
       LR_ID		    NUMBER NOT NULL,
       LR_OWNER_USER_ID     NUMBER NULL,
       LR_OWNER_GROUP_ID    NUMBER NULL,
       LR_LOCKING_USER_ID   NUMBER NULL,
       LR_TYPE_ID	    NUMBER NOT NULL,
       LR_NAME		    VARCHAR2(128) NOT NULL,
       LR_ACCESS_MODE	    NUMBER NOT NULL,
       LR_PARENT_ID	    NUMBER NULL,
       CONSTRAINT XPKT_LANG_RESOURCE
	      PRIMARY KEY (LR_ID),
       CONSTRAINT ownerOd
	      FOREIGN KEY (LR_OWNER_USER_ID)
			     REFERENCES T_USER,
       CONSTRAINT lockedBy
	      FOREIGN KEY (LR_LOCKING_USER_ID)
			     REFERENCES T_USER,
       CONSTRAINT ownerOf
	      FOREIGN KEY (LR_OWNER_GROUP_ID)
			     REFERENCES T_GROUP,
       CONSTRAINT hasChild
	      FOREIGN KEY (LR_PARENT_ID)
			     REFERENCES T_LANG_RESOURCE
)
;


DROP TABLE T_DOCUMENT CASCADE CONSTRAINTS;

CREATE TABLE T_DOCUMENT (
       DOC_ID		    NUMBER NOT NULL,
       DOC_CONTENT_ID	    NUMBER NULL,
       DOC_LR_ID	    NUMBER NOT NULL,
       DOC_URL		    VARCHAR2(4000) NULL,
       DOC_START	    NUMBER NULL,
       DOC_END		    NUMBER NULL,
       DOC_IS_MARKUP_AWARE  NUMBER(1) NOT NULL,
       CONSTRAINT XPKT_DOCUMENT
	      PRIMARY KEY (DOC_ID),
       CONSTRAINT is_content_of
	      FOREIGN KEY (DOC_CONTENT_ID)
			     REFERENCES T_DOC_CONTENT,
       FOREIGN KEY (DOC_LR_ID)
			     REFERENCES T_LANG_RESOURCE
)
   PCTFREE 0
;


DROP TABLE T_NODE CASCADE CONSTRAINTS;

CREATE TABLE T_NODE (
       NODE_GLOBAL_ID	    NUMBER NOT NULL,
       NODE_DOC_ID	    NUMBER NOT NULL,
       NODE_LOCAL_ID	    NUMBER NOT NULL,
       NODE_OFFSET	    NUMBER NOT NULL,
       CONSTRAINT XPKT_NODE
	      PRIMARY KEY (NODE_GLOBAL_ID),
       CONSTRAINT hasNodes
	      FOREIGN KEY (NODE_DOC_ID)
			     REFERENCES T_DOCUMENT
)
	 PCTFREE 0
;


DROP TABLE T_ANNOTATION_TYPE CASCADE CONSTRAINTS;

CREATE TABLE T_ANNOTATION_TYPE (
       AT_ID		    NUMBER NOT NULL,
       AT_NAME		    VARCHAR2(128) NULL,
       CONSTRAINT XPKT_ANNOTATION_TYPE
	      PRIMARY KEY (AT_ID)
)
	 PCTFREE 0
;


DROP TABLE T_ANNOTATION CASCADE CONSTRAINTS;

CREATE TABLE T_ANNOTATION (
       ANN_GLOBAL_ID	    NUMBER NOT NULL,
       ANN_DOC_ID	    NUMBER NULL,
       ANN_LOCAL_ID	    NUMBER NOT NULL,
       ANN_AT_ID	    NUMBER NOT NULL,
       ANN_STARTNODE_ID     NUMBER NOT NULL,
       ANN_ENDNODE_ID	    NUMBER NOT NULL,
       CONSTRAINT XPKT_ANNOTATION
	      PRIMARY KEY (ANN_GLOBAL_ID),
       CONSTRAINT hasAnnotations
	      FOREIGN KEY (ANN_DOC_ID)
			     REFERENCES T_DOCUMENT,
       FOREIGN KEY (ANN_STARTNODE_ID)
			     REFERENCES T_NODE,
       FOREIGN KEY (ANN_ENDNODE_ID)
			     REFERENCES T_NODE,
       FOREIGN KEY (ANN_AT_ID)
			     REFERENCES T_ANNOTATION_TYPE
)
	 PCTFREE 0
;


DROP TABLE T_ANNOT_SET CASCADE CONSTRAINTS;

CREATE TABLE T_ANNOT_SET (
       AS_ID		    NUMBER NOT NULL,
       AS_DOC_ID	    NUMBER NOT NULL,
       AS_NAME		    VARCHAR2(128) NULL,
       CONSTRAINT XPKT_ANNOT_SET
	      PRIMARY KEY (AS_ID),
       CONSTRAINT has_ASet_s_
	      FOREIGN KEY (AS_DOC_ID)
			     REFERENCES T_DOCUMENT
)
;


DROP TABLE T_AS_ANNOTATION CASCADE CONSTRAINTS;

CREATE TABLE T_AS_ANNOTATION (
       ASANN_ID 	    NUMBER NOT NULL,
       ASANN_ANN_ID	    NUMBER NOT NULL,
       ASANN_AS_ID	    NUMBER NOT NULL,
       CONSTRAINT XPKT_AS_ANNOTATION
	      PRIMARY KEY (ASANN_ID),
       FOREIGN KEY (ASANN_ANN_ID)
			     REFERENCES T_ANNOTATION,
       CONSTRAINT has_annotations
	      FOREIGN KEY (ASANN_AS_ID)
			     REFERENCES T_ANNOT_SET
)
	 PCTFREE 0
;


DROP TABLE T_CORPUS CASCADE CONSTRAINTS;

CREATE TABLE T_CORPUS (
       CORP_ID		    NUMBER NOT NULL,
       CORP_LR_ID	    NUMBER NOT NULL,
       CONSTRAINT XPKT_CORPUS
	      PRIMARY KEY (CORP_ID),
       FOREIGN KEY (CORP_LR_ID)
			     REFERENCES T_LANG_RESOURCE
)
;


DROP TABLE T_CORPUS_DOCUMENT CASCADE CONSTRAINTS;

CREATE TABLE T_CORPUS_DOCUMENT (
       CD_ID		    NUMBER NOT NULL,
       CD_CORP_ID	    NUMBER NOT NULL,
       CD_DOC_ID	    NUMBER NOT NULL,
       CONSTRAINT XPKT_CORPUS_DOCUMENT
	      PRIMARY KEY (CD_ID),
       CONSTRAINT fk_coprus_cd
	      FOREIGN KEY (CD_CORP_ID)
			     REFERENCES T_CORPUS,
       FOREIGN KEY (CD_DOC_ID)
			     REFERENCES T_DOCUMENT
)
	 PCTFREE 0
;


DROP TABLE T_PARAMETER CASCADE CONSTRAINTS;

CREATE TABLE T_PARAMETER (
       PAR_ID		    NUMBER NOT NULL,
       PAR_KEY		    VARCHAR2(16) NOT NULL,
       PAR_VALUE_STRING     VARCHAR2(128) NULL,
       PAR_VALUE_DATE	    DATE NULL,
       PAR_VALUE_NUMBER     NUMBER NULL,
       CONSTRAINT XPKT_PARAMETER
	      PRIMARY KEY (PAR_ID)
);




