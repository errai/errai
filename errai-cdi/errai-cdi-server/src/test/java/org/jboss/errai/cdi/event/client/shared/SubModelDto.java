package org.jboss.errai.cdi.event.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class SubModelDto
{
   public String field = "field";
   public String getField()
   {
      return field;
   }
   public void setField( final String field )
   {
      this.field = field;
   }
}