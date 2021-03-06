/*
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 22/Mar/2002
 *
 *  $Id: persist_create_corpus.sp 8929 2007-07-12 16:49:55Z ian_roberts $
 *
 */

CREATE OR REPLACE FUNCTION persist_create_corpus(int4) RETURNS int4 AS '

   DECLARE
      p_lr_id alias for $1;

   BEGIN
      insert into t_corpus(corp_id,
                           corp_lr_id)
      values (nextval(''seq_corpus''),
              p_lr_id);

      return currval(''seq_corpus'');

   END;
'
LANGUAGE 'plpgsql';
