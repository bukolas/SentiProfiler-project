/*
 *  generateGrants.sql
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 28/Sep/2001
 *
 *  $Id: generateGrants.sql 12006 2009-12-01 17:24:28Z thomas_heitz $
 */

 
spool result.sql;
/

 
select 'grant select,insert,update,delete on ' || owner || '.' || table_name || ' to GATEUSER;' 
as xx
from sys.all_tables
where owner='GATEADMIN'
union select 'grant select on ' || sequence_owner || '.' || sequence_name || ' to GATEUSER;' 
as xx
from sys.all_sequences
where sequence_owner='GATEADMIN'
union select 'grant select on ' || owner || '.' || view_name || ' to GATEUSER;' 
as xx
from sys.all_views
where owner='GATEADMIN'
union select 'grant execute on ' || owner || '.' || object_name || ' to GATEUSER;' 
as xx
from sys.all_objects
where owner='GATEADMIN'
      and object_type = 'PACKAGE';
/


spool off;
/