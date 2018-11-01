package org.jboss.resteasy.test.provider;

public class Dummy
{
   private String name;
   private int age;

   public Dummy(String name, int age)
   {
      this.name = name;
      this.age = age;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public int getAge()
   {
      return age;
   }

   public void setAge(int age)
   {
      this.age = age;
   }
}
