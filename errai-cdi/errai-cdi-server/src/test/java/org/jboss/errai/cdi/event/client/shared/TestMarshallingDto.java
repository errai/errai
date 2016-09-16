package org.jboss.errai.cdi.event.client.shared;

import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class TestMarshallingDto
{
   private Set< SubModelDto > empty = new HashSet<>();
   public Set< SubModelDto > getEmpty()
   {
      return empty;
   }
   public void setEmpty( final Set< SubModelDto > empty )
   {
      this.empty = empty;
   }
}